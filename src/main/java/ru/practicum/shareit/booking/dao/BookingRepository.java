package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerOrderByStartDesc(User user);

    List<Booking> findByItemInOrderByStartDesc(List<Item> items);

    List<Booking> findByBooker(User user);

    List<Booking> findByItem(Item item);

    List<Booking> findByItemOrderByStartDesc(Item items);

    List<Booking> findByItemOrderByEndAsc(Item items);

    List<Booking> findByItemOrderByStartAsc(Item items);

    List<Booking> findByItemOrderByEndDesc(Item items);

    List<Booking> findByItemAndBooker(Item item, User booker);
}
