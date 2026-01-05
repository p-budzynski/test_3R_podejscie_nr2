package pl.kurs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kurs.dto.BookDto;
import pl.kurs.dto.CreateBookDto;
import pl.kurs.entity.Author;
import pl.kurs.entity.Book;
import pl.kurs.entity.Category;
import pl.kurs.exception.ResourceNotFoundException;
import pl.kurs.mapper.BookMapper;
import pl.kurs.repository.BookRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepositoryMock;

    @Mock
    private CategoryService categoryServiceMock;

    @Mock
    private AuthorService authorServiceMock;

    @Mock
    private BookMapper bookMapperMock;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldCreateBook() {
        //given
        Author author = new Author(1L, "Test firstName", "Test lastName", null);
        Category category = new Category(1L, "Test category");
        CreateBookDto createBookDto = new CreateBookDto(author.getId(), "Test title", category.getId(), 300);
        BookDto expectedDto = new BookDto(1L, createBookDto.getTitle(), category.getName(), author.getFirstName() + " " + author.getLastName(), 300);
        Book book = new Book(null, "Test title", category, 300, author, null);
        Book savedBook = new Book(1L, "Test title", category, 300, author, null);

        when(categoryServiceMock.findCategoryById(1L)).thenReturn(category);
        when(authorServiceMock.findAuthorById(1L)).thenReturn(author);
        when(bookMapperMock.dtoToEntity(createBookDto)).thenReturn(book);
        when(bookRepositoryMock.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapperMock.entityToDto(savedBook)).thenReturn(expectedDto);

        //when
        BookDto result = bookService.createBook(createBookDto);

        //then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDto);
    }

}
