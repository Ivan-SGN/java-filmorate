package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FilmService {

    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    public Film addFilm(Film film) {
        validateReleaseDate(film);
        film.setId(nextId);
        films.put(nextId, film);
        log.info("Film added: id={}, name={}", film.getId(), film.getName());
        nextId++;
        return film;
    }

    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film not found");
        }
        validateReleaseDate(film);
        films.put(film.getId(), film);
        log.info("Film updated: id={}, name={}", film.getId(), film.getName());

        return film;
    }

    public Collection<Film> getAllFilms() {
        return films.values();
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Release date is before " + MIN_RELEASE_DATE);
        }
    }
}