package pl.kurs.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.kurs.event.ClientVerificationEvent;
import pl.kurs.event.EmailVerifiedConfirmationEvent;
import pl.kurs.service.MailService;

@Component
@RequiredArgsConstructor
public class ClientVerificationEventListener {
    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleClientVerification(ClientVerificationEvent event) {
        mailService.sendVerificationEmail(event.getMail(), event.getToken());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailVerifiedConfirmation(EmailVerifiedConfirmationEvent event) {
        mailService.sendEmailVerifiedConfirmation(event.getMail());
    }
}
