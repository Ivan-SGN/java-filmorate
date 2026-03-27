package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.controller.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto mapToDto(User user);

    @Mapping(target = "friends", ignore = true)
    User map(UserDto userDto);
}
