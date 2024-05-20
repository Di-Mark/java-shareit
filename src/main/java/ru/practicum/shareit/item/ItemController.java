package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemDao itemDao;

    @Autowired
    public ItemController(ItemService itemService, ItemDao itemDao) {
        this.itemService = itemService;
        this.itemDao = itemDao;
    }

    @PostMapping
    public Item createItem(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long id) {
        return itemDao.createItem(itemDto, id);
    }

    @PatchMapping("/{id}")
    public Item patchItem(@RequestBody Item item,
                          @RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable("id") Long itemId) {
        return itemDao.patchItem(item, itemId, userId);
    }

    @GetMapping("/{id}")
    public Item getItem(@PathVariable("id") Long itemId) {
        return itemDao.getItem(itemId);
    }

    @GetMapping
    public List<Item> getItemsListForUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemsListForUser(userId);
    }

    @GetMapping("/search")
    public List<Item> searchItemsForText(@RequestParam("text") String text) {
        return itemService.searchItemsForText(text);
    }
}
