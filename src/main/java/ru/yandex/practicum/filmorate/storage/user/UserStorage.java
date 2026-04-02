package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User createUser(User user);

    Optional<User> getUser(int id);

    List<User> getAllUsers();

    Optional<User> updateUser(User user);

    void deleteUser(int userId);

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    boolean hasFriend(int userId, int friendId);

    Collection<User> getFriends(int userId);

    Collection<User> getCommonFriends(int userId, int otherId);

    Collection<Film> getRecommendations(int userId);
}
