package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.controller.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.controller.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class FeedServiceTest {

    private final UserService userService;
    private final FilmService filmService;

    @Autowired
    FeedServiceTest(UserService userService, FilmService filmService) {
        this.userService = userService;
        this.filmService = filmService;
    }

    @Test
    void testUserFeedEventsInChronologicalOrder() {
        UserDto user1 = userService.addUser(createUser("user1"));
        UserDto user2 = userService.addUser(createUser("user2"));
        int filmId = filmService.addFilm(createFilm("film")).getId().intValue();

        userService.addFriend(user1.getId().intValue(), user2.getId().intValue());
        filmService.addLike(filmId, user1.getId().intValue());
        userService.removeFriend(user1.getId().intValue(), user2.getId().intValue());

        List<FeedEventDto> feed = userService.getFeed(user1.getId().intValue()).stream().toList();

        assertEquals(3, feed.size());
        assertEvent(feed.get(0), user1.getId().intValue(), EventType.FRIEND, Operation.ADD, user2.getId().intValue());
        assertEvent(feed.get(1), user1.getId().intValue(), EventType.LIKE, Operation.ADD, filmId);
        assertEvent(feed.get(2), user1.getId().intValue(), EventType.FRIEND, Operation.REMOVE,
                user2.getId().intValue());
    }

    @Test
    void testNotAddDuplicateFeedEvents() {
        UserDto user1 = userService.addUser(createUser("user1"));
        UserDto user2 = userService.addUser(createUser("user2"));
        int filmId = filmService.addFilm(createFilm("film")).getId().intValue();

        userService.addFriend(user1.getId().intValue(), user2.getId().intValue());
        userService.addFriend(user1.getId().intValue(), user2.getId().intValue());
        filmService.addLike(filmId, user1.getId().intValue());
        filmService.addLike(filmId, user1.getId().intValue());

        List<FeedEventDto> feed = userService.getFeed(user1.getId().intValue()).stream().toList();

        assertEquals(2, feed.size());
        assertEvent(feed.get(0), user1.getId().intValue(), EventType.FRIEND, Operation.ADD, user2.getId().intValue());
        assertEvent(feed.get(1), user1.getId().intValue(), EventType.LIKE, Operation.ADD, filmId);
    }

    @Test
    void testGetFeedForUnknownUser() {
        assertThrows(NotFoundException.class, () -> userService.getFeed(999999));
    }

    private void assertEvent(
            FeedEventDto event,
            int expectedUserId,
            EventType expectedEventType,
            Operation expectedOperation,
            int expectedEntityId
    ) {
        assertEquals(expectedUserId, event.getUserId());
        assertEquals(expectedEventType, event.getEventType());
        assertEquals(expectedOperation, event.getOperation());
        assertEquals(expectedEntityId, event.getEntityId());
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
}
