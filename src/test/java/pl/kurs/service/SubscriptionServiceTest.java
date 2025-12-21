package pl.kurs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.dto.SubscriptionDto;
import pl.kurs.entity.Author;
import pl.kurs.entity.Category;
import pl.kurs.entity.Client;
import pl.kurs.entity.Subscription;
import pl.kurs.exception.InvalidSubscriptionException;
import pl.kurs.exception.ResourceNotFoundException;
import pl.kurs.exception.SubscriptionAlreadyExistsException;
import pl.kurs.mapper.SubscriptionMapper;
import pl.kurs.repository.SubscriptionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepositoryMock;

    @Mock
    private ClientService clientServiceMock;

    @Mock
    private SubscriptionMapper subscriptionMapperMock;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void shouldCreateSubscriptionWithAuthor() {
        //given
        SubscriptionDto subscriptionDto = new SubscriptionDto(null, 1L, 1L, null);
        Subscription subscription = createSubscriptionWithAuthor();
        Subscription savedSubscription = createSavedSubscriptionWithAuthor();
        Client client = createClient();
        SubscriptionDto expectedDto = new SubscriptionDto(1L, 1L, 1L, null);

        given(clientServiceMock.findVerifiedClientById(1L)).willReturn(client);
        given(subscriptionRepositoryMock.existsByClientIdAndAuthorId(1L, 1L)).willReturn(false);
        given(subscriptionMapperMock.dtoToEntity(subscriptionDto)).willReturn(subscription);
        given(subscriptionRepositoryMock.save(any(Subscription.class))).willReturn(savedSubscription);
        given(subscriptionMapperMock.entityToDto(savedSubscription)).willReturn(expectedDto);

        //when
        SubscriptionDto result = subscriptionService.createSubscription(subscriptionDto);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getAuthorId()).isEqualTo(1L);
        assertThat(result.getCategoryId()).isNull();
    }

    @Test
    void shouldCreateSubscriptionWithCategory() {
        //given
        SubscriptionDto subscriptionDto = new SubscriptionDto(null, 1L, null, 1L);
        Subscription subscription = createSubscriptionWithCategory();
        Subscription savedSubscription = createSavedSubscriptionWithCategory();
        Client client = createClient();
        SubscriptionDto expectedDto = new SubscriptionDto(1L, 1L, null, 1L);

        given(clientServiceMock.findVerifiedClientById(1L)).willReturn(client);
        given(subscriptionRepositoryMock.existsByClientIdAndCategoryId(1L, 1L)).willReturn(false);
        given(subscriptionMapperMock.dtoToEntity(subscriptionDto)).willReturn(subscription);
        given(subscriptionRepositoryMock.save(any(Subscription.class))).willReturn(savedSubscription);
        given(subscriptionMapperMock.entityToDto(savedSubscription)).willReturn(expectedDto);

        //when
        SubscriptionDto result = subscriptionService.createSubscription(subscriptionDto);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getAuthorId()).isNull();
        assertThat(result.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void shouldCancelSubscriptionWithAuthor() {
        //given
        Long clientId = 1L;
        Long authorId = 1L;
        Long categoryId = null;

        when(subscriptionRepositoryMock.deleteByClientIdAndAuthorId(clientId, authorId)).thenReturn(1);

        //when
        subscriptionService.cancelSubscription(clientId, authorId, categoryId);

        //then
        verify(subscriptionRepositoryMock).deleteByClientIdAndAuthorId(clientId, authorId);
        verify(subscriptionRepositoryMock, never()).deleteByClientIdAndCategoryId(anyLong(), anyLong());

    }

    @Test
    void shouldCancelSubscriptionWithCategory() {
        //given
        Long clientId = 1L;
        Long authorId = null;
        Long categoryId = 1L;

        when(subscriptionRepositoryMock.deleteByClientIdAndCategoryId(clientId, categoryId)).thenReturn(1);

        //when
        subscriptionService.cancelSubscription(clientId, authorId, categoryId);

        //then
        verify(subscriptionRepositoryMock).deleteByClientIdAndCategoryId(clientId, categoryId);
        verify(subscriptionRepositoryMock, never()).deleteByClientIdAndAuthorId(anyLong(), anyLong());
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionNotFound() {
        //given
        Long clientId = 1L;
        Long authorId = 1L;
        Long categoryId = null;

        when(subscriptionRepositoryMock.deleteByClientIdAndAuthorId(clientId, authorId))
                .thenReturn(0);

        //when then
        assertThatThrownBy(() -> subscriptionService.cancelSubscription(clientId, authorId, categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No subscription found to cancel");
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionAlreadyExists() {
        //given
        Client client = createClient();
        SubscriptionDto subscriptionDto = new SubscriptionDto(null, client.getId(), 1L, null);

        given(clientServiceMock.findVerifiedClientById(client.getId())).willReturn(client);
        given(subscriptionRepositoryMock.existsByClientIdAndAuthorId(client.getId(), 1L)).willReturn(true);

        //when then
        assertThatThrownBy(() -> subscriptionService.createSubscription(subscriptionDto))
                .isInstanceOf(SubscriptionAlreadyExistsException.class)
                .hasMessage("Subscription for client id: " + client.getId() + " is already exists");
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionHasAuthorNullAndCategoryNull() {
        //given
        Client client = createClient();
        SubscriptionDto subscriptionDto = new SubscriptionDto(null, client.getId(), null, null);

        given(clientServiceMock.findVerifiedClientById(client.getId())).willReturn(client);

        //when then
        assertThatThrownBy(() -> subscriptionService.createSubscription(subscriptionDto))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessage("Subscription must have either authorId OR categoryId, but not both.");
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionHasAuthorAndCategory() {
        //given
        Client client = createClient();
        SubscriptionDto subscriptionDto = new SubscriptionDto(null, client.getId(), 1L, 1L);

        given(clientServiceMock.findVerifiedClientById(client.getId())).willReturn(client);

        //when then
        assertThatThrownBy(() -> subscriptionService.createSubscription(subscriptionDto))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessage("Subscription must have either authorId OR categoryId, but not both.");
    }

    private Client createClient() {
        return new Client(1L, "Test firstName", "Test lastName", "test@mail.com", "Test city", true, null, null);
    }

    private Author createAuthor() {
        return new Author(1L, "Test firstName", "Test lastName", null);
    }

    private Category createCategory() {
        return new Category(1L, "Test category");
    }

    private Subscription createSubscriptionWithAuthor() {
        return Subscription.builder()
                .client(createClient())
                .author(createAuthor())
                .category(null)
                .build();
    }

    private Subscription createSubscriptionWithCategory() {
        return Subscription.builder()
                .client(createClient())
                .author(null)
                .category(createCategory())
                .build();
    }

    private Subscription createSavedSubscriptionWithAuthor() {
        return Subscription.builder()
                .id(1L)
                .client(createClient())
                .author(createAuthor())
                .category(null)
                .build();
    }

    private Subscription createSavedSubscriptionWithCategory() {
        return Subscription.builder()
                .id(1L)
                .client(createClient())
                .author(null)
                .category(createCategory())
                .build();
    }
}
