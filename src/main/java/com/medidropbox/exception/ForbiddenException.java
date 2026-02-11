package com.medidropbox.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when the user is not allowed to perform the action (e.g. resource belongs to another hospital).
 * Mapped to 403 by GlobalExceptionHandler.
 */
public class ForbiddenException extends ResponseStatusException {

    public ForbiddenException(String reason) {
        super(HttpStatus.FORBIDDEN, reason);
    }
}
