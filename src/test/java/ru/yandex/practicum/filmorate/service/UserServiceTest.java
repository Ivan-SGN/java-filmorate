package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class UserServiceTest {

    private final UserService userService;

    @Autowired
    public UserServiceTest(UserService userService) {
        this.userService = userService;
    }

    @Test
    void testAddUser() {
        UserDto user = userService.addUser(createUser("login1"));

        assertEquals(1L, user.getId());
        assertEquals("login1", user.getLogin());
    }

    @Test
    void testUpdateUser() {
        UserDto user = userService.addUser(createUser("login1"));
        user.setName("updated");

        UserDto updated = userService.updateUser(user);

        assertEquals("updated", updated.getName());
        assertEquals(user.getId(), updated.getId());
    }

    @Test
    void testGetUser() {
        UserDto user = userService.addUser(createUser("login1"));

        UserDto found = userService.getUser(user.getId().intValue());

        assertEquals(user.getId(), found.getId());
        assertEquals("login1", found.getLogin());
    }

    @Test
    void testGetAllUsers() {
        userService.addUser(createUser("login1"));
        userService.addUser(createUser("login2"));

        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    void testDeleteUser() {
        UserDto user = userService.addUser(createUser("login1"));

        userService.deleteUser(user.getId().intValue());

        assertThrows(NotFoundException.class,
                () -> userService.getUser(user.getId().intValue()));
    }

    @Test
    void testDeleteNonExistingUser() {
        assertThrows(NotFoundException.class,
                () -> userService.deleteUser(999999));
    }

    @Test
    void testAddFriendIsMutual() {
        UserDto user1 = userService.addUser(createUser("login1"));
        UserDto user2 = userService.addUser(createUser("login2"));

        userService.addFriend(user1.getId().intValue(), user2.getId().intValue());

        assertEquals(1, userService.getFriends(user1.getId().intValue()).size());
        assertEquals(0, userService.getFriends(user2.getId().intValue()).size());
    }

    @Test
    void testRemoveFriendIsMutual() {
        UserDto user1 = userService.addUser(createUser("login1"));
        UserDto user2 = userService.addUser(createUser("login2"));

        userService.addFriend(user1.getId().intValue(), user2.getId().intValue());
        userService.removeFriend(user1.getId().intValue(), user2.getId().intValue());

        assertEquals(0, userService.getFriends(user1.getId().intValue()).size());
        assertEquals(0, userService.getFriends(user2.getId().intValue()).size());
    }

    @Test
    void testGetCommonFriends() {
        UserDto user1 = userService.addUser(createUser("login1"));
        UserDto user2 = userService.addUser(createUser("login2"));
        UserDto common = userService.addUser(createUser("login3"));

        userService.addFriend(user1.getId().intValue(), common.getId().intValue());
        userService.addFriend(user2.getId().intValue(), common.getId().intValue());

        Collection<UserDto> commonFriends =
                userService.getCommonFriends(user1.getId().intValue(), user2.getId().intValue());

        assertEquals(1, commonFriends.size());
        assertEquals(common.getId(), commonFriends.stream().toList().getFirst().getId());
    }

    @Test
    void testAddFriendTwiceDoesNotDuplicate() {
        UserDto user1 = userService.addUser(createUser("login1"));
        UserDto user2 = userService.addUser(createUser("login2"));

        userService.addFriend(user1.getId().intValue(), user2.getId().intValue());
        userService.addFriend(user1.getId().intValue(), user2.getId().intValue());

        assertEquals(1, userService.getFriends(user1.getId().intValue()).size());
    }

    private UserDto createUser(String login) {
        UserDto user = new UserDto();
        user.setEmail(login + "@mail.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
