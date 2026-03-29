package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {

    private static final String FIND_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_ALL = "SELECT * FROM reviews ORDER BY useful DESC, review_id ASC LIMIT ?";
    private static final String FIND_BY_FILM =
            "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC, review_id ASC LIMIT ?";
    private static final String INSERT =
            "INSERT INTO reviews(content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE review_id = ?";
    private static final String DELETE = "DELETE FROM reviews WHERE review_id = ?";
    private static final String FIND_REACTION =
            "SELECT is_useful FROM review_reactions WHERE review_id = ? AND user_id = ?";
    private static final String UPSERT_REACTION =
            "MERGE INTO review_reactions (review_id, user_id, is_useful) KEY (review_id, user_id) VALUES (?, ?, ?)";
    private static final String DELETE_REACTION =
            "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?";
    private static final String CHANGE_USEFUL = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc) {
        super(jdbc, new ReviewRowMapper());
    }

    @Override
    public Review createReview(Review review) {
        long id = insert(
                INSERT,
                review.getContent(),
                review.isPositive(),
                review.getUserId(),
                review.getFilmId(),
                0
        );
        review.setReviewId((int) id);
        review.setUseful(0);
        return review;
    }

    @Override
    public Optional<Review> getReview(int reviewId) {
        return findOne(FIND_BY_ID, reviewId);
    }

    @Override
    public Optional<Review> updateReview(Review review) {
        update(
                UPDATE,
                review.getContent(),
                review.isPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getReviewId()
        );
        return Optional.of(review);
    }

    @Override
    public void deleteReview(int reviewId) {
        delete(DELETE, reviewId);
    }

    @Override
    public List<Review> getReviews(Integer filmId, int count) {
        if (filmId == null) {
            return findMany(FIND_ALL, count);
        }
        return findMany(FIND_BY_FILM, filmId, count);
    }

    @Override
    public Optional<Boolean> getReaction(int reviewId, int userId) {
        return Optional.ofNullable(findReaction(reviewId, userId));
    }

    @Override
    public void addReaction(int reviewId, int userId, boolean useful) {
        Boolean currentReaction = findReaction(reviewId, userId);
        if (currentReaction != null && currentReaction == useful) {
            return;
        }

        update(UPSERT_REACTION, reviewId, userId, useful);
        int delta;
        if (currentReaction == null) {
            delta = useful ? 1 : -1;
        } else {
            delta = useful ? 2 : -2;
        }
        changeUseful(reviewId, delta);
    }

    @Override
    public void removeReaction(int reviewId, int userId, boolean useful) {
        Boolean currentReaction = findReaction(reviewId, userId);
        if (currentReaction == null || currentReaction != useful) {
            return;
        }

        delete(DELETE_REACTION, reviewId, userId);
        changeUseful(reviewId, useful ? -1 : 1);
    }

    private Boolean findReaction(int reviewId, int userId) {
        List<Boolean> reactions = jdbc.query(
                FIND_REACTION,
                (rs, rowNum) -> rs.getBoolean("is_useful"),
                reviewId,
                userId
        );
        if (reactions.isEmpty()) {
            return null;
        }
        return reactions.getFirst();
    }

    private void changeUseful(int reviewId, int delta) {
        update(CHANGE_USEFUL, delta, reviewId);
    }
}
