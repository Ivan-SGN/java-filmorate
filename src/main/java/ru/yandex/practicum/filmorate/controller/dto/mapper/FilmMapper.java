package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.controller.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.controller.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.model.Film;

@Mapper(componentModel = "spring", uses = {MpaMapper.class, GenreMapper.class})
public interface FilmMapper {

    FilmRsDto mapToRsDto(Film film);

    @Mapping(target = "likes", ignore = true)
    Film map(FilmRqDto filmRqDto);
}
