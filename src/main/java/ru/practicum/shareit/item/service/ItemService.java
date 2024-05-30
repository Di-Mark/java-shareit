package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    List<ItemDto> getAllItems();

    ItemDto patchItem(ItemDto item, Long itemId, Long userId);

    ItemDtoBooking getItem(Long id, Long userId);

    List<ItemDtoBooking> getItemsListForUser(Long userId);

    List<ItemDto> searchItemsForText(String text);

    CommentDto addComment(Comment comment, Long itemId, Long userId);
}
