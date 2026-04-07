package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

public interface DirectorStorage {

    List<Director> getAll();

    Optional<Director> getById(long id);

    Director create(Director director);

    Director update(Director director);

    void delete(long id);

    Map<Integer, Set<Director>> getDirectorsForFilms(List<Integer> filmIds);

    void saveDirectorsForFilm(int filmId, Collection<Director> directors);

    void deleteDirectorsFromFilm(int filmId);
}