package pl.kurs.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.entity.*;
import pl.kurs.repository.BookRepository;
import pl.kurs.repository.ClientRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookNotificationProcessorTest {

    @Mock
    private ClientRepository clientRepositoryMock;

    @Mock
    private BookRepository bookRepositoryMock;

    @Mock
    private MailService mailServiceMock;

    @Mock
    private EntityManager entityManagerMock;

    @InjectMocks
    private BookNotificationProcessor notificationProcessor;

    @Test
    void shouldMatchBooksAndSendEmail() {
        //given
        LocalDate day = LocalDate.now();
        MessageConfig template = new MessageConfig();

        Author author = new Author(1L, "Stephen", "King", null);
        Category category = new Category(1L, "Horror");

        Book book1 = new Book(10L, "It", new Category(3L, "Other"), 100, author, null);
        Book book2 = new Book(11L, "The Shining", category, 200, new Author(4L, "Other", "A", null), null);

        Client client = new Client();
        client.setId(100L);
        client.setFirstName("Jan");
        client.setEmail("jan@test.pl");

        Subscription subAuthor = new Subscription();
        subAuthor.setAuthor(author);

        Subscription subCategory = new Subscription();
        subCategory.setCategory(category);

        client.setSubscriptions(Set.of(subAuthor, subCategory));

        when(bookRepositoryMock.findAllByCreatedAtWithRelations(day)).thenReturn(List.of(book1, book2));
        when(clientRepositoryMock.findVerifiedWithSubscriptions()).thenReturn(Stream.of(client));

        //when
        notificationProcessor.processSubscriptions(day, template);

        //then
        ArgumentCaptor<Set<Book>> booksCaptor = ArgumentCaptor.forClass(Set.class);
        verify(mailServiceMock).sendNewBookNotifications(eq(client.getFirstName()), eq(client.getEmail()), booksCaptor.capture(), eq(template));

        Set<Book> matchedBooks = booksCaptor.getValue();
        assertThat(matchedBooks).hasSize(2);
        assertThat(matchedBooks).contains(book1, book2);

        verify(entityManagerMock).detach(client);
    }

    @Test
    void shouldLimitBooksPerMail() {
        //given
        LocalDate day = LocalDate.now();
        MessageConfig template = new MessageConfig();

        Category fantasy = new Category(1L, "Fantasy");
        List<Book> fortyBooks = new ArrayList<>();
        for (long i = 1; i <= 40; i++) {
            fortyBooks.add(new Book(i, "Book " + i, fantasy, 100, new Author(i, "A", "B", null), null));
        }

        Client client = new Client();
        client.setFirstName("Jan");
        client.setEmail("jan@test.pl");

        Subscription sub = new Subscription();
        sub.setCategory(fantasy);
        client.setSubscriptions(Set.of(sub));

        when(bookRepositoryMock.findAllByCreatedAtWithRelations(day)).thenReturn(fortyBooks);
        when(clientRepositoryMock.findVerifiedWithSubscriptions()).thenReturn(Stream.of(client));

        //when
        notificationProcessor.processSubscriptions(day, template);

        //then
        ArgumentCaptor<Set<Book>> booksCaptor = ArgumentCaptor.forClass(Set.class);
        verify(mailServiceMock).sendNewBookNotifications(anyString(), anyString(), booksCaptor.capture(), any());

        Set<Book> sentBooks = booksCaptor.getValue();

        assertThat(sentBooks).hasSize(30);
    }

    @Test
    void shouldNotSendEmailWhenNoMatch() {
        //given
        LocalDate day = LocalDate.now();
        when(bookRepositoryMock.findAllByCreatedAtWithRelations(day)).thenReturn(List.of());
        when(clientRepositoryMock.findVerifiedWithSubscriptions()).thenReturn(Stream.of(new Client()));

        //when
        notificationProcessor.processSubscriptions(day, new MessageConfig());

        //then
        verify(mailServiceMock, never()).sendNewBookNotifications(any(), any(), any(), any());
    }

}