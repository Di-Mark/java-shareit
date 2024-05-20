package ru.practicum.shareit.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserDaoImpl implements UserDao {
    private Map<Long, User> userMap = new HashMap<>();
    private Long id = 1L;

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User createUser(User user) {
        checkUserForCreate(user);
        user.setId(id);
        userMap.put(id, user);
        id++;
        log.info("Пользователь успешно создан");
        return user;
    }

    @Override
    public User patchUser(User user, Long id) {
        checkUserForPatch(user, id);
        if (user.getName() != null) {
            userMap.get(id).setName(user.getName());
        }
        if (user.getEmail() != null) {
            userMap.get(id).setEmail(user.getEmail());
        }
        return userMap.get(id);
    }

    @Override
    public User getUser(Long id) {
        if (userMap.containsKey(id)) {
            log.info("Пользователь успешно найден");
            return userMap.get(id);
        } else {
            log.info("Такого пользователя не существует");
            throw new NotFoundException("Такого пользователя нет");
        }
    }

    @Override
    public void deleteUser(Long id) {
        userMap.remove(id);
    }

    private void checkUserForCreate(User user) {
        if (user.getName().equals("") || user.getName() == null) {
            log.info("Имя пользователя не может быть пустым");
            throw new ValidationException("Имя пользователя не может быть пустым");
        } else if (user.getEmail() == null || !user.getEmail().contains("@")) {
            log.info("неправильный формат почты пользователя");
            throw new ValidationException("неправильный формат почты пользователя");
        } else {
            for (User tempUser : userMap.values()) {
                if (user.getEmail().equals(tempUser.getEmail())) {
                    log.info("Пользователь с данной почтой уже существует");
                    throw new RuntimeException();
                }
            }
        }
    }

    private void checkUserForPatch(User user, Long id) {
        if (user.getName() != null) {
            if (user.getName().equals("")) {
                log.info("Имя пользователя не может быть пустым");
                throw new ValidationException("Имя пользователя не может быть пустым");
            }
        }
        if (user.getEmail() != null) {
            if (!user.getEmail().contains("@")) {
                log.info("неправильный формат почты пользователя");
                throw new ValidationException("неправильный формат почты пользователя");
            }
            for (User tempUser : userMap.values()) {
                if (tempUser.getId() != id) {
                    if (user.getEmail().equals(tempUser.getEmail())) {
                        log.info("Пользователь с данной почтой уже существует");
                        throw new RuntimeException();
                    }
                }
            }
        }
    }
}
