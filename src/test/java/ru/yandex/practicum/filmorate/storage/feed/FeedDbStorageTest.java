package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate.storage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FeedDbStorageTest {

    private final FeedDbStorage feedStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final ReviewDbStorage reviewStorage;

    private User user1;
    private User user2;
    private Film film;
    private Review review;

    @BeforeEach
    void setUp() {
        user1 = userStorage.createUser(createUser("user1"));
        user2 = userStorage.createUser(createUser("user2"));
        film = filmStorage.createFilm(createFilm("film"));
        review = reviewStorage.createReview(createReview("review", user1.getId(), film.getId(), true));
    }

    @Test
    void testAddAndGetFeedEvents() {
        feedStorage.addEvent(user1.getId(), EventType.FRIEND, Operation.ADD, user2.getId());
        feedStorage.addEvent(user1.getId(), EventType.LIKE, Operation.ADD, film.getId());
        feedStorage.addEvent(user1.getId(), EventType.REVIEW, Operation.UPDATE, review.getReviewId());

        List<FeedEvent> feed = feedStorage.getFeed(user1.getId());

        assertEquals(3, feed.size());
        assertEquals(EventType.FRIEND, feed.get(0).getEventType());
        assertEquals(EventType.LIKE, feed.get(1).getEventType());
        assertEquals(EventType.REVIEW, feed.get(2).getEventType());
    }

    @Test
    void testGetFeedReturnsOnlyRequestedUserEvents() {
        User anotherUser = userStorage.createUser(createUser("another"));
        feedStorage.addEvent(user1.getId(), EventType.FRIEND, Operation.ADD, user2.getId());
        feedStorage.addEvent(anotherUser.getId(), EventType.LIKE, Operation.ADD, film.getId());

        List<FeedEvent> userFeed = feedStorage.getFeed(user1.getId());

        assertEquals(1, userFeed.size());
        assertEquals(user1.getId(), userFeed.getFirst().getUserId());
        assertEquals(EventType.FRIEND, userFeed.getFirst().getEventType());
    }

    @Test
    void testFeedIsOrderedByTimestampAndEventId() {
        feedStorage.addEvent(user1.getId(), EventType.FRIEND, Operation.ADD, user2.getId());
        feedStorage.addEvent(user1.getId(), EventType.LIKE, Operation.ADD, film.getId());

        List<FeedEvent> feed = feedStorage.getFeed(user1.getId());

        assertEquals(2, feed.size());
        assertEquals(EventType.FRIEND, feed.get(0).getEventType());
        assertEquals(EventType.LIKE, feed.get(1).getEventType());
        assertTrue(feed.get(0).getTimestamp() <= feed.get(1).getTimestamp());
        assertTrue(feed.get(0).getEventId() < feed.get(1).getEventId());
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
