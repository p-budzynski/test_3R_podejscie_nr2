package pl.kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.entity.Book;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("""
            SELECT b FROM Book b
            JOIN FETCH b.category
            JOIN FETCH b.author
            WHERE b.id = :bookId
            """)
    Optional<Book> findByIdWithCategoryAndAuthor(@Param("bookId") Long bookId);
}
