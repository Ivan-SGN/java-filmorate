package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.*;

@Repository
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {

    private static final String FIND_ALL = "SELECT * FROM directors ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM directors WHERE id = ?";

    private static final String INSERT_FILM_DIRECTOR =
            "INSERT INTO film_directors(film_id, director_id) VALUES (?, ?)";

    private static final String DELETE_FILM_DIRECTORS =
            "DELETE FROM film_directors WHERE film_id = ?";

    private static final String FIND_BY_FILM_IDS =
            "SELECT fd.film_id, d.* " +
                    "FROM film_directors fd " +
                    "JOIN directors d ON d.id = fd.director_id " +
                    "WHERE fd.film_id IN (:ids)";

    private final NamedParameterJdbcTemplate namedJdbc;

    public DirectorDbStorage(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        super(jdbc, (rs, rowNum) -> {
            Director d = new Director();
            d.setId(rs.getInt("id"));
            d.setName(rs.getString("name"));
            return d;
        });
        this.namedJdbc = namedJdbc;
    }

    @Override
    public List<Director> getAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public Optional<Director> getById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Director create(Director director) {
        long id = insert(INSERT, director.getName());
        director.setId((int) id);
        return director;
    }

    @Override
    public Director update(Director director) {
        Optional<Director> existing = getById(director.getId());

        if (existing.isEmpty()) {
            throw new NotFoundException("Director not found");
        }


        update(UPDATE, director.getName(), director.getId());
        return director;
    }



    @Override
    public void delete(int id) {
        delete(DELETE, id);
    }

    @Override
    public Map<Integer, Set<Director>> getDirectorsForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) return Map.of();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", filmIds);

        Map<Integer, Set<Director>> result = new HashMap<>();

        namedJdbc.query(FIND_BY_FILM_IDS, params, rs -> {
            int filmId = rs.getInt("film_id");

            Director d = new Director();
            d.setId(rs.getInt("id"));
            d.setName(rs.getString("name"));

            result.computeIfAbsent(filmId, id -> new HashSet<>()).add(d);
        });

        return result;
    }

    @Override
    public void saveDirectorsForFilm(int filmId, Collection<Director> directors) {
        jdbc.update(DELETE_FILM_DIRECTORS, filmId);

        if (directors == null) return;

        for (Director d : directors) {
            jdbc.update(INSERT_FILM_DIRECTOR, filmId, d.getId());
        }
    }

    @Override
    public void deleteDirectorsFromFilm(int filmId) {
        jdbc.update(DELETE_FILM_DIRECTORS, filmId);
    }
}