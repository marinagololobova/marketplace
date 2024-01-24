package ru.skypro.marketplace.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateOrUpdateComment {

    @NotEmpty(message = "Комментарий не может быть пустым")
    @Size(min = 8, max = 64, message = "Комментарий должен содержать от 8 до 64 символов")
    private String text;
}
