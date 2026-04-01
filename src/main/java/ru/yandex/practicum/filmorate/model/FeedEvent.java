package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class FeedEvent {
    private long timestamp;

    private int userId;

    private EventType eventType;

    private Operation operation;

    private int eventId;

    private int entityId;
}
