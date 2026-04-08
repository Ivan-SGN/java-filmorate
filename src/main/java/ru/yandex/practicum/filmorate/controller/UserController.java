package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.controller.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.controller.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.addUser(userDto);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserDto userDto) {
        validateUpdateId(userDto.getId(), "User");
        return userService.updateUser(userDto);
    }

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable int userId) {
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int userId) {
        userService.deleteUser(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable int userId, @PathVariable int friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFriend(@PathVariable int userId, @PathVariable int friendId) {
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public Collection<UserDto> getFriends(@PathVariable int userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<UserDto> getCommonFriends(@PathVariable int userId, @PathVariable int otherId) {
        return userService.getCommonFriends(userId, otherId);
    }

    @GetMapping("/{userId}/recommendations")
    public Collection<FilmRsDto> getRecommendations(@PathVariable int userId) {
        return userService.getRecommendations(userId);
    }

    @GetMapping("/{userId}/feed")
    public Collection<FeedEventDto> getFeed(@PathVariable int userId) {
        return userService.getFeed(userId);
    }

    private void validateUpdateId(Long id, String entityName) {
        if (id == null || id <= 0) {
            throw new ValidationException(entityName + " id must be positive");
        }
    }
}
