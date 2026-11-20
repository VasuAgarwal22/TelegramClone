package com.chatnova.api.controller;

import com.chatnova.api.dto.ChatDtos.ErrorResponse;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(NoSuchElementException exception) {
        return new ErrorResponse("NOT_FOUND", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", "));
        return new ErrorResponse("VALIDATION_ERROR", message, Instant.now());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse illegalArgument(IllegalArgumentException exception) {
        return new ErrorResponse("BAD_REQUEST", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ErrorResponse illegalState(IllegalStateException exception) {
        return new ErrorResponse("LOCKED", exception.getMessage(), Instant.now());
    }
}
