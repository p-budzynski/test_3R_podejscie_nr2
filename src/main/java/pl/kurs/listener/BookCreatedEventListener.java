package pl.kurs.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.kurs.event.BookCreatedEvent;
import pl.kurs.service.SubscriptionNotificationReaderService;

/**
 * Component listening for the BookCreatedEvent.
 *
 * In a microservices architecture, this component would operate as
 * an independent microservice consuming events from a message broker
 * (e.g. Kafka).
 *
 * For demonstration purposes and to simplify the implementation,
 * Spring's local event mechanism (@EventListener) is used.
 */

@Component
@RequiredArgsConstructor
public class BookCreatedEventListener {
    private final SubscriptionNotificationReaderService notificationService;

    @EventListener
    public void handleBookCreated(BookCreatedEvent event) {
        notificationService.createNotificationsForSubscriptions(event.getBookId());
    }
}
