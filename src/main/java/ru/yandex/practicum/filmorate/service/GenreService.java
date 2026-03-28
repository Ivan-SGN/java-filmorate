package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.GenreDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@Slf4j
@Service
public class GenreService {

    private final GenreStorage genreStorage;
    private final GenreMapper genreMapper;

    public GenreService(@Qualifier("genreDbStorage") GenreStorage genreStorage, GenreMapper genreMapper) {
        this.genreStorage = genreStorage;
        this.genreMapper = genreMapper;
    }

    public Collection<GenreDto> getAllGenres() {
        log.info("Get all genres request");
        return genreStorage.getAll().stream()
                .map(genreMapper::mapToDto)
                .toList();
    }

    public GenreDto getGenre(int id) {
        log.info("Get genre request, id: {}", id);
        return genreMapper.mapToDto(getGenreOrThrow(id));
    }

    private Genre getGenreOrThrow(int id) {
        return genreStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Genre not found, id={}", id);
                    return new NotFoundException("Genre not found");
                });
    }
}
