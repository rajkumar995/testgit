package com.medidropbox.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when a requested resource does not exist.
 * Mapped to 404 by GlobalExceptionHandler (priority over generic RuntimeException).
 */
public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(HttpStatus.NOT_FOUND, resourceName + " not found for identifier: " + identifier);
    }
}
