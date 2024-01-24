package ru.skypro.marketplace.service.mapper;

import org.mapstruct.Mapper;
import ru.skypro.marketplace.dto.UserDto;
import ru.skypro.marketplace.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToUserDTO(User user);

    User userDTOToUser(UserDto userDTO);
}
