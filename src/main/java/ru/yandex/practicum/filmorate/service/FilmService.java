package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.controller.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private static final Set<String> ALLOWED_PARAMS = Set.of("title", "director");
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmMapper filmMapper;
    private final FeedStorage feedStorage;
    private final DirectorStorage directorStorage;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("feedDbStorage") FeedStorage feedStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            @Qualifier("directorDbStorage") DirectorStorage directorDBStorage,
            FilmMapper filmMapper, DirectorStorage directorStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.feedStorage = feedStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.filmMapper = filmMapper;
        this.directorStorage = directorStorage;
    }

    public FilmRsDto addFilm(FilmRqDto filmRqDto) {
        Film film = filmMapper.map(filmRqDto);
        resolveReferences(film);
        Film createdFilm = filmStorage.createFilm(film);
        log.info("Film added: id={}", createdFilm.getId());
        return filmMapper.mapToRsDto(createdFilm);
    }

    public FilmRsDto updateFilm(FilmRqDto filmRqDto) {
        Film film = filmMapper.map(filmRqDto);
        getFilmOrThrow(film.getId());
        resolveReferences(film);
        Film updatedFilm = filmStorage.updateFilm(film)
                .orElseThrow(() -> new IllegalStateException("Film update failed"));
        log.info("Film updated: id={}", updatedFilm.getId());
        return filmMapper.mapToRsDto(updatedFilm);
    }

    public Collection<FilmRsDto> getAllFilms() {
        log.info("Get all films request");
        return filmStorage.getAllFilms().stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    public FilmRsDto getFilm(int id) {
        log.info("Get film request, id={}", id);
        return filmMapper.mapToRsDto(getFilmOrThrow(id));
    }

    public List<FilmRsDto> getPopular(int count, Integer genreId, Year year) {
        getGenreOrThrow(genreId);
        return filmStorage.getPopularFilms(count, genreId, year).stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    public List<FilmRsDto> getCommon(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId).stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    public void addLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        boolean hasLike = filmStorage.hasLike(filmId, userId);
        filmStorage.addLike(filmId, userId);
        if (!hasLike) {
            feedStorage.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
        }
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        boolean hasLike = filmStorage.hasLike(filmId, userId);
        filmStorage.removeLike(filmId, userId);
        if (hasLike) {
            feedStorage.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
        }
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public void deleteFilm(int filmId) {
        getFilmOrThrow(filmId);
        filmStorage.deleteFilm(filmId);
        log.info("Film {} deleted", filmId);
    }

    public List<FilmRsDto> getFilmsByDirector(int directorId, String sortBy) {
        return filmStorage.getFilmsByDirector(directorId, sortBy).stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    public List<FilmRsDto> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query must not be empty");
        }

        if (by == null || by.isBlank()) {
            throw new IllegalArgumentException("Parameter 'by' must not be empty");
        }

        Set<String> allowed = Set.of("title", "director");

        Set<String> params = Arrays.stream(by.toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        if (!allowed.containsAll(params)) {
            log.warn("Invalid 'by' parameter: {}", by);
            throw new IllegalArgumentException("Parameter 'by' must be 'title', 'director' or both");
        }
        Set<String> byParams = parseAndValidate(by);
        return filmStorage.searchFilms(query, byParams).stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    private Set<String> parseAndValidate(String by) {
        if (by == null || by.isBlank()) {
            throw new IllegalArgumentException("Parameter 'by' is empty");
        }
        Set<String> params = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (!ALLOWED_PARAMS.containsAll(params)) {
            throw new IllegalArgumentException("Unknown search parameter");
        }
        return params;
    }

    private Film getFilmOrThrow(int id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> {
                    log.warn("Film not found, id={}", id);
                    return new NotFoundException("Film not found");
                });
    }

    private void getUserOrThrow(int id) {
        userStorage.getUser(id)
                .orElseThrow(() -> {
                    log.warn("User not found, id={}", id);
                    return new NotFoundException("User not found");
                });
    }

    private void getGenreOrThrow(Integer genreId) {
        if (genreId == null) {
            return;
        }
        genreStorage.getById(genreId)
                .orElseThrow(() -> {
                    log.warn("Genre not found, id={}", genreId);
                    return new NotFoundException("Genre not found");
                });
    }

    private void resolveReferences(Film film) {
        film.setGenres(resolveGenres(film.getGenres()));
        film.setMpa(resolveMpa(film.getMpa()));
        film.setDirectors(resolveDirectors(film.getDirectors()));
    }

    private Mpa resolveMpa(Mpa mpa) {
        if (mpa == null) {
            return null;
        }
        int id = mpa.getId();
        return mpaStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("MPA not found: id={}", id);
                    return new NotFoundException("MPA not found");
                });
    }

    private Set<Genre> resolveGenres(Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return new HashSet<>();
        }
        Set<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Set<Genre> foundGenres = genreStorage.getAllById(genreIds);
        validateGenres(foundGenres, genreIds);
        return foundGenres;
    }

    private void validateGenres(Set<Genre> foundGenres, Set<Integer> requestedGenreIds) {
        Set<Integer> foundGenreIds = foundGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Set<Integer> missingGenreIds = new HashSet<>(requestedGenreIds);
        missingGenreIds.removeAll(foundGenreIds);
        if (!missingGenreIds.isEmpty()) {
            log.warn("Genres not found: missingIds={}", missingGenreIds);
            throw new NotFoundException("Genres not found: " + missingGenreIds);
        }
    }

    private Set<Director> resolveDirectors(Collection<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return new HashSet<>();
        }

        Set<Integer> ids = directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        List<Director> found = ids.stream()
                .map(id -> directorStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("Director not found: " + id)))
                .toList();

        return new HashSet<>(found);
    }
}
