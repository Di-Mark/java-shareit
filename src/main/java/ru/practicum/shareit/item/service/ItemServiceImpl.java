package ru.practicum.shareit.item.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;

    @Autowired
    public ItemServiceImpl(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    @Override
    public List<Item> getItemsListForUser(Long userId) {
        List<Item> result = new ArrayList<>();
        for (Item item : itemDao.getAllItems()) {
            if (Objects.equals(item.getOwner().getId(), userId)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public List<Item> searchItemsForText(String text) {
        List<Item> result = new ArrayList<>();
        if (text.equals("")) {
            return result;
        }
        for (Item item : itemDao.getAllItems()) {
            if ((item.getName().toUpperCase().contains(text.toUpperCase()) ||
                    item.getDescription().toUpperCase().contains(text.toUpperCase())) && item.getAvailable() == true) {
                result.add(item);
            }
        }
        return result;
    }
}
