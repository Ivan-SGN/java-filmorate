package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate.storage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbc;
    private final DirectorStorage directorStorage;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = createFilm();
        testFilm = filmStorage.createFilm(testFilm);

        jdbc.update("INSERT INTO users(id,email,login,name,birthday) VALUES (1,'test@mail.com','login','name','1990-01-01')");
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
        filmStorage.addLike(testFilm.getId(), 1);

        List<Film> popular = filmStorage.getPopularFilms(10, null, null);

        assertEquals(testFilm.getId(), popular.getFirst().getId());
    }

    @Test
    void testGetPopularFilms() {
        filmStorage.addLike(testFilm.getId(), 1);

        List<Film> popular = filmStorage.getPopularFilms(10, null, null);

        assertFalse(popular.isEmpty());
    }

    @Test
    void testRemoveLike() {
        filmStorage.addLike(testFilm.getId(), 1);
        filmStorage.removeLike(testFilm.getId(), 1);

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

        filmStorage.addLike(created1.getId(), 1);
        filmStorage.addLike(created2.getId(), 1);

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

        filmStorage.addLike(created1.getId(), 1);
        filmStorage.addLike(created2.getId(), 1);

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
        filmStorage.addLike(testFilm.getId(), 1);

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
        jdbc.update("INSERT INTO users(id,email,login,name,birthday) VALUES (2,'test2@mail.com','login2','name2','1991-01-01')");

        filmStorage.addLike(testFilm.getId(), 1);
        filmStorage.addLike(testFilm.getId(), 2);
        filmStorage.addLike(secondFilm.getId(), 1);
        List<Film> common = filmStorage.getCommonFilms(1, 2);

        assertEquals(1, common.size());
        assertEquals(testFilm.getId(), common.get(0).getId());
    }

    @Test
    void testGetCommonFilmsSortedByPopularity() {
        Film secondFilm = filmStorage.createFilm(createFilm());
        jdbc.update("INSERT INTO users(id,email,login,name,birthday) VALUES (2,'test2@mail.com','login2','name2','1991-01-01')");
        jdbc.update("INSERT INTO users(id,email,login,name,birthday) VALUES (3,'test3@mail.com','login3','name3','1992-01-01')");

        filmStorage.addLike(testFilm.getId(), 1);
        filmStorage.addLike(testFilm.getId(), 2);
        filmStorage.addLike(testFilm.getId(), 3);
        filmStorage.addLike(secondFilm.getId(), 1);
        filmStorage.addLike(secondFilm.getId(), 2);
        List<Film> common = filmStorage.getCommonFilms(1, 2);

        assertEquals(2, common.size());
        assertEquals(testFilm.getId(), common.get(0).getId());
        assertEquals(secondFilm.getId(), common.get(1).getId());
    }

    @Test
    void testCreateFilmWithDirectors() {
        Film film = createFilm();

        // добавляем режиссёров
        Director d1 = directorStorage.create(new Director(1, "name1"));
        Director d2 = directorStorage.create(new Director(2, "name2"));

        film.setDirectors(Set.of(d1, d2));

        Film created = filmStorage.createFilm(film);

        List<Integer> directorIds = jdbc.queryForList(
                "SELECT director_id FROM film_directors WHERE film_id = ?",
                Integer.class,
                created.getId()
        );

        assertEquals(2, directorIds.size());
    }

    @Test
    void testUpdateFilmDirectors() {
        Film film = createFilm();
        Director d1 = directorStorage.create(new Director(1, "name1"));
        film.setDirectors(Set.of(d1));

        Film created = filmStorage.createFilm(film);

        // обновляем режиссёров
        Director d2 = directorStorage.create(new Director(2, "name2"));
        created.setDirectors(Set.of(d2));

        filmStorage.updateFilm(created);

        List<Integer> directorIds = jdbc.queryForList(
                "SELECT director_id FROM film_directors WHERE film_id = ?",
                Integer.class,
                created.getId()
        );

        assertEquals(1, directorIds.size());
    }

    @Test
    void testGetFilmWithDirectors() {
        Film film = createFilm();
        Director d1 = directorStorage.create(new Director(1, "name1"));
        Director d2 = directorStorage.create(new Director(2, "name2"));

        film.setDirectors(Set.of(d1, d2));
        Film created = filmStorage.createFilm(film);

        Optional<Film> found = filmStorage.getFilm(created.getId());

        assertTrue(found.isPresent());
        assertEquals(2, found.get().getDirectors().size());
    }

    @Test
    void testDeleteFilmCascadeDirectors() {
        Film film = createFilm();
        Director d1 = directorStorage.create(new Director(1, "name1"));


        film.setDirectors(Set.of(d1));

        Film created = filmStorage.createFilm(film);

        filmStorage.deleteFilm(created.getId());

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_directors WHERE film_id = ?",
                Integer.class,
                created.getId()
        );

        assertEquals(0, count);
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
}
