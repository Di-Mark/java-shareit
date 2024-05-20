package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;


import java.util.List;

public interface UserDao {
    List<User> findAllUsers();

    User createUser(User user);

    User patchUser(User user, Long id);

    User getUser(Long id);

    void deleteUser(Long id);
}
