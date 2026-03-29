package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.controller.dto.ReviewDto;
import ru.yandex.practicum.filmorate.controller.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ReviewServiceTest {

    private final ReviewService reviewService;
    private final UserService userService;
    private final FilmService filmService;

    @Autowired
    ReviewServiceTest(ReviewService reviewService, UserService userService, FilmService filmService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.filmService = filmService;
    }

    @Test
    void testAddReviewStartsWithZeroUseful() {
        UserDto user = userService.addUser(createUser("author"));
        long filmId = filmService.addFilm(createFilm("Film")).getId();
        ReviewDto reviewDto = createReview("Review", user.getId().intValue(), (int) filmId, true);
        reviewDto.setUseful(99);

        ReviewDto created = reviewService.addReview(reviewDto);

        assertEquals(0, created.getUseful());
    }

    @Test
    void testUpdateReviewPreservesUsefulScore() {
        UserDto author = userService.addUser(createUser("author"));
        UserDto voter = userService.addUser(createUser("voter"));
        int filmId = filmService.addFilm(createFilm("Film")).getId().intValue();
        ReviewDto created = reviewService.addReview(createReview("Review", author.getId().intValue(), filmId, true));
        reviewService.addLike(created.getReviewId().intValue(), voter.getId().intValue());

        created.setContent("Updated");
        created.setUseful(-100);
        ReviewDto updated = reviewService.updateReview(created);

        assertEquals("Updated", updated.getContent());
        assertEquals(1, updated.getUseful());
    }

    @Test
    void testGetReviewsByFilmFiltersAndSorts() {
        UserDto firstAuthor = userService.addUser(createUser("author1"));
        UserDto secondAuthor = userService.addUser(createUser("author2"));
        UserDto voter = userService.addUser(createUser("voter"));
        int filmId = filmService.addFilm(createFilm("Film one")).getId().intValue();
        int otherFilmId = filmService.addFilm(createFilm("Film two")).getId().intValue();

        ReviewDto topReview = reviewService.addReview(
                createReview("Top", firstAuthor.getId().intValue(), filmId, true)
        );
        ReviewDto lowReview = reviewService.addReview(
                createReview("Low", secondAuthor.getId().intValue(), filmId, false)
        );
        reviewService.addReview(createReview("Other", firstAuthor.getId().intValue(), otherFilmId, true));

        reviewService.addLike(topReview.getReviewId().intValue(), voter.getId().intValue());
        reviewService.addDislike(lowReview.getReviewId().intValue(), voter.getId().intValue());

        List<ReviewDto> reviews = reviewService.getReviews(filmId, 10);

        assertEquals(2, reviews.size());
        assertEquals(topReview.getReviewId(), reviews.get(0).getReviewId());
        assertEquals(1, reviews.get(0).getUseful());
        assertEquals(lowReview.getReviewId(), reviews.get(1).getReviewId());
        assertEquals(-1, reviews.get(1).getUseful());
    }

    @Test
    void testAddSecondReactionThrowsValidationException() {
        UserDto author = userService.addUser(createUser("author"));
        UserDto voter = userService.addUser(createUser("voter"));
        int filmId = filmService.addFilm(createFilm("Film")).getId().intValue();
        ReviewDto review = reviewService.addReview(createReview("Review", author.getId().intValue(), filmId, true));

        reviewService.addLike(review.getReviewId().intValue(), voter.getId().intValue());

        assertThrows(
                ValidationException.class,
                () -> reviewService.addDislike(review.getReviewId().intValue(), voter.getId().intValue())
        );
    }

    @Test
    void testRemoveReactionByAnotherUserThrowsValidationException() {
        UserDto author = userService.addUser(createUser("author"));
        UserDto voter = userService.addUser(createUser("voter"));
        UserDto anotherUser = userService.addUser(createUser("another"));
        int filmId = filmService.addFilm(createFilm("Film")).getId().intValue();
        ReviewDto review = reviewService.addReview(createReview("Review", author.getId().intValue(), filmId, true));

        reviewService.addLike(review.getReviewId().intValue(), voter.getId().intValue());

        assertThrows(
                ValidationException.class,
                () -> reviewService.removeLike(review.getReviewId().intValue(), anotherUser.getId().intValue())
        );
    }

    @Test
    void testRemoveWrongReactionTypeThrowsValidationException() {
        UserDto author = userService.addUser(createUser("author"));
        UserDto voter = userService.addUser(createUser("voter"));
        int filmId = filmService.addFilm(createFilm("Film")).getId().intValue();
        ReviewDto review = reviewService.addReview(createReview("Review", author.getId().intValue(), filmId, true));

        reviewService.addLike(review.getReviewId().intValue(), voter.getId().intValue());

        assertThrows(
                ValidationException.class,
                () -> reviewService.removeDislike(review.getReviewId().intValue(), voter.getId().intValue())
        );
    }

    private UserDto createUser(String login) {
        UserDto user = new UserDto();
        user.setEmail(login + "@mail.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private FilmRqDto createFilm(String name) {
        FilmRqDto film = new FilmRqDto();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }

    private ReviewDto createReview(String content, int userId, int filmId, boolean positive) {
        ReviewDto review = new ReviewDto();
        review.setContent(content);
        review.setPositive(positive);
        review.setUserId(userId);
        review.setFilmId(filmId);
        return review;
    }
}
