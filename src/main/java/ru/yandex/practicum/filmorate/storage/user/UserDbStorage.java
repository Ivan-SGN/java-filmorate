package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String ADD_FRIEND = "MERGE INTO friends (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
    private static final String REMOVE_FRIEND = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS =
            "SELECT u.* FROM users u JOIN friends f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String GET_COMMON_FRIENDS =
            "SELECT u.* FROM users u " +
            "JOIN friends f1 ON u.id = f1.friend_id " +
            "JOIN friends f2 ON u.id = f2.friend_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc) {
        super(jdbc, new UserRowMapper());
    }

    @Override
    public User createUser(User user) {
        long id = insert(
                INSERT,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId((int) id);
        return user;
    }

    @Override
    public Optional<User> getUser(int id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public List<User> getAllUsers() {
        return findMany(FIND_ALL);
    }

    @Override
    public Optional<User> updateUser(User user) {
        update(
                UPDATE,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return Optional.of(user);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        jdbc.update(ADD_FRIEND, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbc.update(REMOVE_FRIEND, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        return jdbc.query(GET_FRIENDS, mapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        return jdbc.query(GET_COMMON_FRIENDS, mapper, userId, otherId);
    }
}