package com.shop.catalog.dto;

import java.math.BigDecimal;

/**
 * DTO-проєкція (через конструктор). Використовується в JPQL-запиті
 * "select new com.shop.catalog.dto.ProductSummary(p.name, p.price) ...".
 * <p>
 * Сенс проєкції: не тягнути ВСЮ сутність, а вибрати лише потрібні стовпці.
 * У SQL це перетворюється на "select name, price" замість "select *".
 */
public record ProductSummary(String name, BigDecimal price) {
}
