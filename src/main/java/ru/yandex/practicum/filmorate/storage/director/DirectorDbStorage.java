package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {

    private static final String FIND_ALL = "SELECT * FROM directors ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM directors WHERE id = ?";
    private static final String UPDATE = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String INSERT_DIRECTOR = "INSERT INTO directors(name) VALUES (?)";

    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE id = ?";

    private static final String INSERT_FILM_DIRECTOR ="INSERT INTO film_directors(film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS ="DELETE FROM film_directors WHERE film_id = ?";

    private static final String FIND_BY_FILM_IDS =
            "SELECT fd.film_id, d.* " +
                    "FROM film_directors fd " +
                    "JOIN directors d ON d.id = fd.director_id " +
                    "WHERE fd.film_id IN (:ids)";

    private final NamedParameterJdbcTemplate namedJdbc;


    public DirectorDbStorage(JdbcTemplate jdbc, DirectorRowMapper mapper, NamedParameterJdbcTemplate namedJdbc) {
        super(jdbc, mapper);
        this.namedJdbc = namedJdbc;
    }

    @Override
    public Director create(Director director) {
        long id = insert(INSERT_DIRECTOR, director.getName());
        director.setId((int) id);
        return director;
    }

    @Override
    public Optional<Director> getById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public List<Director> getAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public Optional<Director> update(Director director) {
        update(UPDATE, director.getName(), director.getId());
        return Optional.of(director);
    }

    @Override
    public void delete(int id) {
        delete(DELETE_DIRECTOR, id);
    }

    @Override
    public Map<Integer, Set<Director>> getDirectorsForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", filmIds);

        Map<Integer, Set<Director>> result = new HashMap<>();

        namedJdbc.query(FIND_BY_FILM_IDS, params, rs -> {
            int filmId = rs.getInt("film_id");

            Director director = mapper.mapRow(rs, 0);

            result.computeIfAbsent(filmId, id -> new LinkedHashSet<>())
                    .add(director);
        });

        return result;
    }

    @Override
    public void saveDirectorsForFilm(int filmId, Collection<Director> directors) {
        for (Director director : directors) {

            // 1. Проверяем существование
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM directors WHERE id = ?",
                    Integer.class,
                    director.getId()
            );

            // 2. Если нет — создаём
            if (count == null || count == 0) {
                String insertDirector = "INSERT INTO directors (id, name) VALUES (?, ?)";
                jdbc.update(insertDirector, director.getId(), director.getName());
            }

            // 3. Создаём связь
            jdbc.update(
                    "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                    filmId,
                    director.getId()
            );
        }
    }

    @Override
    public void deleteDirectorsFromFilm(int filmId) {
        jdbc.update(DELETE_FILM_DIRECTORS, filmId);
    }
}