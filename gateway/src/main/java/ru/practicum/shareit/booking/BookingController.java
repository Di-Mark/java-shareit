package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.exeption.ValidationException;

import java.time.LocalDateTime;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private final String sharerUserId = "X-Sharer-User-Id";


    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "20") Integer size) {

        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookings(userId, stateParam, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestBody BookItemRequestDto requestDto) {
        if (requestDto.getEnd() == null || requestDto.getStart() == null) {
            throw new ValidationException("");
        }
        if (requestDto.getEnd().isBefore(LocalDateTime.now()) || requestDto.getStart().isBefore(LocalDateTime.now())
                || requestDto.getEnd().isBefore(requestDto.getStart()) || requestDto.getStart()
                .isEqual(requestDto.getEnd())) {
            throw new ValidationException("");
        }
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> changeStatus(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @PathVariable("id") Long bookingId, @RequestParam("approved") Boolean status) {
        log.info("change Status");
        return bookingClient.changeStatus(userId, bookingId, status);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingForOwnerByStatus(@RequestHeader("X-Sharer-User-Id") long userId,
                                                             @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                             @RequestParam(defaultValue = "0") Integer from,
                                                             @RequestParam(defaultValue = "20") Integer size) {

        if (size < 1 || from < 0) {
            throw new ValidationException("");
        }
        log.info("Get booking for owner with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingForOwnerByStatus(userId, stateParam, from, size);
    }
}
