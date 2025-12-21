package pl.kurs.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.entity.*;
import pl.kurs.repository.SubscriptionNotificationRepository;
import pl.kurs.repository.SubscriptionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionNotificationReaderServiceTest {

    @Mock
    private SubscriptionNotificationProcessingService processingServiceMock;

    @Mock
    private SubscriptionNotificationRepository notificationRepositoryMock;

    @Mock
    private SubscriptionRepository subscriptionRepositoryMock;

    @Mock
    private BookService bookServiceMock;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SubscriptionNotificationReaderService readerService;

    @Test
    void shouldCreateNotificationsForSubscriptionsSavesAllWhenLessThanBatchSize() {
        //given
        Long bookId = 1L;
        Book book = new Book();
        Author author = new Author();
        Category category = new Category();
        Client client = new Client();
        author.setId(1L);
        category.setId(1L);
        book.setAuthor(author);
        book.setCategory(category);
        Subscription subscription = new Subscription();
        subscription.setClient(client);

        List<Subscription> subscriptions = List.of(subscription);

        when(bookServiceMock.findBookById(bookId)).thenReturn(book);
        when(subscriptionRepositoryMock.streamByAuthorIdOrCategoryId(anyLong(), anyLong()))
                .thenAnswer(invocation -> subscriptions.stream());

        //when
        readerService.createNotificationsForSubscriptions(bookId);

        //then
        verify(notificationRepositoryMock, times(1)).saveAll(anyList());
        verify(entityManager, never()).clear();
    }

    @Test
    void shouldCreateNotificationsForSubscriptionsSavesInBatchesWhenMoreThanBatchSize() {
        //given
        Long bookId = 1L;
        Book book = new Book();
        Author author = new Author();
        Category category = new Category();
        Client client = new Client();

        author.setId(1L);
        category.setId(1L);
        book.setAuthor(author);
        book.setCategory(category);

        List<Subscription> subscriptions = new ArrayList<>();
        Subscription sub = new Subscription();
        sub.setClient(client);

        for (int i = 0; i < 501; i++) {
            subscriptions.add(sub);
        }

        when(bookServiceMock.findBookById(bookId)).thenReturn(book);
        when(subscriptionRepositoryMock.streamByAuthorIdOrCategoryId(anyLong(), anyLong()))
                .thenAnswer(invocation -> subscriptions.stream());

        //when
        readerService.createNotificationsForSubscriptions(bookId);

        //then
        verify(notificationRepositoryMock, times(2)).saveAll(anyList());
        verify(notificationRepositoryMock, times(1)).flush();
        verify(entityManager, times(1)).clear();
        verify(entityManager, times(1)).merge(book);
    }

    @Test
    void shouldNotCreateNotificationWhenNoSubscription() {
        //given
        Long bookId = 1L;
        Book book = new Book();
        Author author = new Author();
        Category category = new Category();
        author.setId(1L);
        category.setId(1L);
        book.setAuthor(author);
        book.setCategory(category);

        when(bookServiceMock.findBookById(bookId)).thenReturn(book);
        when(subscriptionRepositoryMock.streamByAuthorIdOrCategoryId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Stream.empty());

        //when
        readerService.createNotificationsForSubscriptions(bookId);

        //then
        verify(notificationRepositoryMock, never()).saveAll(anyList());
        verify(entityManager, never()).clear();
    }

    @Test
    void shouldProcessNotificationGroupedByClient() {
        //given
        Client client1 = new Client();
        client1.setId(1L);

        Client client2 = new Client();
        client2.setId(2L);

        SubscriptionNotification notification1 = new SubscriptionNotification(1L, client1, new Book());
        SubscriptionNotification notification2 = new SubscriptionNotification(2L, client1, new Book());
        SubscriptionNotification notification3 = new SubscriptionNotification(3L, client2, new Book());

        List<SubscriptionNotification> notifications = List.of(notification1, notification2, notification3);

        when(notificationRepositoryMock.streamSubscriptionNotification())
                .thenAnswer(invocation -> notifications.stream());

        //when
        readerService.processAllNotificationsStream();

        //then
        verify(processingServiceMock).processBucket(
                argThat(client -> client.getId().equals(1L)),
                argThat(list -> list.size() == 2));
        verify(processingServiceMock).processBucket(
                argThat(client -> client.getId().equals(2L)),
                argThat(list -> list.size() == 1));
        verify(entityManager, times(1)).clear();
    }

    @Test
    void shouldHandleEmptyStream() {
        //given
        when(notificationRepositoryMock.streamSubscriptionNotification())
                .thenAnswer(invocation -> Stream.empty());

        //when
        readerService.processAllNotificationsStream();

        //then
        verify(processingServiceMock, never()).processBucket(any(), any());
    }
}
