package pl.kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByClientIdAndCategoryId(Long clientId, Long categoryId);

    boolean existsByClientIdAndAuthorId(Long clientId, Long AuthorId);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.client.id = :clientId AND s.author.id = :authorId")
    int deleteByClientIdAndAuthorId(@Param("clientId") Long clientId, @Param("authorId") Long authorId);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.client.id = :clientId AND s.category.id = :categoryId")
    int deleteByClientIdAndCategoryId(@Param("clientId") Long clientId, @Param("categoryId") Long categoryId);
}
