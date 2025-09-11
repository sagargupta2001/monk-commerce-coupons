package com.monk.commerce.exceptions;

import com.monk.commerce.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandle_ResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        ResponseEntity<String> response = handler.handle(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody());
    }

    @Test
    void testHandleGeneric_Exception() {
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<String> response = handler.handleGeneric(ex);

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Something went wrong", response.getBody());
    }
}