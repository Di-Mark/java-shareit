package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemDao {
    List<Item> getAllItems();

    Item createItem(ItemDto itemDto, Long userId);

    Item patchItem(Item item, Long itemId, Long userId);

    Item getItem(Long id);
}
