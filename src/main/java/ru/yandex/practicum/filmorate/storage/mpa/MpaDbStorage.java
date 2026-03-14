package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository("mpaDbStorage")
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {

    private static final String FIND_ALL = "SELECT * FROM mpa ORDER BY id";

    private static final String FIND_BY_ID = "SELECT * FROM mpa WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Mpa> getAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public Optional<Mpa> getById(int id) {
        return findOne(FIND_BY_ID, id);
    }
}