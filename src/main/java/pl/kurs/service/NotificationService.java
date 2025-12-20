package pl.kurs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.kurs.entity.Book;
import pl.kurs.event.BookCreatedEvent;
import pl.kurs.event.ClientVerificationEvent;

/**
 * NotificationService simulates an independent microservice responsible
 * for sending notifications.
 *
 * In a real-world architecture, communication would be asynchronous
 * (e.g. via a message broker such as Kafka).
 *
 * For the purposes of this project, communication is simplified and
 * implemented using Spring's local ApplicationEventPublisher.
 */

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ApplicationEventPublisher eventPublisher;

    public void publishCreatedBookNotification(Book book) {
        eventPublisher.publishEvent(new BookCreatedEvent(this, book.getId()));
    }

    public void publishClientRegistryNotification(String email, String token) {
        eventPublisher.publishEvent(new ClientVerificationEvent(this, email, token));
    }
}
