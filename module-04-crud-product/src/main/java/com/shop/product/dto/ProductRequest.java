package com.shop.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) для ВХІДНИХ даних — те, що клієнт надсилає
 * при створенні/оновленні товару.
 * <p>
 * Чому окремий клас, а не сама @Entity?
 * <ul>
 *   <li>клієнт не має задавати id (його проставляє база);</li>
 *   <li>можна додати перевірки (валідацію) окремо від моделі БД;</li>
 *   <li>сутність не "витікає" назовні — у модулі 05 це врятує нас від
 *       LazyInitializationException та нескінченної JSON-рекурсії.</li>
 * </ul>
 * Використовуємо record — короткий незмінний носій даних.
 */
public record ProductRequest(

        @NotBlank(message = "name не може бути порожнім")
        String name,

        @NotNull(message = "price обов'язкова")
        @PositiveOrZero(message = "price не може бути від'ємною")
        BigDecimal price,

        @NotNull(message = "quantity обов'язкова")
        @PositiveOrZero(message = "quantity не може бути від'ємною")
        Integer quantity
) {
}
