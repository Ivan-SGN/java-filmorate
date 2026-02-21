package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        log.info("Add user request: id={}", user.getId());
        validateLogin(user);
        normalizeName(user);
        User createdUser = userStorage.createUser(user);
        log.info("User added: id={}", createdUser.getId());
        return createdUser;
    }

    public User updateUser(User user) {
        log.info("Update user request: id={}", user.getId());
        validateLogin(user);
        normalizeName(user);
        User updatedUser = userStorage.updateUser(user);
        log.info("User updated: id={}", updatedUser.getId());
        return updatedUser;
    }

    public Collection<User> getAllUsers() {
        log.info("Get all users request");
        return userStorage.getAllUsers();
    }

    public User getUser(int id) {
        log.info("Get user request, id: {}", id);
        return userStorage.getUser(id);
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Users {} and {} are now friends", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Users {} and {} are no longer friends", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getUser(userId);
        return user.getFriends().stream().map(userStorage::getUser).toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = userStorage.getUser(userId);
        User other = userStorage.getUser(otherId);
        return user.getFriends().stream()
                .filter(id -> other.getFriends().contains(id))
                .map(userStorage::getUser)
                .toList();
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("Validation failed: id={}", user.getId());
            throw new ValidationException("Invalid login");
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Name is blank. Using login as name. id={}", user.getId());
            user.setName(user.getLogin());
        }
    }
}