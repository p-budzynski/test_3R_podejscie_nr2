package pl.kurs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.dto.SubscriptionDto;
import pl.kurs.entity.Client;
import pl.kurs.entity.Subscription;
import pl.kurs.exception.InvalidSubscriptionException;
import pl.kurs.exception.ResourceNotFoundException;
import pl.kurs.exception.SubscriptionAlreadyExistsException;
import pl.kurs.mapper.SubscriptionMapper;
import pl.kurs.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final ClientService clientService;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public SubscriptionDto createSubscription(SubscriptionDto dto) {
        Client client = clientService.findVerifiedClientById(dto.getClientId());

        if (subscriptionExists(dto)) {
            throw new SubscriptionAlreadyExistsException("Subscription for client id: " + dto.getClientId() + " is already exists");
        }

        Subscription subscription = subscriptionMapper.dtoToEntity(dto);
        subscription.setClient(client);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return subscriptionMapper.entityToDto(savedSubscription);
    }

    @Transactional
    public void cancelSubscription(Long clientId, Long authorId, Long categoryId) {
        int deleteRows;

        if (authorId != null) {
            deleteRows = subscriptionRepository.deleteByClientIdAndAuthorId(clientId, authorId);
        } else {
            deleteRows = subscriptionRepository.deleteByClientIdAndCategoryId(clientId, categoryId);
        }

        if (deleteRows == 0) {
            throw new ResourceNotFoundException("No subscription found to cancel");
        }
    }

    private boolean subscriptionExists(SubscriptionDto dto) {
        if ((dto.getAuthorId() == null && dto.getCategoryId() == null) ||
            (dto.getAuthorId() != null && dto.getCategoryId() != null)) {
            throw new InvalidSubscriptionException("Subscription must have either authorId OR categoryId, but not both.");
        }

        if (dto.getCategoryId() != null) {
            return subscriptionRepository.existsByClientIdAndCategoryId(dto.getClientId(), dto.getCategoryId());
        } else {
            return subscriptionRepository.existsByClientIdAndAuthorId(dto.getClientId(), dto.getAuthorId());
        }
    }
}
