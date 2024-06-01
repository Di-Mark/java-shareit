package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.List;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public User createUser(User user) {
        checkUserForCreate(user);
        try {
            return userRepository.save(user);
        } catch (RuntimeException e) {
            throw new RuntimeException();
        }
    }

    @Transactional
    @Override
    public User patchUser(User user, Long id) {
        checkUserForPatch(user);
        User oldUser = getUser(id);
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }
        return userRepository.save(oldUser);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("такого юзера не существует"));
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private void checkUserForCreate(User user) {
        if (user.getName() == null || user.getName().equals("")) {
            log.info("Имя пользователя не может быть пустым");
            throw new ValidationException("Имя пользователя не может быть пустым");
        } else if (user.getEmail() == null || !user.getEmail().contains("@")) {
            log.info("неправильный формат почты пользователя");
            throw new ValidationException("неправильный формат почты пользователя");
        }
    }

    private void checkUserForPatch(User user) {
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
        }
    }
}
