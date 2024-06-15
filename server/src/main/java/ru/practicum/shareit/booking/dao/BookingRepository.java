package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByBooker(User user, Pageable pageable);

    Page<Booking> findByItemIn(List<Item> items, Pageable pageable);


    List<Booking> findByItemOrderByStartAsc(Item items);

    List<Booking> findByItemOrderByEndDesc(Item items);

    List<Booking> findByItemAndBooker(Item item, User booker);
}
