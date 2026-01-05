package pl.kurs.repository;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import pl.kurs.entity.Client;

import java.util.Optional;
import java.util.stream.Stream;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByVerificationToken(String token);

    @QueryHints(value = {
            @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "1000"),
            @QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "false"),
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("""
            SELECT DISTINCT c FROM Client c
            JOIN FETCH c.subscriptions
            WHERE c.emailVerified = true
            """)
    Stream<Client> findVerifiedWithSubscriptions();
}
