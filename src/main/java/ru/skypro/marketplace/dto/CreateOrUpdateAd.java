package ru.skypro.marketplace.dto;

import lombok.*;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateOrUpdateAd {
    @NotEmpty(message = "Заголовок объявления не может быть пустым")
    @Size(min = 4, max = 32, message = "Заголовок объявления должен содержать от 4 до 32 символов")
    private String title;

    @NotNull(message = "Цена объявления не может быть пустой")
    @Min(value = 0, message = "Цена объявления не может быть меньше 0")
    @Max(value = 10000000, message = "Цена объявления не может быть больше 10000000")
    private Integer price;

    @NotEmpty(message = "Описание объявления не может быть пустым")
    @Size(min = 8, max = 64, message = "Описание объявления должно содержать от 8 до 64 символов")
    private String description;
}
