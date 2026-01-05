package pl.kurs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "authors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @OneToMany(mappedBy = "author")
    private Set<Book> books;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Author author)) return true;
        return getId() != null && getId().equals(author.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
