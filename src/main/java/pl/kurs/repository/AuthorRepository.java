package pl.kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
