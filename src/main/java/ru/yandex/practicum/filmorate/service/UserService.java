package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.controller.dto.UserDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.controller.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;
    private final FilmMapper filmMapper;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            UserMapper userMapper,
            FilmMapper filmMapper
    ) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
        this.filmMapper = filmMapper;
    }

    public UserDto addUser(UserDto userDto) {
        User user = userMapper.map(userDto);
        User createdUser = userStorage.createUser(user);
        log.info("User added: id={}", createdUser.getId());
        return userMapper.mapToDto(createdUser);
    }

    public UserDto updateUser(UserDto userDto) {
        User user = userMapper.map(userDto);
        getUserOrThrow(user.getId());
        User updatedUser = userStorage.updateUser(user)
                .orElseThrow(() -> new IllegalStateException("Error during update user"));
        log.info("User updated: id={}", updatedUser.getId());
        return userMapper.mapToDto(updatedUser);
    }

    public Collection<UserDto> getAllUsers() {
        log.info("Get all users");
        return userStorage.getAllUsers().stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    public UserDto getUser(int id) {
        log.info("Get user, id: {}", id);
        return userMapper.mapToDto(getUserOrThrow(id));
    }

    public void deleteUser(int userId) {
        getUserOrThrow(userId);
        userStorage.deleteUser(userId);
        log.info("Deleted user, id: {}", userId);
    }

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.warn("User tried to add himself as friend: id={}", userId);
            throw new ValidationException("User cannot add himself as friend");
        }
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("User {} added friend {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.warn("User tried to remove himself from friends: id={}", userId);
            throw new ValidationException("User cannot remove himself from friends");
        }
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("User {} removed friend {}", userId, friendId);
    }

    public Collection<UserDto> getFriends(int userId) {
        getUserOrThrow(userId);
        log.info("Get friends request for user {}", userId);
        return userStorage.getFriends(userId).stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    public Collection<UserDto> getCommonFriends(int userId, int otherId) {
        if (userId == otherId) {
            log.warn("User tried to get common friends with himself: id={}", userId);
            throw new ValidationException("Users must be different");
        }
        getUserOrThrow(userId);
        getUserOrThrow(otherId);
        log.info("Get common friends for users {} and {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    public Collection<FilmRsDto> getRecommendations(int userId) {
        getUserOrThrow(userId);
        log.info("Get recommendations request for user {}", userId);

        return userStorage.getRecommendations(userId).stream()
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    private User getUserOrThrow(int id) {
        return userStorage.getUser(id)
                .orElseThrow(() -> {
                    log.warn("User not found, id={}", id);
                    return new NotFoundException("User not found");
                });
    }
}
