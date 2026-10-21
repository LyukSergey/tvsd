package com.shop.product;

/**
 * Виняток "товар не знайдено". Кидається в сервісі, якщо запису з таким id немає.
 * Перетворюється на HTTP 404 у GlobalExceptionHandler.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Товар з id=" + id + " не знайдено");
    }
}
