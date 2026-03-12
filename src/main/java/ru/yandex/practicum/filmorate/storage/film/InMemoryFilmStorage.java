package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.*;

public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film createFilm(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            return Optional.empty();
        }
        films.put(film.getId(), film);
        return Optional.of(film);
    }

    @Override
    public Optional<Film> getFilm(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAllFilms() {
        return films.values().stream().toList();
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> result = new ArrayList<>(films.values());
        result.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));
        if (result.size() > count) {
            return result.subList(0, count);
        }
        return result;
    }

    @Override
    public void addLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikes().add(userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikes().remove(userId);
        }
    }
}