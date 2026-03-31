package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate.storage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {

    private final ReviewDbStorage reviewStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    private User author;
    private User secondAuthor;
    private User voter;
    private Film film;
    private Film secondFilm;

    @BeforeEach
    void setUp() {
        author = userStorage.createUser(createUser("author"));
        secondAuthor = userStorage.createUser(createUser("second"));
        voter = userStorage.createUser(createUser("voter"));
        film = filmStorage.createFilm(createFilm("Film one"));
        secondFilm = filmStorage.createFilm(createFilm("Film two"));
    }

    @Test
    void testCreateAndGetReview() {
        Review created = reviewStorage.createReview(createReview("Review", author.getId(), film.getId(), true));

        Optional<Review> found = reviewStorage.getReview(created.getReviewId());

        assertTrue(created.getReviewId() > 0);
        assertTrue(found.isPresent());
        assertEquals("Review", found.get().getContent());
        assertEquals(0, found.get().getUseful());
    }

    @Test
    void testUpdateReview() {
        Review created = reviewStorage.createReview(createReview("Review", author.getId(), film.getId(), true));
        created.setContent("Updated");
        created.setPositive(false);

        Review updated = reviewStorage.updateReview(created).orElseThrow();

        assertEquals("Updated", updated.getContent());
        assertFalse(updated.isPositive());
    }

    @Test
    void testGetReviewsByFilmSortedByUseful() {
        Review topReview = reviewStorage.createReview(createReview("Top", author.getId(), film.getId(), true));
        Review lowReview = reviewStorage.createReview(createReview("Low", secondAuthor.getId(), film.getId(), false));
        reviewStorage.createReview(createReview("Other film", author.getId(), secondFilm.getId(), true));

        reviewStorage.addReaction(topReview.getReviewId(), voter.getId(), true);
        reviewStorage.addReaction(lowReview.getReviewId(), voter.getId(), false);

        List<Review> reviews = reviewStorage.getReviews(film.getId(), 10);

        assertEquals(2, reviews.size());
        assertEquals(topReview.getReviewId(), reviews.get(0).getReviewId());
        assertEquals(1, reviews.get(0).getUseful());
        assertEquals(lowReview.getReviewId(), reviews.get(1).getReviewId());
        assertEquals(-1, reviews.get(1).getUseful());
    }

    @Test
    void testChangingReactionUpdatesUsefulScore() {
        Review review = reviewStorage.createReview(createReview("Review", author.getId(), film.getId(), true));

        reviewStorage.addReaction(review.getReviewId(), voter.getId(), true);
        reviewStorage.addReaction(review.getReviewId(), voter.getId(), false);

        Review updated = reviewStorage.getReview(review.getReviewId()).orElseThrow();
        assertEquals(-1, updated.getUseful());
    }

    @Test
    void testDuplicateReactionDoesNotChangeUsefulScore() {
        Review review = reviewStorage.createReview(createReview("Review", author.getId(), film.getId(), true));

        reviewStorage.addReaction(review.getReviewId(), voter.getId(), true);
        reviewStorage.addReaction(review.getReviewId(), voter.getId(), true);

        Review updated = reviewStorage.getReview(review.getReviewId()).orElseThrow();
        assertEquals(1, updated.getUseful());
    }

    @Test
    void testRemoveReactionRestoresUsefulScore() {
        Review review = reviewStorage.createReview(createReview("Review", author.getId(), film.getId(), true));

        reviewStorage.addReaction(review.getReviewId(), voter.getId(), true);
        reviewStorage.removeReaction(review.getReviewId(), voter.getId(), true);

        Review updated = reviewStorage.getReview(review.getReviewId()).orElseThrow();
        assertEquals(0, updated.getUseful());
    }

    @Test
    void testDeleteReview() {
        Review review = reviewStorage.createReview(createReview("Review", author.getId(), film.getId(), true));

        reviewStorage.deleteReview(review.getReviewId());

        assertTrue(reviewStorage.getReviews(null, 10).isEmpty());
        assertTrue(reviewStorage.getReview(review.getReviewId()).isEmpty());
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }

    private Review createReview(String content, int userId, int filmId, boolean positive) {
        Review review = new Review();
        review.setContent(content);
        review.setPositive(positive);
        review.setUserId(userId);
        review.setFilmId(filmId);
        return review;
    }
}
