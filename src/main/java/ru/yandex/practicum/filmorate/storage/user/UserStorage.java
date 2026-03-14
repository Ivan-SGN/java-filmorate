package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User createUser(User user);

    Optional<User> getUser(int id);

    List<User> getAllUsers();

    Optional<User> updateUser(User user);

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    Collection<User> getFriends(int userId);

    Collection<User> getCommonFriends(int userId, int otherId);
}
