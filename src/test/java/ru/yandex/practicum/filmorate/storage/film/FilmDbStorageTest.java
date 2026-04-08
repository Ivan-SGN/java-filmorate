package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate.storage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbc;
    private Film testFilm;
    private int testUserId;

    @BeforeEach
    void setUp() {
        testFilm = createFilm();
        testFilm = filmStorage.createFilm(testFilm);
        testUserId = insertUser("test@mail.com", "login", "name", "1990-01-01");
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmStorage.getFilm(testFilm.getId());

        assertTrue(filmOptional.isPresent());
        assertEquals(testFilm.getId(), filmOptional.get().getId());
    }

    @Test
    void testGetAllFilms() {
        List<Film> films = filmStorage.getAllFilms();

        assertFalse(films.isEmpty());
    }

    @Test
    void testUpdateFilm() {
        testFilm.setName("Updated film");

        Film updated = filmStorage.updateFilm(testFilm).orElseThrow();

        assertEquals("Updated film", updated.getName());
        assertEquals(testFilm.getId(), updated.getId());
    }

    @Test
    void testCreateFilm() {
        Film film = createFilm();

        Film created = filmStorage.createFilm(film);

        assertTrue(created.getId() > 0);
        assertEquals("Test film", created.getName());
    }

    @Test
    void testAddLike() {
        filmStorage.addLike(testFilm.getId(), testUserId);

        List<Film> popular = filmStorage.getPopularFilms(10, null, null);

        assertEquals(testFilm.getId(), popular.getFirst().getId());
    }

    @Test
    void testGetPopularFilms() {
        filmStorage.addLike(testFilm.getId(), testUserId);

        List<Film> popular = filmStorage.getPopularFilms(10, null, null);

        assertFalse(popular.isEmpty());
    }

    @Test
    void testRemoveLike() {
        filmStorage.addLike(testFilm.getId(), testUserId);
        filmStorage.removeLike(testFilm.getId(), testUserId);

        List<Film> popular = filmStorage.getPopularFilms(10, null, null);

        assertNotNull(popular);
    }

    @Test
    void testGetPopularFilmsFilteredByGenre() {
        Film film1 = createFilm();
        film1.setGenres(genres(1));
        Film film2 = createFilm();
        film2.setGenres(genres(2));

        Film created1 = filmStorage.createFilm(film1);
        Film created2 = filmStorage.createFilm(film2);

        filmStorage.addLike(created1.getId(), testUserId);
        filmStorage.addLike(created2.getId(), testUserId);

        List<Film> popular = filmStorage.getPopularFilms(10, 1, null);

        assertEquals(1, popular.size());
        assertEquals(created1.getId(), popular.getFirst().getId());
    }

    @Test
    void testGetPopularFilmsFilteredByYear() {
        Film film1 = createFilm();
        Film film2 = createFilm();
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));

        Film created1 = filmStorage.createFilm(film1);
        Film created2 = filmStorage.createFilm(film2);

        filmStorage.addLike(created1.getId(), testUserId);
        filmStorage.addLike(created2.getId(), testUserId);

        List<Film> popular = filmStorage.getPopularFilms(10, null, Year.of(2001));

        assertEquals(1, popular.size());
        assertEquals(created2.getId(), popular.getFirst().getId());
    }

    @Test
    void testDeleteFilm() {
        int filmId = testFilm.getId();

        filmStorage.deleteFilm(filmId);

        Optional<Film> deleted = filmStorage.getFilm(filmId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void testDeleteFilmCascadeLikes() {
        filmStorage.addLike(testFilm.getId(), testUserId);

        filmStorage.deleteFilm(testFilm.getId());

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ?",
                Integer.class,
                testFilm.getId()
        );

        assertEquals(0, count);
    }

    @Test
    void testGetCommonFilms() {
        Film secondFilm = filmStorage.createFilm(createFilm());
        int userId2 = insertUser("test2@mail.com", "login2", "name2", "1990-01-01");

        filmStorage.addLike(testFilm.getId(), testUserId);
        filmStorage.addLike(testFilm.getId(), userId2);
        filmStorage.addLike(secondFilm.getId(), testUserId);
        List<Film> common = filmStorage.getCommonFilms(testUserId, userId2);

        assertEquals(1, common.size());
        assertEquals(testFilm.getId(), common.get(0).getId());
    }

    @Test
    void testGetCommonFilmsSortedByPopularity() {
        Film secondFilm = filmStorage.createFilm(createFilm());
        int userId2 = insertUser("test2@mail.com", "login2", "name2", "1990-01-01");
        int userId3 = insertUser("test3@mail.com", "login3", "name3", "1990-01-01");

        filmStorage.addLike(testFilm.getId(), testUserId);
        filmStorage.addLike(testFilm.getId(), userId2);
        filmStorage.addLike(testFilm.getId(), userId3);
        filmStorage.addLike(secondFilm.getId(), testUserId);
        filmStorage.addLike(secondFilm.getId(), userId2);
        List<Film> common = filmStorage.getCommonFilms(testUserId, userId2);

        assertEquals(2, common.size());
        assertEquals(testFilm.getId(), common.get(0).getId());
        assertEquals(secondFilm.getId(), common.get(1).getId());
    }

    @Test
    void testGetRecommendationsSuccess() {
        long userId2 = insertUser("test2@mail.com", "login2", "name2", "1990-01-01");

        Film film1 = createFilm();
        film1.setName("Film 1");
        long filmId1 = filmStorage.createFilm(film1).getId();

        Film film2 = createFilm();
        film2.setName("Film 2");
        long filmId2 = filmStorage.createFilm(film2).getId();

        jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId1, testUserId);

        jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId1, userId2);
        jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId2, userId2);

        Collection<Film> recommendations = filmStorage.getRecommendations(testUserId);

        assertEquals(1, recommendations.size());
        assertEquals("Film 2", recommendations.iterator().next().getName());
    }

    @Test
    void testGetRecommendationsEmptyWhenNoSimilarUsers() {
        Collection<Film> recommendations = filmStorage.getRecommendations(testUserId);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testGetRecommendationsEmptyWhenLikesAreIdentical() {
        int userId2 = insertUser("test2@mail.com", "login2", "name2", "1990-01-01");

        Film film1 = createFilm();
        film1.setName("Film 1");
        long filmId1 = filmStorage.createFilm(film1).getId();

        Film film2 = createFilm();
        film2.setName("Film 2");
        long filmId2 = filmStorage.createFilm(film2).getId();

        jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId1, testUserId);
        jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId2, testUserId);

        jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId1, userId2);
        jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId2, userId2);

        Collection<Film> recommendations = filmStorage.getRecommendations(1);

        assertTrue(recommendations.isEmpty());
    }

    private Film createFilm() {
        Film film = new Film();
        film.setName("Test film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }

    private Set<Genre> genres(int... ids) {
        Set<Genre> genres = new LinkedHashSet<>();
        for (int id : ids) {
            Genre genre = new Genre();
            genre.setId(id);
            genres.add(genre);
        }
        return genres;
    }

    private int insertUser(String email, String login, String name, String birthday) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) " +
                            "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, email);
            ps.setString(2, login);
            ps.setString(3, name);
            ps.setString(4, birthday);
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }
}
