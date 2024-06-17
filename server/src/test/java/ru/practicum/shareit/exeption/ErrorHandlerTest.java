package ru.practicum.shareit.exeption;


import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

    @Test
    void handleNotFoundException() {
        // Arrange
        ErrorHandler errorHandler = new ErrorHandler();
        NotFoundException notFoundException = new NotFoundException("Not found");
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // Act
        ErrorResponse response = errorHandler.handleNotFoundException(notFoundException);

        // Assert
        assertEquals("Not found", response.getError());
    }

    @Test
    void handleValidationException() {
        // Arrange
        ErrorHandler errorHandler = new ErrorHandler();
        ValidationException validationException = new ValidationException("Validation error");
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // Act
        ErrorResponse response = errorHandler.handleValidationException(validationException);

        // Assert
        assertEquals("Validation error", response.getError());
    }

    @Test
    void handleRuntimeException() {
        // Arrange
        ErrorHandler errorHandler = new ErrorHandler();
        RuntimeException runtimeException = new RuntimeException("Runtime error");
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // Act
        ErrorResponse response = errorHandler.handleRuntimeException(runtimeException);

        // Assert
        assertEquals("Runtime error", response.getError());
    }
}