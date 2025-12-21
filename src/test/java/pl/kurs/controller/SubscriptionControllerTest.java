package pl.kurs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.dto.SubscriptionDto;
import pl.kurs.entity.Author;
import pl.kurs.entity.Category;
import pl.kurs.entity.Client;
import pl.kurs.entity.Subscription;
import pl.kurs.repository.*;
import pl.kurs.service.SubscriptionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionNotificationRepository notificationRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void clean() {
        notificationRepository.deleteAll();
        subscriptionRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldCreateSubscription() throws Exception {
        //given
        Client client = clientRepository.save(new Client(null, "Test name", "Test name", "test@mail.com", "Test city", true, null, null));
        Category category = categoryRepository.save(new Category(null, "Test Category"));

        SubscriptionDto subscriptionDto = new SubscriptionDto();
        subscriptionDto.setClientId(client.getId());
        subscriptionDto.setCategoryId(category.getId());

        //when then
        mockMvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(subscriptionDto.getClientId()))
                .andExpect(jsonPath("$.categoryId").value(subscriptionDto.getCategoryId()));
    }

    @Test
    void shouldCancelSubscriptionWithCategory() throws Exception {
        //given
        Subscription subscription = createSubscriptionWithCategory();
        Long clientId = subscription.getClient().getId();
        Long categoryId = subscription.getCategory().getId();

        //when then
        mockMvc.perform(delete("/subscriptions")
                        .param("clientId", String.valueOf(clientId))
                        .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription cancelled"));
    }

    @Test
    void shouldCancelSubscriptionWithAuthor() throws Exception {
        //given
        Subscription subscription = createSubscriptionWithAuthor();
        Long clientId = subscription.getClient().getId();
        Long authorId = subscription.getAuthor().getId();

        //when then
        mockMvc.perform(delete("/subscriptions")
                        .param("clientId", String.valueOf(clientId))
                        .param("authorId", String.valueOf(authorId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription cancelled"));
    }

    @Test
    void shouldReturnBadRequestWhenNoIdsProvided() throws Exception {
        //when then
        mockMvc.perform(delete("/subscriptions")
                        .param("clientId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage")
                        .value("You must provide either authorId or categoryId"));
    }

    @Test
    void shouldReturnBadRequestWhenBothIdsProvided() throws Exception {
        //when then
        mockMvc.perform(delete("/subscriptions")
                        .param("clientId", "1")
                        .param("authorId", "1")
                        .param("categoryId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage")
                        .value("You must provide either authorId or categoryId"));
    }

    @Test
    void shouldReturnBadRequestWhenClientIdMissing() throws Exception {
        //when then
        mockMvc.perform(delete("/subscriptions")
                        .param("authorId", "1"))
                .andExpect(status().isBadRequest());
    }

    private Subscription createSubscriptionWithCategory() {
        Client client = clientRepository.save(new Client(null, "Test firstName", "Test lastName", "test@mail.com", "Test city", true, null, null));
        Category category = categoryRepository.save(new Category(null, "Test Category"));

        Subscription subscription = new Subscription(null, client, null, category);
        return subscriptionRepository.save(subscription);
    }

    private Subscription createSubscriptionWithAuthor() {
        Client client = clientRepository.save(new Client(null, "Test firstName", "Test lastName", "test@mail.com", "Test city", true, null, null));
        Author author = authorRepository.save(new Author(null, "Test firstName", "Test lastName", null));
        Subscription subscription = new Subscription(null, client, author, null);
        return subscriptionRepository.save(subscription);
    }
}
