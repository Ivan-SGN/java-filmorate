package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@Slf4j
@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getAllGenres() {
        log.info("Get all genres request");
        return genreStorage.getAll();
    }

    public Genre getGenre(int id) {
        log.info("Get genre request, id: {}", id);
        return getGenreOrThrow(id);
    }

    private Genre getGenreOrThrow(int id) {
        return genreStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Genre not found, id={}", id);
                    return new NotFoundException("Genre not found");
                });
    }
}