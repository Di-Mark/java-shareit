package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingTest {

    @Test
    void equalTest() {
        Booking booking1 = new Booking(1L,
                LocalDateTime.of(2010, 12, 12, 12, 21, 12),
                LocalDateTime.of(2010, 12, 12, 12, 21, 12),
                null, null, StatusBooking.WAITING);
        Booking booking2 = new Booking(1L,
                LocalDateTime.of(2010, 12, 12, 12, 21, 12),
                LocalDateTime.of(2010, 12, 12, 12, 21, 12),
                null, null, StatusBooking.WAITING);
        assertEquals(booking1.equals(booking2), true);
    }
}
