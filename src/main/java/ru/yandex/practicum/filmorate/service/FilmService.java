package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class FilmService {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    public Film addFilm(Film film) {
        film.setId(nextId);
        films.put(nextId, film);
        nextId++;
        return film;
    }

    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    public Collection<Film> getAllFilms() {
        return films.values();
    }
}
