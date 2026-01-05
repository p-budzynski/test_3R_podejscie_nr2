package pl.kurs.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.kurs.entity.MessageConfig;
import pl.kurs.service.BookNotificationProcessor;
import pl.kurs.service.MessageConfigService;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookNotificationScheduler {
    private final BookNotificationProcessor bookNotificationProcessor;
    private final MessageConfigService messageConfigService;

    @Scheduled(cron = "${app.scheduling.daily-notifications}")
    public void runDailyNotification() {
        LocalDate day = LocalDate.now().minusDays(1);
        MessageConfig template = messageConfigService.findMessageConfigByCode("NEW_BOOKS");
        log.info("Starting daily notification job for day={}", day);

        try {
            bookNotificationProcessor.processSubscriptions(day, template);
            log.info("Daily notification job completed successfully for day={}", day);
        } catch (Exception ex) {
            log.error("Daily notification job failed for day={}", day, ex);
        }
    }
}
