package com.shop.catalog.dto;

/**
 * DTO для результату АГРЕГАТНОГО запиту (GROUP BY по категоріях).
 * Повертає назву категорії, кількість товарів у ній та середню ціну.
 * <p>
 * Типи ВАЖЛИВІ й мають точно збігатися з тим, що повертає JPQL:
 *   count(...) -> Long,  avg(...) -> Double.
 * Інакше Hibernate не знайде відповідний конструктор ("Missing constructor").
 */
public record CategoryStats(String category, Long productsCount, Double avgPrice) {
}
