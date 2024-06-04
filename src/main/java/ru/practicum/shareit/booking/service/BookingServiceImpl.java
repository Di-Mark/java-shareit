package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.Optional;
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
    public List<Booking> getBookingForUserByStatus(Long user, String status,Integer from, Integer size) {
        if (size < 1 || from < 0){
            throw new ValidationException("");
        }
        User booker = userRepository.findById(user).orElseThrow(() -> new NotFoundException(""));
        if (status == null || status.equals("") || status.equals(StatusBooking.ALL.name())) {
            List<Booking> res = bookingRepository
                    .findByBooker(booker,PageRequest.of(from,size, Sort.by(Sort.Direction.DESC,"start")))
                    .getContent();
            if(res.size() == 0){
                List<Booking> answer =  bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(0,20, Sort.by(Sort.Direction.ASC,"start")))
                        .getContent();
                return bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(1,answer.size() - 1,
                                        Sort.by(Sort.Direction.DESC,"start")))
                        .getContent();
            }
            return res;
        }
        if (status.equals(StatusBooking.CURRENT.name())) {
            List<Booking> res = bookingRepository
                    .findByBooker(booker,PageRequest.of(from,size, Sort.by(Sort.Direction.DESC,"start")))
                    .stream()
                    .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now())).collect(Collectors.toList());
            if(res.size() == 0){
                List<Booking> answer = bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(0,20, Sort.by(Sort.Direction.ASC,"start")))
                        .stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                                && booking.getEnd().isAfter(LocalDateTime.now())).collect(Collectors.toList());
                return bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(1,answer.size() - 1,
                                        Sort.by(Sort.Direction.DESC,"start")))
                        .stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                                && booking.getEnd().isAfter(LocalDateTime.now())).collect(Collectors.toList());
            }
            return res;
        }
        if (status.equals(StatusBooking.PAST.name())) {
            List<Booking> res = bookingRepository
                    .findByBooker(booker,PageRequest.of(from,size, Sort.by(Sort.Direction.DESC,"start")))
                    .stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository
                        .findByBooker(booker,PageRequest.of(0,20, Sort.by(Sort.Direction.ASC,"start")))
                        .stream()
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                return bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(1,answer.size() - 1,
                                        Sort.by(Sort.Direction.DESC,"start")))
                        .stream()
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());

            }
            return res;
        }
        if (status.equals(StatusBooking.FUTURE.name())) {
            List<Booking> res = bookingRepository
                    .findByBooker(booker,PageRequest.of(from,size, Sort.by(Sort.Direction.DESC,"start")))
                    .stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository
                        .findByBooker(booker,PageRequest.of(0,20,
                                Sort.by(Sort.Direction.ASC,"start")))
                        .stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                return bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(1,answer.size() - 1,
                                        Sort.by(Sort.Direction.DESC,"start")))
                        .stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            }
            return res;
        }
        if (status.equals(StatusBooking.WAITING.name())) {
            List<Booking> res = bookingRepository
                    .findByBooker(booker,PageRequest.of(from,size, Sort.by(Sort.Direction.DESC,"start")))
                    .stream()
                    .filter(booking -> booking.getStatus().name().equals(StatusBooking.WAITING.name()))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(0,20, Sort.by(Sort.Direction.ASC,"start")))
                        .stream()
                        .filter(booking -> booking.getStatus().name().equals(StatusBooking.WAITING.name()))
                        .collect(Collectors.toList());
                return bookingRepository
                        .findByBooker(booker,PageRequest.of(0,answer.size() - 1, Sort.by(Sort.Direction.DESC,"start")))
                        .stream()
                        .filter(booking -> booking.getStatus().name().equals(StatusBooking.WAITING.name()))
                        .collect(Collectors.toList());
            }
            return res;
        }
        if (status.equals(StatusBooking.REJECTED.name())) {
            List<Booking> res = bookingRepository
                    .findByBooker(booker,PageRequest.of(from,size, Sort.by(Sort.Direction.DESC,"start")))
                    .stream()
                    .filter(booking -> booking.getStatus().name().equals(StatusBooking.REJECTED.name()))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(0,20, Sort.by(Sort.Direction.ASC,"start")))
                        .stream()
                        .filter(booking -> booking.getStatus().name().equals(StatusBooking.REJECTED.name()))
                        .collect(Collectors.toList());
                return bookingRepository
                        .findByBooker(booker,
                                PageRequest.of(1,answer.size() - 1,
                                        Sort.by(Sort.Direction.DESC,"start")))
                        .stream()
                        .filter(booking -> booking.getStatus().name().equals(StatusBooking.REJECTED.name()))
                        .collect(Collectors.toList());
            }
            return res;
        }
        throw new ValidationException("Unknown state: " + status);
    }

    @Override
    public List<Booking> getBookingForOwnerByStatus(Long userId, String status,Integer from, Integer size) {
        if (size < 1 || from < 0){
            throw new ValidationException("");
        }
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(""));
        List<Item> itemsOwner = itemRepository.findByOwner(owner, PageRequest.of(0,20)).stream()
                .collect(Collectors.toList());
        if (status == null || status.equals("") || status.equals("ALL")) {
            List<Booking> res = bookingRepository
                    .findByItemIn(itemsOwner,PageRequest
                            .of(from,size, Sort.by(Sort.Direction.DESC,"start"))).getContent();
            if (res.size() == 0){
                List<Booking> answer = bookingRepository
                        .findByItemIn(itemsOwner,PageRequest
                                .of(0,20, Sort.by(Sort.Direction.ASC,"start"))).getContent();
                return bookingRepository
                        .findByItemIn(itemsOwner,PageRequest
                                .of(1,answer.size() - 1,
                                        Sort.by(Sort.Direction.DESC,"start"))).getContent();
            }
            return res;
        }
        if (status.equals("CURRENT")) {
            List<Booking> res = bookingRepository.findByItemIn(itemsOwner,PageRequest
                            .of(from,size, Sort.by(Sort.Direction.DESC,"start"))).stream()
                    .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(0,20, Sort.by(Sort.Direction.ASC,"start"))).stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                                && booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                return bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(1,answer.size() - 1, Sort.by(Sort.Direction.DESC,"start"))).stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now())
                                && booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            }
            return res;
        }
        if (status.equals("PAST")) {
            List<Booking> res = bookingRepository.findByItemIn(itemsOwner,PageRequest
                            .of(from,size, Sort.by(Sort.Direction.DESC,"start"))).stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(0,20, Sort.by(Sort.Direction.ASC,"start"))).stream()
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                return bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(1,answer.size() - 1, Sort.by(Sort.Direction.DESC,"start"))).stream()
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            }
            return res;
        }
        if (status.equals("FUTURE")) {
            List<Booking> res = bookingRepository.findByItemIn(itemsOwner,PageRequest
                            .of(from,size, Sort.by(Sort.Direction.DESC,"start"))).stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(0,20, Sort.by(Sort.Direction.ASC,"start"))).stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                return bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(1,answer.size() - 1, Sort.by(Sort.Direction.DESC,"start"))).stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            }
            return res;
        }
        if (status.equals("WAITING")) {
            List<Booking> res = bookingRepository.findByItemIn(itemsOwner,PageRequest
                            .of(from,size, Sort.by(Sort.Direction.DESC,"start"))).stream()
                    .filter(booking -> booking.getStatus().name().equals("WAITING"))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(0,20, Sort.by(Sort.Direction.ASC,"start"))).stream()
                        .filter(booking -> booking.getStatus().name().equals("WAITING"))
                        .collect(Collectors.toList());
                return bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(1,answer.size() - 1,
                                        Sort.by(Sort.Direction.DESC,"start"))).stream()
                        .filter(booking -> booking.getStatus().name().equals("WAITING"))
                        .collect(Collectors.toList());
            }
            return res;
        }
        if (status.equals("REJECTED")) {
            List<Booking> res = bookingRepository.findByItemIn(itemsOwner,PageRequest
                            .of(from,size, Sort.by(Sort.Direction.DESC,"start"))).stream()
                    .filter(booking -> booking.getStatus().name().equals("REJECTED"))
                    .collect(Collectors.toList());
            if (res.size() == 0){
                List<Booking> answer = bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(0,20, Sort.by(Sort.Direction.ASC,"start"))).stream()
                        .filter(booking -> booking.getStatus().name().equals("REJECTED"))
                        .collect(Collectors.toList());
                return bookingRepository.findByItemIn(itemsOwner,PageRequest
                                .of(1,answer.size() - 1, Sort.by(Sort.Direction.DESC,"start"))).stream()
                        .filter(booking -> booking.getStatus().name().equals("REJECTED"))
                        .collect(Collectors.toList());
            }
            return res;
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
