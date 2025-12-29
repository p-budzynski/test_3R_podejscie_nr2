package pl.kurs.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.kurs.dto.SubscriptionDto;
import pl.kurs.exception.InvalidSubscriptionException;
import pl.kurs.service.SubscriptionService;
import pl.kurs.validation.Create;

@Validated
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto createSubscription(@Validated(Create.class) @RequestBody SubscriptionDto subscriptionDto) {
        return subscriptionService.createSubscription(subscriptionDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void cancelSubscription(@RequestParam @NotNull Long clientId,
                                   @RequestParam(required = false) Long authorId,
                                   @RequestParam(required = false) Long categoryId) {
        if ((authorId == null && categoryId == null) || (authorId != null && categoryId != null)) {
            throw new InvalidSubscriptionException("You must provide either authorId or categoryId");
        }
        subscriptionService.cancelSubscription(clientId, authorId, categoryId);
    }
}
