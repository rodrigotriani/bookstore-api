package com.example.bookstore.order;

import com.example.bookstore.common.error.ConflictException;
import com.example.bookstore.support.IntegrationTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    OrderService orderService;
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * Deterministic overlap: transaction A holds the row lock on the last unit; order B is started while A is
     * still open and must block until A commits. After A takes the last unit, B must fail with a stock conflict.
     */
    @Test
    void secondOrderBlocksWhileFirstTransactionHoldsLockAndThenFailsOnStock() throws Exception {
        long customer = createCustomer("lock@example.com");
        long book = createBook("C-LOCK", "10.00", 1);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> holder = pool.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
                var locked = bookRepository.findAllByIdForUpdate(Set.of(book)).get(0);
                lockAcquired.countDown();
                await(releaseLock);
                locked.setStockQuantity(locked.getStockQuantity() - 1);
            }));
            assertThat(lockAcquired.await(10, TimeUnit.SECONDS)).isTrue();

            CompletableFuture<Object> second = new CompletableFuture<>();
            pool.submit(() -> {
                try {
                    second.complete(orderService.create(new OrderRequest(customer,
                            List.of(new OrderItemRequest(book, 1)))));
                } catch (Throwable t) {
                    second.completeExceptionally(t);
                }
            });

            // B overlaps with A's open transaction and must not complete while the lock is held.
            Thread.sleep(500);
            assertThat(second.isDone()).as("second order must wait for the row lock").isFalse();

            releaseLock.countDown();
            holder.get(10, TimeUnit.SECONDS);

            assertThatSecondFailedWithConflict(second);
        } finally {
            pool.shutdownNow();
        }
        assertThat(bookRepository.findById(book).orElseThrow().getStockQuantity()).isZero();
        assertThat(orderRepository.count()).isZero();
    }

    private static void assertThatSecondFailedWithConflict(CompletableFuture<Object> second) throws Exception {
        try {
            second.get(10, TimeUnit.SECONDS);
            throw new AssertionError("second order should not have succeeded");
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ConflictException.class);
        }
    }

    /** Many simultaneous buyers (released together by a barrier) compete for 3 units: exactly 3 may succeed. */
    @Test
    void manyConcurrentOrdersNeverOversell() throws Exception {
        int stock = 3;
        int buyers = 12;
        long customer = createCustomer("many@example.com");
        long book = createBook("C-MANY", "10.00", stock);

        CyclicBarrier barrier = new CyclicBarrier(buyers);
        ExecutorService pool = Executors.newFixedThreadPool(buyers);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < buyers; i++) {
                Callable<Boolean> task = () -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    try {
                        orderService.create(new OrderRequest(customer, List.of(new OrderItemRequest(book, 1))));
                        return true;
                    } catch (ConflictException e) {
                        return false;
                    }
                };
                results.add(pool.submit(task));
            }
            int succeeded = 0;
            for (Future<Boolean> result : results) {
                if (result.get(30, TimeUnit.SECONDS)) {
                    succeeded++;
                }
            }
            assertThat(succeeded).isEqualTo(stock);
        } finally {
            pool.shutdownNow();
        }
        assertThat(bookRepository.findById(book).orElseThrow().getStockQuantity()).isZero();
        assertThat(orderRepository.count()).isEqualTo(stock);
        assertThat(orderItemRepository.count()).isEqualTo(stock);
    }

    /** Opposite item ordering across two multi-book orders must not deadlock thanks to sorted lock acquisition. */
    @Test
    void ordersWithOppositeItemOrderDoNotDeadlock() throws Exception {
        long customer = createCustomer("deadlock@example.com");
        long b1 = createBook("C-DL1", "1.00", 50);
        long b2 = createBook("C-DL2", "1.00", 50);
        int rounds = 20;

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            Future<?> f1 = pool.submit(() -> run(rounds, barrier, customer, b1, b2));
            Future<?> f2 = pool.submit(() -> run(rounds, barrier, customer, b2, b1));
            f1.get(60, TimeUnit.SECONDS);
            f2.get(60, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        assertThat(bookRepository.findById(b1).orElseThrow().getStockQuantity()).isEqualTo(50 - 2 * rounds);
        assertThat(bookRepository.findById(b2).orElseThrow().getStockQuantity()).isEqualTo(50 - 2 * rounds);
    }

    private void run(int rounds, CyclicBarrier barrier, long customer, long first, long second) {
        try {
            for (int i = 0; i < rounds; i++) {
                barrier.await(10, TimeUnit.SECONDS);
                orderService.create(new OrderRequest(customer,
                        List.of(new OrderItemRequest(first, 1), new OrderItemRequest(second, 1))));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(20, TimeUnit.SECONDS)) {
                throw new IllegalStateException("timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
