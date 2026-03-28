package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.*;

@Repository("genreDbStorage")
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

    private static final String INSERT_GENRE = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_ALL = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_ALL_BY_ID = "SELECT * FROM genres WHERE id IN (:ids) ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String DELETE_GENRE = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String FIND_BY_FILM_IDS =
            "SELECT fg.film_id, g.* " +
            "FROM film_genres fg " +
            "JOIN genres g ON g.id = fg.genre_id " +
            "WHERE fg.film_id IN (:ids) " +
            "ORDER BY g.id";


    private final NamedParameterJdbcTemplate namedJdbc;

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper, NamedParameterJdbcTemplate namedJdbc) {
        super(jdbc, mapper);
        this.namedJdbc = namedJdbc;
    }

    @Override
    public List<Genre> getAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public Set<Genre> getAllById(Set<Integer> ids) {
        if (ids.isEmpty()) {
            return Collections.emptySet();
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", ids);
        List<Genre> genres = namedJdbc.query(FIND_ALL_BY_ID, params, mapper);
        return new LinkedHashSet<>(genres);
    }

    @Override
    public Optional<Genre> getById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Map<Integer, Set<Genre>> getGenresForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", filmIds);
        Map<Integer, Set<Genre>> result = new HashMap<>();
        namedJdbc.query(FIND_BY_FILM_IDS, params, rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = mapper.mapRow(rs, 0);
            result.computeIfAbsent(filmId, id -> new LinkedHashSet<>())
                    .add(genre);
        });
        return result;
    }

    @Override
    public void saveGenresForFilm(int filmId, Collection<Genre> genres) {
        jdbc.update(DELETE_GENRE, filmId);
        if (genres == null || genres.isEmpty()) {
            return;
        }
        Set<Integer> uniqueGenres = new LinkedHashSet<>();
        for (Genre genre : genres) {
            uniqueGenres.add(genre.getId());
        }
        for (Integer genreId : uniqueGenres) {
            jdbc.update(INSERT_GENRE, filmId, genreId);
        }
    }

    @Override
    public void deleteGenresFromFilm(int filmId) {
        jdbc.update(DELETE_GENRE, filmId);
    }
}