package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL = "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id";
    private static final String FIND_BY_ID = "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String INSERT =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String INSERT_LIKE = "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_POPULAR =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
            "LEFT JOIN mpa m ON f.mpa_id = m.id " +
            "LEFT JOIN (" +
                "SELECT film_id, COUNT(*) AS likes_count FROM film_likes GROUP BY film_id" +
            ") l ON f.id = l.film_id " +
            "ORDER BY COALESCE(l.likes_count, 0) DESC, f.id " +
            "LIMIT ?";

    private static final String GET_GENRES_BY_FILM =
            "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";

    private final GenreStorage genreStorage;

    public FilmDbStorage(JdbcTemplate jdbc, GenreStorage genreStorage) {
        super(jdbc, new FilmRowMapper());
        this.genreStorage = genreStorage;
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
        genreStorage.saveGenresForFilm(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Optional<Film> getFilm(int id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public List<Film> getAllFilms() {
        return findMany(FIND_ALL);
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
        genreStorage.deleteGenresFromFilm(film.getId());
        genreStorage.saveGenresForFilm(film.getId(), film.getGenres());
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
        return findMany(GET_POPULAR, count);
    }
}