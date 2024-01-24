package ru.skypro.marketplace.dto;

import lombok.*;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewPassword {
    @Size(min = 8, max = 16)
    private String currentPassword;

    @Size(min = 8, max = 16, message = "Длина пароля должна быть 8-16 символов")
    private String newPassword;
}
