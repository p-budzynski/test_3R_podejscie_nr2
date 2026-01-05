package pl.kurs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.entity.MessageConfig;
import pl.kurs.repository.MessageConfigRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageConfigServiceTest {

    @Mock
    private MessageConfigRepository configRepositoryMock;

    @InjectMocks
    private MessageConfigService messageConfigService;

    @Test
    void shouldReturnMessageConfigByCode() {
        //given
        String code = "TestCode";
        MessageConfig messageConfig = new MessageConfig(1L, code, "TestSubject", "TestBody");

        when(configRepositoryMock.findByCode(code)).thenReturn(Optional.of(messageConfig));

        //when
        MessageConfig result = messageConfigService.findMessageConfigByCode(code);

        //then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(messageConfig);
    }

    @Test
    void shouldThrowExceptionWhenConfigNotFound() {
        //given
        String code = "UNKNOWN";

        when(configRepositoryMock.findByCode(code)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> messageConfigService.findMessageConfigByCode(code))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Message Config not found for code: " + code);
    }
}
