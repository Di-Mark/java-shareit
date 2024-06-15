package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exeption.ValidationException;


@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;
    private final String sharerUserId = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long id) {
        if (itemDto.getName() == null || itemDto.getName().equals("")) {
            throw new ValidationException("имя вещи не может быть пустым или отсутствовать");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().equals("")) {
            throw new ValidationException("описание вещи не может быть пустым или отсутствовать");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("статус вещи не может быть пустым или отсутствовать");
        }
        log.info("create Item");
        return itemClient.createItem(itemDto, id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patchItem(@RequestBody ItemDto item,
                                            @RequestHeader("X-Sharer-User-Id") long userId, @PathVariable("id") Long itemId) {
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
        log.info("patch Item");
        return itemClient.patchItem(item, itemId, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@PathVariable("id") long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("get Item");
        return itemClient.getItem(itemId, userId);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> addComment(@RequestBody Comment comment,
                                             @PathVariable("id") Long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        if (comment.getText() == null || comment.getText().equals("") || itemId == null) {
            throw new ValidationException("");
        }
        log.info("add comment");
        return itemClient.addComment(comment, itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsListForUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(defaultValue = "0") Integer from,
                                                      @RequestParam(defaultValue = "20") Integer size) {
        if (size < 1 || from < 0) {
            throw new ValidationException("");
        }
        log.info("get items list for user");
        return itemClient.getItemsListForUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsForText(@RequestParam("text") String text,
                                                     @RequestParam(defaultValue = "0") Integer from,
                                                     @RequestParam(defaultValue = "20") Integer size,
                                                     @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("search item for text");
        return itemClient.searchItemsForText(text, from, size, userId);
    }


}
