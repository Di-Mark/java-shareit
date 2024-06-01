package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final String sharerUserId = "X-Sharer-User-Id";

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking createBooking(@RequestBody BookingDto booking, @RequestHeader(sharerUserId) Long id) {
        return bookingService.createBooking(booking, id);
    }

    @PatchMapping("/{id}")
    public Booking changeStatus(@RequestHeader(sharerUserId) Long userId,
                                @PathVariable("id") Long bookingId, @RequestParam("approved") Boolean status) {
        return bookingService.changeStatus(bookingId, userId, status);
    }

    @GetMapping("/{id}")
    public Booking getBooking(@RequestHeader(sharerUserId) Long userId, @PathVariable("id") Long bookingId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<Booking> getBookingForUserByStatus(@RequestHeader(sharerUserId) Long userId,
                                                   @RequestParam(value = "state", required = false) String status) {
        return bookingService.getBookingForUserByStatus(userId, status);
    }

    @GetMapping("/owner")
    public List<Booking> getBookingForOwnerByStatus(@RequestHeader(sharerUserId) Long userId,
                                                    @RequestParam(value = "state", required = false) String status) {
        return bookingService.getBookingForOwnerByStatus(userId, status);
    }
}
