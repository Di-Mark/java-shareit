package ru.practicum.shareit.item.dao;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.*;

@Getter
@Slf4j
@Component
public class ItemDtoImpl implements ItemDao {
    private Map<Long, Item> itemMap = new HashMap<>();
    private Long id = 1L;
    private final UserDao userDao;

    @Autowired
    public ItemDtoImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(itemMap.values());
    }

    @Override
    public Item createItem(ItemDto itemDto, Long userId) {
        checkItemForCreate(itemDto, userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setId(id);
        item.setOwner(userDao.getUser(userId));
        itemMap.put(id, item);
        id++;
        log.info("предмет успешно создан");
        return item;
    }

    @Override
    public Item patchItem(Item item, Long itemId, Long userId) {
        checkItemForPatch(item, itemId, userId);
        if (item.getName() != null) {
            itemMap.get(itemId).setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemMap.get(itemId).setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemMap.get(itemId).setAvailable(item.getAvailable());
        }
        log.info("пользователь успешно обновлен");
        return itemMap.get(itemId);
    }

    @Override
    public Item getItem(Long id) {
        if (itemMap.containsKey(id)) {
            log.info("пользователь успешно найден");
            return itemMap.get(id);
        } else throw new NotFoundException("пользователя не существует");
    }

    private void checkItemForCreate(ItemDto itemDto, Long userId) {
        if (userId == null) {
            throw new ValidationException("нет id владельца вещи");
        }
        userDao.getUser(userId);
        if (itemDto.getName() == null || itemDto.getName().equals("")) {
            throw new ValidationException("имя вещи не может быть пустым или отсутствовать");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().equals("")) {
            throw new ValidationException("описание вещи не может быть пустым или отсутствовать");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("статус вещи не может быть пустым или отсутствовать");
        }
    }

    private void checkItemForPatch(Item item, Long idItem, Long userId) {
        if (userId == null) {
            throw new ValidationException("отсутствует id владельца вещи");
        }
        userDao.getUser(userId);
        getItem(idItem);
        if (getItem(idItem).getOwner().getId() != userId) {
            throw new NotFoundException("только владелец вещи может вносить изменения");
        }
        if (item.getName() != null) {
            if (item.getName().equals("")) {
                throw new ValidationException("имя вещи не может быть пустым или отсутствовать");
            }
        }
        if (item.getDescription() != null) {
            if (item.getDescription().equals("")) {
                throw new ValidationException("описание вещи не может быть пустым или отсутствовать");
            }
        }
    }
}
