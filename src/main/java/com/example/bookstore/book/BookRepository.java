package com.example.bookstore.book;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /** Loads books with a pessimistic write lock, ordered by id so concurrent transactions lock in the same order. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id in :ids order by b.id asc")
    List<Book> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
