package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto createRequest(ItemRequest itemRequest, Long userId) {
        if (itemRequest.getDescription() == null || itemRequest.getDescription().equals("")) {
            throw new ValidationException("ошибка в описании");
        }
        itemRequest.setRequestor(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("пользователь не найден")));
        itemRequest.setCreated(LocalDateTime.now());
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getRequestForUser(Long userId) {
        List<ItemRequest> requests = itemRequestRepository
                .findByRequestorOrderByCreatedDesc(userRepository
                        .findById(userId).orElseThrow(() -> new NotFoundException("пользователь не найден")));
        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest itemRequest : requests) {
            ItemRequestDto temp = ItemRequestMapper.toItemRequestDto(itemRequest);
            temp.setItems(itemRepository.findByRequest(itemRequest).stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList()));
            result.add(temp);
        }
        return result;
    }

    @Override
    public ItemRequestDto findRequestById(Long requestId, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(""));
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(""));
        ItemRequestDto result = ItemRequestMapper
                .toItemRequestDto(request);
        result.setItems(itemRepository.findByRequest(request).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public List<ItemRequestDto> findAllRequest(Integer from, Integer size, Long userId) {
        if (size < 1 || from < 0) {
            throw new ValidationException("");
        }
        return itemRequestRepository
                .findAll(PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"))).stream()
                .map(itemRequest -> findRequestById(itemRequest.getId(), userId))
                .filter(itemRequestDto -> itemRequestDto.getId() != userId)
                .collect(Collectors.toList());
    }


}
