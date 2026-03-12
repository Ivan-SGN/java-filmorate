package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        validateUser(user);
        User createdUser = userStorage.createUser(user);
        log.info("User added: id={}", createdUser.getId());
        return createdUser;
    }

    public User updateUser(User user) {
        getUserOrThrow(user.getId());
        validateUser(user);
        User updatedUser = userStorage.updateUser(user)
                .orElseThrow(() -> new IllegalStateException("Error during update user"));
        log.info("User updated: id={}", updatedUser.getId());
        return updatedUser;
    }

    public Collection<User> getAllUsers() {
        log.info("Get all users request");
        return userStorage.getAllUsers();
    }

    public User getUser(int id) {
        log.info("Get user request, id: {}", id);
        return getUserOrThrow(id);
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

    public Collection<User> getFriends(int userId) {
        getUserOrThrow(userId);
        log.info("Get friends request for user {}", userId);
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        if (userId == otherId) {
            log.warn("User tried to get common friends with himself: id={}", userId);
            throw new ValidationException("Users must be different");
        }
        getUserOrThrow(userId);
        getUserOrThrow(otherId);
        log.info("Get common friends for users {} and {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    private User getUserOrThrow(int id) {
        return userStorage.getUser(id)
                .orElseThrow(() -> {
                    log.warn("User not found, id={}", id);
                    return new NotFoundException("User not found");
                });
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Validation failed: login is empty");
            throw new ValidationException("Login cannot be empty");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Validation failed: login contains spaces");
            throw new ValidationException("Login cannot contain spaces");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}