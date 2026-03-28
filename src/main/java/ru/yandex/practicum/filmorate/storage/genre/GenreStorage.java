package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {

    List<Genre> getAll();

    Set<Genre> getAllById(Set<Integer> ids);

    Optional<Genre> getById(int id);

    Map<Integer, Set<Genre>> getGenresForFilms(List<Integer> id);

    void saveGenresForFilm(int filmId, Collection<Genre> genres);

    void deleteGenresFromFilm(int filmId);

}