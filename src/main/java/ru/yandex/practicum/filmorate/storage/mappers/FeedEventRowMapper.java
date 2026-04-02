package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedEventRowMapper implements RowMapper<FeedEvent> {

    @Override
    public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeedEvent event = new FeedEvent();
        event.setEventId(rs.getInt("event_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getInt("user_id"));
        event.setEventType(EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getInt("entity_id"));
        return event;
    }
}
