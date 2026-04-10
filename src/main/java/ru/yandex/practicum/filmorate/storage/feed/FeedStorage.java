package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

public interface FeedStorage {

    void addEvent(int userId, EventType eventType, Operation operation, int entityId);

    List<FeedEvent> getFeed(int userId);
}
