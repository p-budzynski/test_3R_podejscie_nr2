package pl.kurs.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmailVerifiedConfirmationEvent extends ApplicationEvent {
    private final String mail;

    public EmailVerifiedConfirmationEvent(Object source, String mail) {
        super(source);
        this.mail = mail;
    }
}
