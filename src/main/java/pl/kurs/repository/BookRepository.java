package pl.kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.entity.Book;

import java.time.LocalDate;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("""
            SELECT DISTINCT b from Book b
            JOIN FETCH b.category
            JOIN FETCH b.author
            WHERE b.createdAt = :startDate
            """)
    List<Book> findAllByCreatedAtWithRelations(@Param("startDate") LocalDate startDate);
}
