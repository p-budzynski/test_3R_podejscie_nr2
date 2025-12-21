package pl.kurs.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kurs.entity.Subscription;

import java.util.stream.Stream;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {


    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "500"),
            @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    @Query("SELECT s FROM Subscription s JOIN FETCH s.client WHERE s.author.id = :authorId")
    Stream<Subscription> streamByAuthorId(@Param("authorId") Long authorId);

    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "500"),
            @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    @Query("SELECT s FROM Subscription s JOIN FETCH s.client WHERE s.category.id = :categoryId ")
    Stream<Subscription> streamByCategoryId(@Param("categoryId") Long categoryId);

    boolean existsByClientIdAndCategoryId(Long clientId, Long categoryId);

    boolean existsByClientIdAndAuthorId(Long clientId, Long AuthorId);

    int deleteByClientIdAndAuthorId(Long clientId, Long authorId);

    int deleteByClientIdAndCategoryId(Long clientId, Long categoryId);
}
