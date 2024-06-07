package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
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
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    @Test
    void createBooking() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        bookingService.createBooking(bookingDto, book.getId());
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.start = :start", Booking.class);
        Booking result = query.setParameter("start", bookingDto.getStart())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(new User(book.getId(), "booker", "booker@email.com")));
        assertThat(result.getItem(),
                equalTo(new Item(item.getId(), "name", "desc",
                        true, new User(ow.getId(), "Пётр", "some@email.com"), null)));
        assertThat(result.getStatus(), equalTo(StatusBooking.WAITING));
        List<Booking> bookings = bookingRepository.findByItemAndBooker(itemRepository.findById(item.getId()).get(),
                userService.getUser(book.getId()));
        Assertions.assertNotNull(bookings);
        bookings = bookingRepository.findByItemOrderByEndDesc(itemRepository.findById(item.getId()).get());
        Assertions.assertNotNull(bookings);
        bookings = bookingRepository.findByItemOrderByStartAsc(itemRepository.findById(item.getId()).get());
        Assertions.assertNotNull(bookings);
        bookings = bookingRepository.findByBooker(
                userService.getUser(book.getId()), PageRequest.of(0, 20)).getContent();
        Assertions.assertNotNull(bookings);
        bookings = bookingRepository
                .findByItemIn(List.of(itemRepository.findById(item.getId()).get()),
                        PageRequest.of(0, 20)).getContent();
        Assertions.assertNotNull(bookings);
    }

    @Test
    void createBookingWithFailBooker() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDto, ow.getId()));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void createBookingWithFailUserId() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, null));
        Assertions.assertEquals(e.getMessage(), "юзер отсутствует");
    }

    @Test
    void createBookingWithFailStatus() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", false);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, book.getId()));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void createBookingWithFailStart() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                null,
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, book.getId()));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void createBookingWithFailDate() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, book.getId()));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void changeStatus() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        Booking booking = bookingService.createBooking(bookingDto, book.getId());
        bookingService.changeStatus(booking.getId(), ow.getId(), true);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.start = :start", Booking.class);
        Booking result = query.setParameter("start", bookingDto.getStart())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(new User(book.getId(), "booker", "booker@email.com")));
        assertThat(result.getItem(),
                equalTo(new Item(item.getId(), "name", "desc",
                        true, new User(ow.getId(), "Пётр", "some@email.com"), null)));
        assertThat(result.getStatus(), equalTo(StatusBooking.APPROVED));
    }

    @Test
    void getBooking() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7,
                        12, 12, 12), book.getId(), item.getId());
        Booking booking = bookingService.createBooking(bookingDto, book.getId());
        Booking result = bookingService.getBooking(booking.getId(), book.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(new User(book.getId(), "booker", "booker@email.com")));
        assertThat(result.getItem(),
                equalTo(new Item(item.getId(), "name", "desc",
                        true, new User(ow.getId(), "Пётр", "some@email.com"), null)));
        assertThat(result.getStatus(), equalTo(StatusBooking.WAITING));
    }

    @Test
    void getBookingForUserByStatusAll() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(book.getId(), "ALL", 0, 20);
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
    void getBookingForUserByStatusAllWithFailPage() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getBookingForUserByStatus(book.getId(), "ALL", -1, 20));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void getBookingForOwnerByStatusAllWithFailPage() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getBookingForOwnerByStatus(book.getId(), "ALL", -1, 20));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void getBookingForOwnerByStatusAllWithFailStatus() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getBookingForOwnerByStatus(book.getId(), "AAA", 0, 20));
        Assertions.assertEquals(e.getMessage(), "Unknown state: AAA");
    }

    @Test
    void getBookingForUserByStatusAllWithFailStatus() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getBookingForUserByStatus(book.getId(), "AAA", 0, 20));
        Assertions.assertEquals(e.getMessage(), "Unknown state: AAA");
    }

    @Test
    void getBookingForUserByStatusCurrent() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 5, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 8, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(book.getId(), "CURRENT", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 3, 6, 12, 12, 12),
                LocalDateTime.of(2024, 4, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(book.getId(), "PAST", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(book.getId(), "FUTURE", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(book.getId(), "WAITING", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.REJECTED);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForUserByStatus(book.getId(), "REJECTED", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(ow.getId(), "ALL", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 5, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 8, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(ow.getId(), "CURRENT", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 3, 6, 12, 12, 12),
                LocalDateTime.of(2024, 4, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(ow.getId(), "PAST", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 7, 6, 12, 12, 12),
                LocalDateTime.of(2024, 7, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(ow.getId(), "FUTURE", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(ow.getId(), "WAITING", 0, 20);
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
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 6, 6, 12, 12, 12),
                LocalDateTime.of(2024, 6, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.REJECTED);
        List<Booking> sourceList = List.of(booking);
        em.persist(booking);
        em.flush();
        List<Booking> targetList = bookingService.getBookingForOwnerByStatus(ow.getId(), "REJECTED", 0, 20);
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
