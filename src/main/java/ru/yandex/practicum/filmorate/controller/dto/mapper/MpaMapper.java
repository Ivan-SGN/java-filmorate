package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.controller.dto.IdDto;
import ru.yandex.practicum.filmorate.controller.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

@Mapper(componentModel = "spring")
public interface MpaMapper {

    MpaDto mapToDto(Mpa mpa);

    Mpa map(MpaDto mpaDto);

    @Mapping(target = "name", ignore = true)
    Mpa map(IdDto idDto);
}
