package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.filmorate.controller.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto mapToDto(User user);

    @Mapping(target = "friends", ignore = true)
    User map(UserDto userDto);

    @AfterMapping
    default void applyDefaultName(@MappingTarget User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
