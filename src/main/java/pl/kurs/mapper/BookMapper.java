package pl.kurs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kurs.dto.BookDto;
import pl.kurs.dto.CreateBookDto;
import pl.kurs.entity.Author;
import pl.kurs.entity.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "author", ignore = true)
    Book dtoToEntity(CreateBookDto dto);


    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "author", target = "authorFullName")
    BookDto entityToDto(Book entity);

    default String map(Author author) {
        if (author == null) {
            return null;
        }
        return author.getFirstName() + " " + author.getLastName();
    }

}
