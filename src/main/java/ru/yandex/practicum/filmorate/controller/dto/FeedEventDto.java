package ru.yandex.practicum.filmorate.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

@Data
@Accessors(chain = true)
public class FeedEventDto {
    private Long timestamp;

    private Integer userId;

    private EventType eventType;

    private Operation operation;

    private Integer eventId;

    private Integer entityId;
}
