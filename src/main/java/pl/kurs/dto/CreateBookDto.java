package pl.kurs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateBookDto {
    @NotNull(message = "Author ID is required")
    @Positive(message = "Author ID must be greater than 0")
    private Long authorId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be greater than 0")
    private Long categoryId;

    @Positive(message = "Page count must be greater than 0")
    private Integer pageCount;
}
