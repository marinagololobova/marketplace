package ru.skypro.marketplace.dto;


import lombok.*;
import ru.skypro.marketplace.entity.Role;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Register {

    @Min(4)
    @Max(32)
    private String username;
    @Min(8)
    @Max(16)
    private String password;
    @Min(2)
    @Max(16)
    private String firstName;
    @Min(2)
    @Max(16)
    private String lastName;
    private String phone;
    private Role role;
}
