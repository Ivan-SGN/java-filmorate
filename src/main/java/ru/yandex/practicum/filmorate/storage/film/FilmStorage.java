package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film createFilm(Film film);

    Optional<Film> getFilm(int filmId);

    List<Film> getAllFilms();

    Optional<Film> updateFilm(Film film);

    void deleteFilm (int filmId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopularFilms(int count);
}
