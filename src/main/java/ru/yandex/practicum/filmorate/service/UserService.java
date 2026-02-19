package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    public User addUser(User user) {
        log.info("Add user request: id={}", user.getId());
        validateLogin(user);
        normalizeName(user);
        user.setId(nextId);
        users.put(nextId, user);
        log.info("User added: id={}", user.getId());
        nextId++;
        return user;
    }

    public User updateUser(User user) {
        log.info("Update user request: id={}", user.getId());
        if (!users.containsKey(user.getId())) {
            log.warn("User not found for update: id={}", user.getId());
            throw new NotFoundException("User with id: "  + user.getId() + "not found");
        }
        validateLogin(user);
        normalizeName(user);
        users.put(user.getId(), user);
        log.info("User updated: id={}", user.getId());
        return user;
    }

    public Collection<User> getAllUsers() {
        log.info("Get all users request");
        return users.values();
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