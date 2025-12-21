package pl.kurs.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.kurs.event.ClientVerificationEvent;
import pl.kurs.service.MailService;

/**
 * Component listening for the ClientVerificationEvent.
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
public class ClientVerificationEventListener {
    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleClientVerification(ClientVerificationEvent event) {
        mailService.sendVerificationEmail(event.getMail(), event.getToken());
    }
}
