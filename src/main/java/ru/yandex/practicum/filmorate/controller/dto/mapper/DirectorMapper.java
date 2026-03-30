package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.controller.dto.DirectorDto;
import ru.yandex.practicum.filmorate.controller.dto.IdDto;
import ru.yandex.practicum.filmorate.model.Director;

@Mapper(componentModel = "spring")
public interface DirectorMapper {

    DirectorDto mapToDto(Director director);

    Director map(DirectorDto directorDto);

    @Mapping(target = "name", ignore = true)
    Director map(IdDto idDto);
}