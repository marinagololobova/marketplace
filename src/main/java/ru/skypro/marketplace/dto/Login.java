package ru.skypro.marketplace.dto;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Login {

    @Min(8)
    @Max(16)
    private String username;
    @Min(4)
    @Max(32)
    private String password;
}
