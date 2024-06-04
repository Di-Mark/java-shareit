package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(BookingDto booking, Long userId);

    Booking changeStatus(Long bookingId, Long userId, Boolean status);

    Booking getBooking(Long bookingId, Long userId);

    List<Booking> getBookingForUserByStatus(Long user, String status, Integer from, Integer size);

    List<Booking> getBookingForOwnerByStatus(Long userId, String status,Integer from, Integer size);
}
