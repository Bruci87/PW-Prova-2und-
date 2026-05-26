package com.ufrn.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookFormDTO {

    private Long id;

    @NotBlank(message = "O título do livro é obrigatório e não pode conter apenas espaços.")
    private String title;

    @NotBlank(message = "O autor do livro é obrigatório.")
    private String author;

    @NotBlank(message = "O ISBN é obrigatório.")
    @Pattern(
            // Regex corrigida: Aceita formatos padrão de ISBN-10 e ISBN-13 (com ou sem hífen)
            regexp = "^(?:\\d{9}[\\dX]|\\d{13}|(?:\\d{1,5}-\\d{1,7}-\\d{1,6}-[\\dX])|(?:\\d{3}-\\d{1,5}-\\d{1,7}-\\d{1,6}-\\d))$",
            message = "O formato do ISBN deve ser válido (Exemplo: 978-3-16-148410-0)."
    )
    private String isbn;

    @NotNull(message = "O preço é obrigatório.")
    @Positive(message = "O preço deve ser um valor maior que zero.")
    private Double price;

    @NotBlank(message = "A categoria é obrigatória.")
    private String category;
}