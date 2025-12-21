package pl.kurs.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.entity.Book;
import pl.kurs.entity.Client;
import pl.kurs.entity.Subscription;
import pl.kurs.entity.SubscriptionNotification;
import pl.kurs.repository.SubscriptionNotificationRepository;
import pl.kurs.repository.SubscriptionRepository;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionNotificationReaderService {
    private final SubscriptionNotificationProcessingService processingService;
    private final SubscriptionNotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BookService bookService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional
    public void createNotificationsForSubscriptions(Long bookId) {
        Book book = bookService.findBookById(bookId);
        List<SubscriptionNotification> batch = new ArrayList<>();

        Set<Long> processedClientIds = new HashSet<>();

        try (Stream<Subscription> authorStream = subscriptionRepository.streamByAuthorId(book.getAuthor().getId());
             Stream<Subscription> categoryStream = subscriptionRepository.streamByCategoryId(book.getCategory().getId())) {

            Stream.concat(authorStream, categoryStream)
                    .filter(sub -> processedClientIds.add(sub.getClient().getId()))
                    .forEach(subscription -> {
                        batch.add(SubscriptionNotification.builder()
                                .client(subscription.getClient())
                                .book(book)
                                .build());

                        if (batch.size() >= 500) {
                            saveBatch(batch, book, true);
                        }
                    });

            if (!batch.isEmpty()) {
                saveBatch(batch, book, false);
            }
        }
    }

    @Transactional
    public void processAllNotificationsStream() {
        Long currentClientId = null;
        Client currentClient = null;
        List<SubscriptionNotification> bucket = new ArrayList<>();

        try (Stream<SubscriptionNotification> stream = notificationRepository.streamSubscriptionNotification()) {
            Iterator<SubscriptionNotification> it = stream.iterator();

            while (it.hasNext()) {
                SubscriptionNotification sn = it.next();
                Long clientId = sn.getClient().getId();

                if (currentClientId != null && !currentClientId.equals(clientId)) {
                    processingService.processBucket(currentClient, new ArrayList<>(bucket));

                    bucket.clear();
                    entityManager.clear();
                }

                currentClientId = clientId;
                currentClient = sn.getClient();
                bucket.add(sn);
            }

            if (!bucket.isEmpty()) {
                processingService.processBucket(currentClient, new ArrayList<>(bucket));
            }
        }
    }

    private void saveBatch(List<SubscriptionNotification> batch, Book book, boolean shouldClear) {
        notificationRepository.saveAll(batch);
        notificationRepository.flush();

        if (shouldClear) {
            entityManager.clear();
            batch.clear();
            entityManager.merge(book);
        } else {
            batch.clear();
        }
    }
}
