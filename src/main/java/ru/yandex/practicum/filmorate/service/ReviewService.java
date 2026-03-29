package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.dto.ReviewDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final ReviewMapper reviewMapper;

    public ReviewService(
            @Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            ReviewMapper reviewMapper
    ) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.reviewMapper = reviewMapper;
    }

    @Transactional
    public ReviewDto addReview(ReviewDto reviewDto) {
        Review review = reviewMapper.map(reviewDto);
        review.setUseful(0);
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        Review createdReview = reviewStorage.createReview(review);
        log.info("Review added: id={}", createdReview.getReviewId());
        return reviewMapper.mapToDto(createdReview);
    }

    @Transactional
    public ReviewDto updateReview(ReviewDto reviewDto) {
        Review existingReview = getReviewOrThrow(reviewDto.getReviewId().intValue());
        Review review = reviewMapper.map(reviewDto);
        review.setUseful(existingReview.getUseful());
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        Review updatedReview = reviewStorage.updateReview(review)
                .orElseThrow(() -> new IllegalStateException("Review update failed"));
        log.info("Review updated: id={}", updatedReview.getReviewId());
        return reviewMapper.mapToDto(updatedReview);
    }

    @Transactional
    public void deleteReview(int reviewId) {
        getReviewOrThrow(reviewId);
        reviewStorage.deleteReview(reviewId);
        log.info("Review deleted: id={}", reviewId);
    }

    public ReviewDto getReview(int reviewId) {
        log.info("Get review request, id={}", reviewId);
        return reviewMapper.mapToDto(getReviewOrThrow(reviewId));
    }

    public List<ReviewDto> getReviews(Integer filmId, int count) {
        if (filmId != null) {
            getFilmOrThrow(filmId);
        }
        return reviewStorage.getReviews(filmId, count).stream()
                .map(reviewMapper::mapToDto)
                .toList();
    }

    @Transactional
    public void addLike(int reviewId, int userId) {
        getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        validateReactionAbsent(reviewId, userId);
        reviewStorage.addReaction(reviewId, userId, true);
        log.info("User {} liked review {}", userId, reviewId);
    }

    @Transactional
    public void addDislike(int reviewId, int userId) {
        getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        validateReactionAbsent(reviewId, userId);
        reviewStorage.addReaction(reviewId, userId, false);
        log.info("User {} disliked review {}", userId, reviewId);
    }

    @Transactional
    public void removeLike(int reviewId, int userId) {
        getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        validateReactionForRemoval(reviewId, userId, true);
        reviewStorage.removeReaction(reviewId, userId, true);
        log.info("User {} removed like from review {}", userId, reviewId);
    }

    @Transactional
    public void removeDislike(int reviewId, int userId) {
        getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        validateReactionForRemoval(reviewId, userId, false);
        reviewStorage.removeReaction(reviewId, userId, false);
        log.info("User {} removed dislike from review {}", userId, reviewId);
    }

    private Review getReviewOrThrow(int reviewId) {
        return reviewStorage.getReview(reviewId)
                .orElseThrow(() -> {
                    log.warn("Review not found, id={}", reviewId);
                    return new NotFoundException("Review not found");
                });
    }

    private void getUserOrThrow(int userId) {
        userStorage.getUser(userId)
                .orElseThrow(() -> {
                    log.warn("User not found, id={}", userId);
                    return new NotFoundException("User not found");
                });
    }

    private void getFilmOrThrow(int filmId) {
        filmStorage.getFilm(filmId)
                .orElseThrow(() -> {
                    log.warn("Film not found, id={}", filmId);
                    return new NotFoundException("Film not found");
                });
    }

    private void validateUserAndFilm(int userId, int filmId) {
        getUserOrThrow(userId);
        getFilmOrThrow(filmId);
    }

    private void validateReactionAbsent(int reviewId, int userId) {
        if (reviewStorage.getReaction(reviewId, userId).isPresent()) {
            throw new ValidationException("User already added reaction to this review");
        }
    }

    private void validateReactionForRemoval(int reviewId, int userId, boolean useful) {
        Boolean reaction = reviewStorage.getReaction(reviewId, userId)
                .orElseThrow(() -> new ValidationException("User has not added reaction to this review"));
        if (reaction != useful) {
            String reactionName = useful ? "like" : "dislike";
            throw new ValidationException("User has not added " + reactionName + " to this review");
        }
    }
}
