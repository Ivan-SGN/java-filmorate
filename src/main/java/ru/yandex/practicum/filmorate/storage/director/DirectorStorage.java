package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

public interface DirectorStorage {

    Director create(Director director);

    Optional<Director> getById(int id);

    List<Director> getAll();

    Optional<Director> update(Director director);

    Map<Integer, Set<Director>> getDirectorsForFilms(List<Integer> filmIds);

    void delete(int id);

    void saveDirectorsForFilm(int filmId, Collection<Director> directors);

    void deleteDirectorsFromFilm(int filmId);
}