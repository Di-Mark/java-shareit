package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;


@RestController
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;
    private final String sharerUserId = "X-Sharer-User-Id";


    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestBody ItemDto itemDto, @RequestHeader(sharerUserId) Long id) {
        return itemService.createItem(itemDto, id);
    }

    @PatchMapping("/{id}")
    public ItemDto patchItem(@RequestBody ItemDto item,
                             @RequestHeader(sharerUserId) Long userId, @PathVariable("id") Long itemId) {
        return itemService.patchItem(item, itemId, userId);
    }

    @GetMapping("/{id}")
    public ItemDtoBooking getItem(@PathVariable("id") Long itemId, @RequestHeader(sharerUserId) Long userId) {
        return itemService.getItem(itemId, userId);
    }

    @PostMapping("/{id}/comment")
    public CommentDto addComment(@RequestBody Comment comment,
                                 @PathVariable("id") Long itemId, @RequestHeader(sharerUserId) Long userId) {
        return itemService.addComment(comment, itemId, userId);
    }

    @GetMapping
    public List<ItemDtoBooking> getItemsListForUser(@RequestHeader(sharerUserId) Long userId,
                                                    @RequestParam(defaultValue = "0") Integer from,
                                                    @RequestParam(defaultValue = "20") Integer size) {
        return itemService.getItemsListForUser(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsForText(@RequestParam("text") String text,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "20") Integer size) {
        return itemService.searchItemsForText(text, from, size);
    }
}
