package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapping;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Booking createBooking(BookingDto booking, Long userId) {
        checkBookingForCreate(booking, userId);
        Booking result = BookingMapping.toBooking(booking);
        result.setItem(itemRepository.findById(booking.getItemId()).orElseThrow(() -> new NotFoundException("")));
        result.setBooker(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("")));
        if (result.getBooker().equals(result.getItem().getOwner())) {
            throw new NotFoundException("");
        }
        result.setStatus(StatusBooking.WAITING);
        return bookingRepository.save(result);
    }

    @Transactional
    @Override
    public Booking changeStatus(Long bookingId, Long userId, Boolean status) {
        if (userId == null || bookingId == null || status == null) {
            throw new ValidationException("");
        }
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException(""));
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("");
        }
        if (booking.getStatus().name().equals("APPROVED")) {
            throw new ValidationException("");
        }
        if (status) {
            booking.setStatus(StatusBooking.APPROVED);
        } else booking.setStatus(StatusBooking.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long bookingId, Long userId) {
        if (userId == null || bookingId == null) {
            throw new ValidationException("");
        }
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException(""));
        if (!Objects.equals(booking.getBooker().getId(), userId)
                && !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("");
        }
        return booking;
    }

    @Override
    public List<Booking> getBookingForUserByStatus(Long user, String status) {
        User booker = userRepository.findById(user).orElseThrow(() -> new NotFoundException(""));
        if (status == null || status.equals("") || status.equals(StatusBooking.ALL.name())) {
            return bookingRepository.findByBookerOrderByStartDesc(booker);
        }
        if (status.equals(StatusBooking.CURRENT.name())) {
            return bookingRepository.findByBookerOrderByStartDesc(booker).stream()
                    .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now())).collect(Collectors.toList());
        }
        if (status.equals(StatusBooking.PAST.name())) {
            return bookingRepository.findByBookerOrderByStartDesc(booker).stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                    .collect(Collectors.toList());
        }
        if (status.equals(StatusBooking.FUTURE.name())) {
            return bookingRepository.findByBookerOrderByStartDesc(booker).stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
        }
        if (status.equals(StatusBooking.WAITING.name())) {
            return bookingRepository.findByBookerOrderByStartDesc(booker).stream()
                    .filter(booking -> booking.getStatus().name().equals(StatusBooking.WAITING.name()))
                    .collect(Collectors.toList());
        }
        if (status.equals(StatusBooking.REJECTED.name())) {
            return bookingRepository.findByBookerOrderByStartDesc(booker).stream()
                    .filter(booking -> booking.getStatus().name().equals(StatusBooking.REJECTED.name()))
                    .collect(Collectors.toList());
        }
        throw new ValidationException("Unknown state: " + status);
    }

    @Override
    public List<Booking> getBookingForOwnerByStatus(Long userId, String status) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(""));
        List<Item> itemsOwner = itemRepository.findByOwner(owner);
        if (status == null || status.equals("") || status.equals("ALL")) {
            return bookingRepository.findByItemInOrderByStartDesc(itemsOwner);
        }
        if (status.equals("CURRENT")) {
            return bookingRepository.findByItemInOrderByStartDesc(itemsOwner).stream()
                    .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
        }
        if (status.equals("PAST")) {
            return bookingRepository.findByItemInOrderByStartDesc(itemsOwner).stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                    .collect(Collectors.toList());
        }
        if (status.equals("FUTURE")) {
            return bookingRepository.findByItemInOrderByStartDesc(itemsOwner).stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
        }
        if (status.equals("WAITING")) {
            return bookingRepository.findByItemInOrderByStartDesc(itemsOwner).stream()
                    .filter(booking -> booking.getStatus().name().equals("WAITING"))
                    .collect(Collectors.toList());
        }
        if (status.equals("REJECTED")) {
            return bookingRepository.findByItemInOrderByStartDesc(itemsOwner).stream()
                    .filter(booking -> booking.getStatus().name().equals("REJECTED"))
                    .collect(Collectors.toList());
        }
        throw new ValidationException("Unknown state: " + status);
    }


    private void checkBookingForCreate(BookingDto booking, Long userId) {
        if (userId == null) {
            throw new ValidationException("юзер отсутствует");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("такого пользователя не существует"));

        if (booking.getItemId() == null) {
            throw new NotFoundException("такого айтема не существует");
        }
        Item item = itemRepository.findById(booking.getItemId()).orElseThrow(() -> new NotFoundException(""));
        if (!item.getAvailable()) {
            throw new ValidationException("");
        }

        if (booking.getStart() == null || booking.getEnd() == null) {
            throw new ValidationException("");
        }
        if (booking.getEnd().isBefore(LocalDateTime.now()) || booking.getStart().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(booking.getStart()) || booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("");
        }
    }


}
