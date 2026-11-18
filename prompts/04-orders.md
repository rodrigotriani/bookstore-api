# Stage 04 — Orders, inventory and transactions

Implement an Orders module connecting existing customers to books. Design for correct accounting and stock updates.

## Model
- `BookOrder` (avoid SQL reserved word `order`): generated id, `Customer` association, created timestamp, `BigDecimal total`, collection of OrderItem.
- `OrderItem`: id, order association, book association, quantity positive, `BigDecimal unitPrice`, `BigDecimal subtotal`. Snapshot price at purchase time; don't recalculate historical totals from mutable books.
- Persist order and items with proper cascading; avoid unintended cascading deletes to books/customers.

## API
- `POST /api/orders`: `{ "customerId": 1, "items": [{ "bookId": 1, "quantity": 2 }] }`. Return 201 with order id, customer, items, subtotals, total and timestamp.
- `GET /api/orders`: list with safe DTO serialization.
- `GET /api/orders/{id}`: detail or 404.

## Rules
1. Require customer to exist; require nonempty items, IDs and positive quantities. Return 400/404 as appropriate.
2. Reject repeated book IDs in a single order request as **400 Bad Request** (document and test this exact policy).
3. Check all requested books exist and have sufficient stock. Return 409 for insufficient inventory.
4. Calculate unit prices, item subtotals and grand total with BigDecimal.
5. Use Spring-managed `@Transactional` on order creation to atomically persist order/items and decrement stock. Any failure must roll back all changes.
6. Prevent overselling under concurrent purchases. Prefer pessimistic write locking of Book rows, acquire in sorted book-ID order and keep locks within a single transaction. Ensure the approach works with H2 as configured; test two overlapping transactions attempting the final stock unit. Ensure stock never negative. If full concurrency guarantee cannot be demonstrated, report the limitation rather than claim success.
7. Preserve history: if a book or customer is referenced in an existing order, reject deletion with 409. Historical item prices must not change on price updates.
8. No payment gateways, login, external email or credit card data for this demo.

## Testing
Successful single/multiple items, totals, snapshots, stock deduction, invalid quantities, nonexistent customer/book, duplicate book IDs, insufficient stock, transaction rollback, concurrent orders.

Run `mvn test`; correct errors and continue to Stage 05.
