package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL = "SELECT * FROM films";
    private static final String FIND_BY_ID =  "SELECT * FROM films WHERE id = ?";
    private static final String INSERT = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    public FilmDbStorage(JdbcTemplate jdbc) {
        super(jdbc, new FilmRowMapper());
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(
                INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId((int) id);
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
        update(
                UPDATE,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return Optional.of(film);
    }
}