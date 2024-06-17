package ru.practicum.shareit.user.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;

import java.util.List;


@Transactional(readOnly = true)
public interface UserService {
    List<User> findAllUsers();

    @Transactional
    User createUser(User user);

    @Transactional
    User patchUser(User user, Long id);

    User getUser(Long id);

    @Transactional
    void deleteUser(Long id);
}
