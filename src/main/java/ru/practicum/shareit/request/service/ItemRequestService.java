package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;


public interface ItemRequestService {
    ItemRequestDto createRequest(ItemRequest itemRequest, Long userId);

    List<ItemRequestDto> getRequestForUser(Long userId);

    ItemRequestDto findRequestById(Long requestId, Long userId);

    List<ItemRequestDto> findAllRequest(Integer from, Integer size, Long userId);
}
