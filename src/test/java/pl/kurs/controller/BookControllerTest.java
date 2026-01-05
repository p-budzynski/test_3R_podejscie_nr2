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
import pl.kurs.dto.CreateBookDto;
import pl.kurs.entity.Author;
import pl.kurs.entity.Category;
import pl.kurs.repository.AuthorRepository;
import pl.kurs.repository.BookRepository;
import pl.kurs.repository.CategoryRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void before() {
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void shouldCreateBook() throws Exception {
        //given
        Author author = new Author(null, "firstName", "lastName", null);
        Author savedAuthor = authorRepository.save(author);
        Category category = new Category(null, "categoryTest");
        Category savedCategory = categoryRepository.save(category);

        CreateBookDto dto = new CreateBookDto();
        dto.setAuthorId(savedAuthor.getId());
        dto.setTitle("titleTest");
        dto.setCategoryId(savedCategory.getId());
        dto.setPageCount(100);

        //when then
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.authorFullName").value(savedAuthor.getFirstName() + " " + savedAuthor.getLastName()))
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.categoryName").value(savedCategory.getName()))
                .andExpect(jsonPath("$.pageCount").value(dto.getPageCount()));
    }
}
