package pl.kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
