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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

        try (Stream<Subscription> subscriptionStream = subscriptionRepository.streamByAuthorIdOrCategoryId(
                book.getAuthor().getId(), book.getCategory().getId())) {

            subscriptionStream.forEach(subscription -> {
                batch.add(SubscriptionNotification.builder()
                        .client(subscription.getClient())
                        .book(book)
                        .build());

                if (batch.size() >= 500) {
                    notificationRepository.saveAll(batch);
                    notificationRepository.flush();
                    entityManager.clear();
                    batch.clear();
                    entityManager.merge(book);
                }

            });

            if (!batch.isEmpty()) {
                notificationRepository.saveAll(batch);
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
}
