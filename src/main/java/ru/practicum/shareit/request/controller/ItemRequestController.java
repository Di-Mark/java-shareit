package ru.practicum.shareit.request.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final String sharerUserId = "X-Sharer-User-Id";

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto createRequest(@RequestBody ItemRequest itemRequest,
                                        @RequestHeader(sharerUserId) Long userId) {
        return itemRequestService.createRequest(itemRequest,userId);
    }

    @GetMapping
    public List<ItemRequestDto> getRequestForUser(@RequestHeader(sharerUserId) Long userId){
        return itemRequestService.getRequestForUser(userId);
    }

    @GetMapping("/{id}")
    ItemRequestDto findRequestById(@PathVariable("id") Long requestId, @RequestHeader(sharerUserId) Long userId){
        return itemRequestService.findRequestById(requestId,userId);
    }

    @GetMapping("/all")
    List<ItemRequestDto> findAllRequest(@RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "20") Integer size,
                                        @RequestHeader(sharerUserId) Long userId){
        return itemRequestService.findAllRequest(from,size,userId);
    }
}
