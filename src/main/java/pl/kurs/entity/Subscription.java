package pl.kurs.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.kurs.exception.InvalidSubscriptionException;

@Entity
@Table(name = "subscriptions",
        indexes = {
                @Index(name = "idx_sub_author", columnList = "author_fk"),
                @Index(name = "idx_sub_category", columnList = "category_fk"),
                @Index(name = "idx_sub_client_author", columnList = "client_fk, author_fk"),
                @Index(name = "idx_sub_client_category", columnList = "client_fk, category_fk")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_fk", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_fk")
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_fk")
    private Category category;

    @PrePersist
    @PreUpdate
    private void validate() {
        if ((author == null && category == null) || (author != null && category != null)) {
            throw new InvalidSubscriptionException("Subscription must have either author OR category, but not both.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Subscription subscription)) return false;
        return getId() != null && getId().equals(subscription.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
