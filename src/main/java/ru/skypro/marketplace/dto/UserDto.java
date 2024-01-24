package ru.skypro.marketplace.dto;

import lombok.*;
import ru.skypro.marketplace.entity.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private String image;
}
