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

    @Query("""
                SELECT s FROM Subscription s
                JOIN FETCH s.client
                WHERE (s.author.id = :authorId)
                   OR (s.category.id = :categoryId)
            """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "500"),
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.cacheable", value = "false")
    })
    Stream<Subscription> streamByAuthorIdOrCategoryId(
            @Param("authorId") Long authorId,
            @Param("categoryId") Long categoryId);

    boolean existsByClientIdAndCategoryId(Long clientId, Long categoryId);

    boolean existsByClientIdAndAuthorId(Long clientId, Long AuthorId);

    int deleteByClientIdAndAuthorId(Long clientId, Long authorId);

    int deleteByClientIdAndCategoryId(Long clientId, Long categoryId);
}
