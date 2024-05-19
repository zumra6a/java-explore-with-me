package ru.practicum.ewm.exception;

import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({NoSuchElementException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoSuchElementException(Exception e) {
        log.debug("Element not found exception", e);

        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.toString())
                .message(e.getMessage())
                .reason("The required object was not found.")
                .build();
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerValidationException(Exception e) {
        log.debug("Validation exception {}", e);

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .message(e.getMessage())
                .reason("Incorrect parameters")
                .build();
    }

    @ExceptionHandler({
            ConflictException.class,
            DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerConflictException(Exception e) {
        log.debug("Conflict exception {}", e);

        return ApiError.builder()
                .status(HttpStatus.CONFLICT.toString())
                .message(e.getMessage())
                .reason("Request conflicts")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handlerExceptions(Throwable e) {
        log.warn("Unexpected exception", e);

        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .message(e.getMessage())
                .reason("Unexpected internal error")
                .build();
    }
}
