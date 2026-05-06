package com.inkWell.auth.exception;

import com.inkWell.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleUserAlreadyExists() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Already exists");
        HttpServletRequest request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).setRequestURI("/test");

        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Already exists", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleGlobalException() {
        Exception ex = new Exception("Global error");
        HttpServletRequest request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).setRequestURI("/global");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred: Global error", response.getBody().getMessage());
    }
}
