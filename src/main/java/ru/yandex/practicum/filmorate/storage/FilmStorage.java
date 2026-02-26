package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film createFilm(Film film);

    Optional<Film> getFilm(int id);

    List<Film> getAllFilms();

    Film updateFilm(Film film);
}
