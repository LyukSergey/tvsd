package com.shop.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Перша сутність курсу — товар.
 * <p>
 * Навмисно робимо її максимально простою: БЕЗ звʼязку з категорією.
 * Поле category_id додамо пізніше, у модулі 05. Зараз мета — побачити,
 * як ОДНА анотована Java-клас перетворюється на ОДНУ таблицю в базі.
 * <p>
 * Кожна анотація нижче має прямий відбиток у згенерованому SQL —
 * див. "SQL-місток" у README цього модуля.
 */
@Entity                               // ця клас — сутність JPA (= рядок таблиці)
@Table(name = "products")             // назва таблиці в базі
@Getter                               // Lombok згенерує getter-и
@Setter                               // ... і setter-и
@NoArgsConstructor                    // JPA вимагає конструктор без аргументів
public class Product {

    @Id                                           // первинний ключ (PRIMARY KEY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // база сама проставляє id (як IDENTITY у SQL)
    private Long id;

    @Column(nullable = false, length = 255)       // -> NOT NULL, VARCHAR(255)
    private String name;

    // Для грошей ЗАВЖДИ BigDecimal, ніколи double/float (похибки при обчисленнях).
    @Column(nullable = false, precision = 12, scale = 2)  // -> NUMERIC(12,2) NOT NULL
    private BigDecimal price;

    @Column(nullable = false)                     // -> NOT NULL
    private Integer quantity;

    public Product(String name, BigDecimal price, Integer quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
