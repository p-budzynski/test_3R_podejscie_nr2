package pl.kurs.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.kurs.dto.BookDto;
import pl.kurs.dto.CreateBookDto;
import pl.kurs.entity.Author;
import pl.kurs.entity.Book;
import pl.kurs.entity.Category;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class BookMapperTest {
    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    @Test
    void shouldMapEntityToDto() {
        //given
        Book testBook = createTestBook();
        BookDto testBookDto = new BookDto(testBook.getId(), testBook.getTitle(),
                testBook.getCategory().getName(),
                testBook.getAuthor().getFirstName() + " " + testBook.getAuthor().getLastName(),
                testBook.getPageCount());

        //when
        BookDto dto = bookMapper.entityToDto(testBook);

        //then
        assertThat(dto)
                .usingRecursiveComparison()
                .isEqualTo(testBookDto);
    }

    @Test
    void shouldMapDtoToEntity() {
        //given
        Book testBook = createTestBook();
        CreateBookDto testBookDto = createTestBookDto();

        //when
        Book entity = bookMapper.dtoToEntity(testBookDto);

        //then
        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("id", "category", "author")
                .isEqualTo(testBook);
    }

    @Test
    void shouldReturnNullWhenEntityToDtoGivenNull() {
        //when then
        assertThat(bookMapper.entityToDto(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenDtoToEntityGivenNull() {
        //when then
        assertThat(bookMapper.dtoToEntity(null)).isNull();
    }

    @Test
    void shouldReturnNullCategoryIdsWhenFieldsAreNull() {
        //given
        Book testBook = createTestBook();
        testBook.setCategory(null);

        //when
        BookDto dto = bookMapper.entityToDto(testBook);

        //then
        assertThat(dto.getTitle()).isEqualTo(testBook.getTitle());
        assertThat(dto.getCategoryName()).isNull();
        assertThat(dto.getPageCount()).isEqualTo(testBook.getPageCount());
    }

    @Test
    void shouldReturnNullAuthorIdsWhenFieldsAreNull() {
        //given
        Book testBook = createTestBook();
        testBook.setAuthor(null);

        //when
        BookDto dto = bookMapper.entityToDto(testBook);

        //then
        assertThat(dto.getAuthorFullName()).isNull();
        assertThat(dto.getTitle()).isEqualTo(testBook.getTitle());
        assertThat(dto.getCategoryName()).isEqualTo(testBook.getCategory().getName());
        assertThat(dto.getPageCount()).isEqualTo(testBook.getPageCount());
    }

    private Book createTestBook() {
        return new Book(1L,"Test title",
                new Category(1L,"Test category"),100,
                new Author(1L, "Test firstName", "Test lastName",null), null);
    }

    private CreateBookDto createTestBookDto() {
        return new CreateBookDto(1L,"Test title",1L,100);
    }
}
