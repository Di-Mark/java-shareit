package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {
    private final EntityManager em;
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;


    @Test
    void createBooking() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 3L);
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12), 4L, 2L);
        bookingService.createBooking(bookingDto, 4L);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.start = :start", Booking.class);
        Booking result = query.setParameter("start", bookingDto.getStart())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(new User(4L, "booker", "booker@email.com")));
        assertThat(result.getItem(),
                equalTo(new Item(2L, "name", "desc",
                        true, new User(3L, "Пётр", "some@email.com"), null)));
        assertThat(result.getStatus(), equalTo(StatusBooking.WAITING));
    }

    @Test
    void changeStatus() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 17L);
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12), 18L, 9L);
        bookingService.createBooking(bookingDto, 18L);
        bookingService.changeStatus(9L, 17L, true);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.start = :start", Booking.class);
        Booking result = query.setParameter("start", bookingDto.getStart())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(new User(18L, "booker", "booker@email.com")));
        assertThat(result.getItem(),
                equalTo(new Item(9L, "name", "desc",
                        true, new User(17L, "Пётр", "some@email.com"), null)));
        assertThat(result.getStatus(), equalTo(StatusBooking.APPROVED));
    }

    @Test
    void getBooking() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 25L);
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12), 26L, 13L);
        bookingService.createBooking(bookingDto, 26L);
        Booking result = bookingService.getBooking(13L, 26L);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(new User(26L, "booker", "booker@email.com")));
        assertThat(result.getItem(),
                equalTo(new Item(13L, "name", "desc",
                        true, new User(25L, "Пётр", "some@email.com"), null)));
        assertThat(result.getStatus(), equalTo(StatusBooking.WAITING));
    }

    @Test
    void getBookingForUserByStatusAll() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 29L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(30L, "booker", "booker@email.com"),
                new Item(15L, "name", "desc", true,
                        new User(29L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(30L, "ALL", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForUserByStatusCurrent() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 7L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 5, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(8L, "booker", "booker@email.com"),
                new Item(4L, "name", "desc", true,
                        new User(7L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(8L, "CURRENT", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForUserByStatusPast() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 27L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 3, 6, 12, 12, 12),
                LocalDateTime.of(2024, 4, 7, 12, 12, 12),
                new User(28L, "booker", "booker@email.com"),
                new Item(14L, "name", "desc", true,
                        new User(27L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(28L, "PAST", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForUserByStatusFuture() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 19L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(20L, "booker", "booker@email.com"),
                new Item(10L, "name", "desc", true,
                        new User(19L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(20L, "FUTURE", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForUserByStatusWaiting() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 5L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(6L, "booker", "booker@email.com"),
                new Item(3L, "name", "desc", true,
                        new User(5L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(6L, "WAITING", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForUserByStatusRejected() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 11L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(12L, "booker", "booker@email.com"),
                new Item(6L, "name", "desc", true,
                        new User(11L, "Пётр", "some@email.com"), null),
                StatusBooking.REJECTED);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(12L, "REJECTED", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForOwnerByStatusAll() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 9L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(10L, "booker", "booker@email.com"),
                new Item(5L, "name", "desc", true,
                        new User(9L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(9L, "ALL", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForOwnerByStatusCurrent() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 23L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 5, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(24L, "booker", "booker@email.com"),
                new Item(12L, "name", "desc", true,
                        new User(23L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(23L, "CURRENT", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForOwnerByStatusPast() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 15L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 3, 6, 12, 12, 12),
                LocalDateTime.of(2024, 4, 7, 12, 12, 12),
                new User(16L, "booker", "booker@email.com"),
                new Item(8L, "name", "desc", true,
                        new User(15L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(15L, "PAST", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForOwnerByStatusFuture() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 13L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(14L, "booker", "booker@email.com"),
                new Item(7L, "name", "desc", true,
                        new User(13L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(13L, "FUTURE", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForOwnerByStatusWaiting() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        itemService.createItem(itemDto, 21L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(22L, "booker", "booker@email.com"),
                new Item(11L, "name", "desc", true,
                        new User(21L, "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(21L, "WAITING", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingForOwnerByStatusRejected() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);

        itemService.createItem(itemDto, 1L);
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(2L, "booker", "booker@email.com"),
                new Item(1L, "name", "desc", true,
                        new User(1L, "Пётр", "some@email.com"), null),
                StatusBooking.REJECTED);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(1L, "REJECTED", 0, 20);
        assertThat(targetList, hasSize(1));
        for (Booking sourceBooking : sourceList) {
            assertThat(targetList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }


    private ItemDto makeItemDto(String name, String description, Boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private BookingDto makeBookingDto(LocalDateTime start, LocalDateTime end, Long bookerId, Long itemId) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setBooker(bookerId);
        bookingDto.setItemId(itemId);
        return bookingDto;
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, User booker, Item item, StatusBooking status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(status);
        return booking;
    }
}
