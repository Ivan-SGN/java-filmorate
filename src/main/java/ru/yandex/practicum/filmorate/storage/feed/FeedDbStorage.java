package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.FeedEventRowMapper;

import java.util.List;

@Repository("feedDbStorage")
public class FeedDbStorage extends BaseRepository<FeedEvent> implements FeedStorage {

    private static final String INSERT =
            "INSERT INTO feed(timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_USER =
            "SELECT * FROM feed WHERE user_id = ? ORDER BY timestamp ASC, event_id ASC";

    public FeedDbStorage(JdbcTemplate jdbc, FeedEventRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addEvent(int userId, EventType eventType, Operation operation, int entityId) {
        insert(INSERT, System.currentTimeMillis(), userId, eventType.name(), operation.name(), entityId);
    }

    @Override
    public List<FeedEvent> getFeed(int userId) {
        return findMany(FIND_BY_USER, userId);
    }
}
