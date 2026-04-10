package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review createReview(Review review);

    Optional<Review> getReview(int reviewId);

    Optional<Review> updateReview(Review review);

    void deleteReview(int reviewId);

    List<Review> getReviews(Integer filmId, int count);

    Optional<Boolean> getReaction(int reviewId, int userId);

    void addReaction(int reviewId, int userId, boolean useful);

    void removeReaction(int reviewId, int userId, boolean useful);
}
