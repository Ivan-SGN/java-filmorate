package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewDto addReview(@Valid @RequestBody ReviewDto reviewDto) {
        return reviewService.addReview(reviewDto);
    }

    @PutMapping
    public ReviewDto updateReview(@Valid @RequestBody ReviewDto reviewDto) {
        validateUpdateId(reviewDto.getReviewId());
        return reviewService.updateReview(reviewDto);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable @Positive int id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable @Positive int id) {
        return reviewService.getReview(id);
    }

    @GetMapping
    public List<ReviewDto> getReviews(
            @RequestParam(required = false) @Positive Integer filmId,
            @RequestParam(defaultValue = "10") @Positive int count
    ) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        reviewService.removeDislike(id, userId);
    }

    private void validateUpdateId(Long reviewId) {
        if (reviewId == null || reviewId <= 0) {
            throw new ValidationException("Review id must be positive");
        }
    }
}
