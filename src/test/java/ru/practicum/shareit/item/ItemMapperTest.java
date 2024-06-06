package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {

    @Test
    void toItemDto() {
        Item item = new Item(1L, "name", "description", true, null, null);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
    }

    @Test
    void toItem() {
        ItemDto itemDto = new ItemDto(1L, "name", "desc", true, null);
        Item item = ItemMapper.toItem(itemDto);
        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
    }

    @Test
    void toItemDtoBooking() {
        Item item = new Item(1L, "name", "description", true, null, null);
        ItemDtoBooking itemDtoBooking = ItemMapper.toItemDtoBooking(item);
        assertEquals(item.getId(), itemDtoBooking.getId());
        assertEquals(item.getName(), itemDtoBooking.getName());
        assertEquals(item.getDescription(), itemDtoBooking.getDescription());
    }
}
