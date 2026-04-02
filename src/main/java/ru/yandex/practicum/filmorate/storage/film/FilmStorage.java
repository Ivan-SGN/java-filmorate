package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.Year;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film createFilm(Film film);

    Optional<Film> getFilm(int filmId);

    List<Film> getAllFilms();

    List<Film> getCommonFilms(int userId, int friendId);

    Optional<Film> updateFilm(Film film);

    void deleteFilm(int filmId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    boolean hasLike(int filmId, int userId);

    List<Film> getPopularFilms(int count, Integer genreId, Year year);
}
