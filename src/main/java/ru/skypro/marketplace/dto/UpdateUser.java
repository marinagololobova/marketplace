package ru.skypro.marketplace.dto;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateUser {
    @Min(3)
    @Max(10)
    private String firstName;
    @Min(3)
    @Max(10)
    private String lastName;
    private String phone;
}
