package com.medidropbox.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown for invalid business input (e.g. insufficient stock, duplicate).
 * Mapped to 400 by GlobalExceptionHandler.
 */
public class BadRequestException extends ResponseStatusException {

    public BadRequestException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
