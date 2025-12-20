package pl.kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kurs.entity.MessageConfig;

import java.util.Optional;

@Repository
public interface MessageConfigRepository extends JpaRepository<MessageConfig, Long> {

    Optional<MessageConfig> findByCode(String code);
}
