package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public class InMemoryGenreStorage implements GenreStorage {
    @Override
    public List<Genre> getAll() {
        return List.of();
    }

    @Override
    public Optional<Genre> getById(int id) {
        return Optional.empty();
    }

    @Override
    public Map<Integer, Set<Genre>> getGenresForFilms(List<Integer> id) {
        return Map.of();
    }

    @Override
    public void saveGenresForFilm(int filmId, Collection<Genre> genres) {

    }

    @Override
    public void deleteGenresFromFilm(int filmId) {

    }
}
