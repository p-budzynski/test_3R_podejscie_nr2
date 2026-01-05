package pl.kurs.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.entity.Book;
import pl.kurs.entity.Client;
import pl.kurs.entity.MessageConfig;
import pl.kurs.entity.Subscription;
import pl.kurs.repository.BookRepository;
import pl.kurs.repository.ClientRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookNotificationProcessor {
    private static final int MAX_BOOKS_PER_MAIL = 30;

    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final MailService mailService;
    private final EntityManager entityManager;

    public void processSubscriptions(LocalDate day, MessageConfig template) {
        List<Book> newBooks = bookRepository.findAllByCreatedAtWithRelations(day);

        Map<Long, List<Book>> byAuthor = newBooks.stream()
                .collect(Collectors.groupingBy(b -> b.getAuthor().getId()));
        Map<Long, List<Book>> byCategory = newBooks.stream()
                .collect(Collectors.groupingBy(b -> b.getCategory().getId()));

        try (Stream<Client> clientStream = clientRepository.findVerifiedWithSubscriptions()) {
            clientStream.forEach(client -> {
                Set<Book> booksForClient = new HashSet<>();

                for (Subscription sub : client.getSubscriptions()) {
                    if (sub.getAuthor() != null) {
                        booksForClient.addAll(byAuthor.getOrDefault(sub.getAuthor().getId(), List.of()));
                    }
                    if (sub.getCategory() != null) {
                        booksForClient.addAll(byCategory.getOrDefault(sub.getCategory().getId(), List.of()));
                    }

                    if (booksForClient.size() >= MAX_BOOKS_PER_MAIL) break;
                }

                if (!booksForClient.isEmpty()) {
                    Set<Book> limitedBooks = booksForClient.stream()
                            .limit(MAX_BOOKS_PER_MAIL)
                            .collect(Collectors.toSet());
                    mailService.sendNewBookNotifications(client.getFirstName(), client.getEmail(), limitedBooks, template);
                }

                entityManager.detach(client);
            });
        }
    }

}
