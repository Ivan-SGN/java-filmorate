package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.controller.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.model.FeedEvent;

@Mapper(componentModel = "spring")
public interface FeedEventMapper {

    FeedEventDto mapToDto(FeedEvent feedEvent);
}
