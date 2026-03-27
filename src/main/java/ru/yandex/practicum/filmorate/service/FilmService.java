package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.controller.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmMapper filmMapper;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            FilmMapper filmMapper
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.filmMapper = filmMapper;
    }

    public FilmRsDto addFilm(FilmRqDto filmRqDto) {
        Film film = filmMapper.map(filmRqDto);
        validateReferences(film);
        Film createdFilm = filmStorage.createFilm(film);
        log.info("Film added: id={}", createdFilm.getId());
        return filmMapper.mapToRsDto(createdFilm);
    }

    public FilmRsDto updateFilm(FilmRqDto filmRqDto) {
        Film film = filmMapper.map(filmRqDto);
        getFilmOrThrow(film.getId());
        validateReferences(film);
        Film updatedFilm = filmStorage.updateFilm(film)
                .orElseThrow(() -> new IllegalStateException("Film update failed"));
        log.info("Film updated: id={}", updatedFilm.getId());
        return filmMapper.mapToRsDto(updatedFilm);
    }

    public Collection<FilmRsDto> getAllFilms() {
        log.info("Get all films request");
        return filmStorage.getAllFilms().stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    public FilmRsDto getFilm(int id) {
        log.info("Get film request, id={}", id);
        return filmMapper.mapToRsDto(getFilmOrThrow(id));
    }

    public List<FilmRsDto> getPopular(int count) {
        return filmStorage.getPopularFilms(count).stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    public void addLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmStorage.addLike(filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    private Film getFilmOrThrow(int id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> {
                    log.warn("Film not found, id={}", id);
                    return new NotFoundException("Film not found");
                });
    }

    private void getUserOrThrow(int id) {
        userStorage.getUser(id)
                .orElseThrow(() -> {
                    log.warn("User not found, id={}", id);
                    return new NotFoundException("User not found");
                });
    }

    private void validateReferences(Film film) {
        validateGenres(film);
        validateMpa(film);
    }

    private void validateMpa(Film film) {
        if (film.getMpa() == null) {
            return;
        }
        int id = film.getMpa().getId();
        mpaStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("MPA not found: id={}", id);
                    return new NotFoundException("MPA not found");
                });
    }

    private void validateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            int id = genre.getId();
            genreStorage.getById(id)
                    .orElseThrow(() -> {
                        log.warn("Genre not found: id={}", id);
                        return new NotFoundException("Genre not found");
                    });
        }
    }
}
