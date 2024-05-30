package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        checkItemForCreate(itemDto, userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userRepository.findById(userId).get());
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto patchItem(ItemDto item, Long itemId, Long userId) {
        checkItemForPatch(item, itemId, userId);
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует"));
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(oldItem));
    }

    @Override
    public ItemDtoBooking getItem(Long id, Long userId) {
        Item item = itemRepository.findById(id).
                orElseThrow(() -> new NotFoundException("такого айтема не существует"));
        ItemDtoBooking result = ItemMapper.toItemDtoBooking(item);
        if (Objects.equals(item.getOwner().getId(), userId)) {
            List<Booking> test = bookingRepository.findByItemOrderByStartAsc(item);
            Optional<Booking> tempNext = bookingRepository.findByItemOrderByStartAsc(item).stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .filter(booking -> !booking.getStatus().equals(StatusBooking.REJECTED)).findFirst();
            if (tempNext.isPresent()) {
                BookingDtoForItem bookingDtoForItem
                        = new BookingDtoForItem(tempNext.get().getId(), tempNext.get().getBooker().getId());
                result.setNextBooking(bookingDtoForItem);
            } else result.setNextBooking(null);
            Optional<Booking> tempEnd = bookingRepository.findByItemOrderByEndDesc(item).stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())
                            || booking.getStart().isBefore(LocalDateTime.now()))
                    .filter(booking -> !booking.getStatus().equals(StatusBooking.REJECTED)).findFirst();
            if (tempEnd.isPresent()) {
                BookingDtoForItem bookingDtoForItem
                        = new BookingDtoForItem(tempEnd.get().getId(), tempEnd.get().getBooker().getId());
                result.setLastBooking(bookingDtoForItem);
            } else result.setLastBooking(null);
        }
        result.setComments(commentRepository.findByItem(item)
                .stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        return result;
    }

    @Override
    public List<ItemDtoBooking> getItemsListForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("такого пользователя нет"));
        return itemRepository.findByOwner(user).stream()
                .map(item -> getItem(item.getId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItemsForText(String text) {
        if (text == null || text.equals("")) {
            return new ArrayList<>();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .filter(ItemDto::getAvailable)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(Comment comment, Long itemId, Long userId) {
        if (comment.getText() == null || comment.getText().equals("") || itemId == null || userId == null) {
            throw new ValidationException("");
        }
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(""));
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(""));
        List<Booking> testList = bookingRepository.findByItemAndBooker(item, user).stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (testList.size() != 0) {
            comment.setItem(item);
            comment.setAuthor(user);
            comment.setCreated(LocalDateTime.now());
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        } else throw new ValidationException("");
    }

    private void checkItemForCreate(ItemDto itemDto, Long userId) {
        if (userId == null) {
            throw new ValidationException("нет id владельца вещи");
        }
        if (itemDto.getName() == null || itemDto.getName().equals("")) {
            throw new ValidationException("имя вещи не может быть пустым или отсутствовать");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().equals("")) {
            throw new ValidationException("описание вещи не может быть пустым или отсутствовать");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("статус вещи не может быть пустым или отсутствовать");
        }
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("такого пользователя не существует"));
    }

    private void checkItemForPatch(ItemDto item, Long idItem, Long userId) {
        if (userId == null) {
            throw new ValidationException("отсутствует id владельца вещи");
        }
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
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("такого пользователя не существует"));
        if (!Objects.equals(itemRepository.findById(idItem)
                .orElseThrow(() -> new NotFoundException("")).getOwner().getId(), userId)) {
            throw new NotFoundException("только владелец вещи может вносить изменения");
        }
    }
}
