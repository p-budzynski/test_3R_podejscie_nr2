package pl.kurs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.dto.ClientDto;
import pl.kurs.entity.Client;
import pl.kurs.exception.EmailNotVerifiedException;
import pl.kurs.exception.ResourceNotFoundException;
import pl.kurs.mapper.ClientMapper;
import pl.kurs.repository.ClientRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepositoryMock;

    @Mock
    private ClientMapper clientMapperMock;

    @Mock
    private MailService mailServiceMock;

    @InjectMocks
    private ClientService clientService;

    @Test
    void shouldRegisterClientSuccessfully() {
        //given
        ClientDto clientDto = createClientDto();
        Client client = createClient();
        Client savedClient = createSavedClient();
        String expectedToken = "generated-uuid-token";

        try (MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            UUID mockUuid = mock(UUID.class);
            uuidMock.when(UUID::randomUUID).thenReturn(mockUuid);
            when(mockUuid.toString()).thenReturn(expectedToken);

            when(clientMapperMock.dtoToEntity(clientDto)).thenReturn(client);
            when(clientRepositoryMock.save(any(Client.class))).thenReturn(savedClient);
            doNothing().when(mailServiceMock).sendVerificationEmail(clientDto.getEmail(), expectedToken);
            when(clientMapperMock.entityToDto(savedClient)).thenReturn(clientDto);

            //when
            ClientDto result = clientService.createClient(clientDto);

            //then
            assertThat(result).isEqualTo(clientDto);
        }
    }

    @Test
    void shouldReturnClientById() {
        //given
        Long clientId = 1L;
        Client savedClient = createSavedClient();
        when(clientRepositoryMock.findById(clientId)).thenReturn(Optional.of(savedClient));

        //when
        Client result = clientService.findClientById(clientId);

        //then
        assertThat(result).isEqualTo(savedClient);
    }

    @Test
    void shouldThrowExceptionWhenClientNotFoundById() {
        //given
        Long clientId = 1L;
        when(clientRepositoryMock.findById(clientId)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> clientService.findClientById(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client not found with id: " + clientId);
    }

    @Test
    void shouldReturnVerifiedClientById() {
        //given
        Long clientId = 1L;
        Client verifiedClient = createVerifiedClient(clientId);

        when(clientRepositoryMock.findById(clientId)).thenReturn(Optional.of(verifiedClient));

        //when
        Client result = clientService.findVerifiedClientById(clientId);

        //then
        assertThat(result).isEqualTo(verifiedClient);
        assertThat(result.getEmailVerified()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenClientEmailNotVerified() {
        //given
        Long clientId = 1L;
        Client unverifiedClient = createUnverifiedClient(clientId);

        when(clientRepositoryMock.findById(clientId)).thenReturn(Optional.of(unverifiedClient));

        //when then
        assertThatThrownBy(() -> clientService.findVerifiedClientById(clientId))
                .isInstanceOf(EmailNotVerifiedException.class)
                .hasMessage("Email must be verified before creating subscription");
    }

    private ClientDto createClientDto() {
        return new ClientDto(1L, "Jan", "Kowalski", "test@example.com", false, "Warszawa");
    }

    private Client createClient() {
        return Client.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .email("test@example.com")
                .city("Warszawa")
                .emailVerified(false)
                .build();
    }

    private Client createSavedClient() {
        return Client.builder()
                .id(1L)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("test@example.com")
                .city("Warszawa")
                .emailVerified(false)
                .verificationToken("test-token")
                .build();
    }

    private Client createVerifiedClient(Long id) {
        return Client.builder()
                .id(id)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("test@example.com")
                .city("Warszawa")
                .emailVerified(true)
                .build();
    }

    private Client createUnverifiedClient(Long id) {
        return Client.builder()
                .id(id)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("test@example.com")
                .city("Warszawa")
                .emailVerified(false)
                .build();
    }
}
