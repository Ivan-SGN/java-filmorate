package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL = "SELECT * FROM films";
    private static final String FIND_BY_ID = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String INSERT_GENRE = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String INSERT_LIKE = "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_POPULAR =
            "SELECT f.* FROM films f LEFT JOIN film_likes l ON f.id = l.film_id GROUP BY f.id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
    private static final String GET_GENRES_BY_FILM =
            "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";

    public FilmDbStorage(JdbcTemplate jdbc) {
        super(jdbc, new FilmRowMapper());
    }

    @Override
    public Film createFilm(Film film) {
        Integer mpaId = film.getMpa() != null ? film.getMpa().getId() : null;
        long id = insert(
                INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId
        );
        film.setId((int) id);
        saveGenres(film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(int id) {
        Optional<Film> film = findOne(FIND_BY_ID, id);
        film.ifPresent(f -> f.setGenres(loadGenres(f.getId())));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL);
        for (Film film : films) {
            film.setGenres(loadGenres(film.getId()));
        }
        return films;
    }

    @Override
    public Optional<Film> updateFilm(Film film) {
        Integer mpaId = film.getMpa() != null ? film.getMpa().getId() : null;
        update(
                UPDATE,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId()
        );
        jdbc.update(DELETE_GENRES, film.getId());
        saveGenres(film);
        return Optional.of(film);
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbc.update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbc.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = findMany(GET_POPULAR, count);
        for (Film film : films) {
            film.setGenres(loadGenres(film.getId()));
        }
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) return;
        Set<Integer> uniqueGenres = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            uniqueGenres.add(genre.getId());
        }
        for (Integer genreId : uniqueGenres) {
            jdbc.update(INSERT_GENRE, film.getId(), genreId);
        }
    }

    private Set<Genre> loadGenres(int filmId) {
        return new LinkedHashSet<>(jdbc.query(GET_GENRES_BY_FILM, new GenreRowMapper(), filmId));
    }
}