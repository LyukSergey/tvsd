package com.shop.common;

import com.shop.product.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Централізована обробка помилок для всіх контролерів.
 * Перетворює винятки на коректні HTTP-відповіді, щоб клієнт отримував
 * зрозумілі коди замість "500 Internal Server Error".
 *
 * ProblemDetail — стандартний формат тіла помилки (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Товар не знайдено -> 404 Not Found. */
    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleNotFound(ProductNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Невалідні вхідні дані (@Valid не пройшов) -> 400 Bad Request. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
    }
}
