package ru.yandex.practicum.filmorate.controller.dto.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.controller.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewDto mapToDto(Review review);

    Review map(ReviewDto reviewDto);
}
