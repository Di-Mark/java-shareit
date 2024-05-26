package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getItemsListForUser(Long userId);

    List<Item> searchItemsForText(String text);
}
