package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.controller.dto.GenreDto;
import ru.yandex.practicum.filmorate.controller.dto.IdDto;
import ru.yandex.practicum.filmorate.model.Genre;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreDto mapToDto(Genre genre);

    Genre map(GenreDto genreDto);

    @Mapping(target = "name", ignore = true)
    Genre map(IdDto idDto);
}
