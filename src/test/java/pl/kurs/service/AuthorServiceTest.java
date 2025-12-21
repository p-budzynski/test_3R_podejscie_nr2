package pl.kurs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.entity.Author;
import pl.kurs.exception.ResourceNotFoundException;
import pl.kurs.repository.AuthorRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepositoryMock;

    @InjectMocks
    private AuthorService authorService;

    @Test
    void shouldReturnAuthorById() {
        //given
        Long authorId = 1L;
        Author author = new Author(authorId, "Test firstName", "Test lastName", null);

        when(authorRepositoryMock.findById(authorId)).thenReturn(Optional.of(author));

        //when
        Author result = authorService.findAuthorById(authorId);

        //then
        assertThat(result).isEqualTo(author);
    }

    @Test
    void shouldThrowExceptionWhenAuthorNotFound() {
        //given
        Long authorId = 1L;
        when(authorRepositoryMock.findById(authorId))
                .thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> authorService.findAuthorById(authorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Author with id: " + authorId + " not found");
    }
}
