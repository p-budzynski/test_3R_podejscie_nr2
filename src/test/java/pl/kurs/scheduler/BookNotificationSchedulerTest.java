package pl.kurs.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.entity.MessageConfig;
import pl.kurs.service.BookNotificationProcessor;
import pl.kurs.service.MessageConfigService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookNotificationSchedulerTest {

    @Mock
    private BookNotificationProcessor notificationProcessorMock;

    @Mock
    private MessageConfigService messageConfigServiceMock;

    @InjectMocks
    private BookNotificationScheduler scheduler;

    @Test
    void shouldCallNotificationServiceWhenJobRuns() {
        //given
        MessageConfig mockTemplate = new MessageConfig();
        mockTemplate.setCode("NEW_BOOKS");

        when(messageConfigServiceMock.findMessageConfigByCode("NEW_BOOKS"))
                .thenReturn(mockTemplate);

        //when
        scheduler.runDailyNotification();

        //then
        verify(notificationProcessorMock, times(1))
                .processSubscriptions(eq(LocalDate.now().minusDays(1)), eq(mockTemplate));
    }

    @Test
    void shouldNotThrowExceptionWhenNotificationServiceFails() {
        //given
        when(messageConfigServiceMock.findMessageConfigByCode(anyString()))
                .thenReturn(new MessageConfig());

        //when
        doThrow(new RuntimeException("**** TEST - Daily notification job failed ****"))
                .when(notificationProcessorMock).processSubscriptions(any(), any());

        scheduler.runDailyNotification();

        //then
        verify(notificationProcessorMock).processSubscriptions(any(), any());
    }
}
