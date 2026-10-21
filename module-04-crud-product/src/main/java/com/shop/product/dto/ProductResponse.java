package com.shop.product.dto;

import com.shop.product.Product;

import java.math.BigDecimal;

/**
 * DTO для ВИХІДНИХ даних — те, що застосунок повертає клієнту.
 * Містить id (на відміну від ProductRequest).
 */
public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer quantity
) {
    /** Перетворити сутність на відповідь. */
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getQuantity());
    }
}
