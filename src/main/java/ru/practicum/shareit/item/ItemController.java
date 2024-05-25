package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;



@RestController
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemDao itemDao;
    private final String HEADER = "X-Sharer-User-Id";


    @Autowired
    public ItemController(ItemService itemService, ItemDao itemDao) {
        this.itemService = itemService;
        this.itemDao = itemDao;
    }

    @PostMapping
    public Item createItem(@RequestBody ItemDto itemDto, @RequestHeader(HEADER) Long id) {
        return itemDao.createItem(itemDto, id);
    }

    @PatchMapping("/{id}")
    public Item patchItem(@RequestBody Item item,
                          @RequestHeader(HEADER) Long userId, @PathVariable("id") Long itemId) {
        return itemDao.patchItem(item, itemId, userId);
    }

    @GetMapping("/{id}")
    public Item getItem(@PathVariable("id") Long itemId) {
        return itemDao.getItem(itemId);
    }

    @GetMapping
    public List<Item> getItemsListForUser(@RequestHeader(HEADER) Long userId) {
        return itemService.getItemsListForUser(userId);
    }

    @GetMapping("/search")
    public List<Item> searchItemsForText(@RequestParam("text") String text) {
        return itemService.searchItemsForText(text);
    }
}
