package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exeption.ValidationException;

import java.util.List;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;
    private final String sharerUserId = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestBody ItemRequest itemRequest,
                                                @RequestHeader("X-Sharer-User-Id") long userId) {
        if (itemRequest.getDescription() == null || itemRequest.getDescription().equals("")) {
            throw new ValidationException("ошибка в описании");
        }
        log.info("create request");
        return requestClient.createRequest(itemRequest,userId);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestForUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("get request");
        return requestClient.getRequestForUser(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findRequestById(@PathVariable("id") Long requestId,
                                                  @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("find request by id");
        return requestClient.findRequestById(requestId,userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllRequest(@RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "20") Integer size,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        if (size < 1 || from < 0) {
            throw new ValidationException("");
        }
        log.info("find all requests");
        return requestClient.findAllRequest(userId,from,size);
    }
}
