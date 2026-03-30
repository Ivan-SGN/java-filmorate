package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.Year;
import java.util.*;

@Transactional
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL = "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id";
    private static final String FIND_BY_ID = "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String INSERT =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM films WHERE id = ?";

    private static final String INSERT_LIKE = "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_COMMON =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
                    "JOIN film_likes fl1 ON f.id = fl1.film_id AND fl1.user_id = ? " +
                    "JOIN film_likes fl2 ON f.id = fl2.film_id AND fl2.user_id = ? " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "GROUP BY f.id, m.name " +
                    "ORDER BY COUNT(fl.user_id) DESC";

    private static final String GET_POPULAR =
            "SELECT f.*, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN (" +
                    "SELECT film_id, COUNT(*) AS likes_count " +
                    "FROM film_likes GROUP BY film_id" +
                    ") l ON f.id = l.film_id ";

    private static final String GET_FILMS_BY_DIRECTOR =
            "SELECT f.*, m.name AS mpa_name " +
                    "FROM films f " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id, m.name ";

    private final GenreStorage genreStorage;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final DirectorStorage directorStorage;

    public FilmDbStorage(JdbcTemplate jdbc, GenreStorage genreStorage, NamedParameterJdbcTemplate namedJdbc, DirectorStorage directorStorage) {
        super(jdbc, new FilmRowMapper());
        this.genreStorage = genreStorage;
        this.namedJdbc = namedJdbc;
        this.directorStorage = directorStorage;
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
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.saveDirectorsForFilm(film.getId(), film.getDirectors());
        }        return film;
    }

    @Override
    public Optional<Film> getFilm(int filmId) {
        Optional<Film> film = findOne(FIND_BY_ID, filmId);
        film.ifPresent(f -> enrichFilmsWithGenres(List.of(f)));
        film.ifPresent(f -> enrichFilmsWithDirectors(List.of(f)));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL);
        if (films.isEmpty()) {
            return films;
        }
        enrichFilmsWithGenres(films);
        enrichFilmsWithDirectors(films);
        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> films = findMany(GET_COMMON, userId, friendId);
        if (films.isEmpty()) {
            return films;
        }
        enrichFilmsWithGenres(films);
        enrichFilmsWithDirectors(films);
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

        // жанры
        genreStorage.deleteGenresFromFilm(film.getId());
        genreStorage.saveGenresForFilm(film.getId(), film.getGenres());

        directorStorage.deleteDirectorsFromFilm(film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.saveDirectorsForFilm(film.getId(), film.getDirectors());
        }

        return Optional.of(film);
    }

    @Override
    public void deleteFilm(int filmId) {
        jdbc.update(DELETE, filmId);
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
    public List<Film> getPopularFilms(int count, Integer genreId, Year year) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("count", count);

        String query = buildPopularFilmsQuery(genreId, year, params);

        List<Film> films = namedJdbc.query(query, params, mapper);
        if (films.isEmpty()) {
            return films;
        }

        enrichFilmsWithGenres(films);
        enrichFilmsWithDirectors(films);
        return films;
    }

    private void enrichFilmsWithGenres(List<Film> films) {
        List<Integer> ids = new ArrayList<>();
        for (Film film : films) {
            ids.add(film.getId());
        }

        Map<Integer, Set<Genre>> genres = genreStorage.getGenresForFilms(ids);

        for (Film film : films) {
            film.setGenres(genres.getOrDefault(film.getId(), Set.of()));
        }
    }

    private String buildPopularFilmsQuery(Integer genreId, Year year, MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder(GET_POPULAR);
        List<String> conditions = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genres fg ON f.id = fg.film_id AND fg.genre_id = :genreId ");
            params.addValue("genreId", genreId);
        }

        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = :year");
            params.addValue("year", year.getValue());
        }

        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }

        sql.append("ORDER BY COALESCE(l.likes_count, 0) DESC, f.id ");
        sql.append("LIMIT :count");

        return sql.toString();
    }

    private void enrichFilmsWithDirectors(List<Film> films) {
        List<Integer> ids = films.stream()
                .map(Film::getId)
                .toList();

        Map<Integer, Set<Director>> directors = directorStorage.getDirectorsForFilms(ids);

        for (Film film : films) {
            film.setDirectors(directors.getOrDefault(film.getId(), Set.of()));
        }
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        String orderBy;

        if ("year".equalsIgnoreCase(sortBy)) {
            orderBy = "ORDER BY f.release_date";
        } else {
            orderBy = "ORDER BY COUNT(fl.user_id) DESC";
        }

        List<Film> films = findMany(GET_FILMS_BY_DIRECTOR + orderBy, directorId);

        if (films.isEmpty()) {
            return films;
        }

        enrichFilmsWithGenres(films);
        enrichFilmsWithDirectors(films);

        return films;
    }
}
