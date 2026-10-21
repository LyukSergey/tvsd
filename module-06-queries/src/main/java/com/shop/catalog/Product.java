package com.shop.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Товар. Тепер (на відміну від модулів 03-04) має звʼязок із категорією.
 * Це сторона "БАГАТО" у звʼязку 1:N та ВЛАСНИК звʼязку (тут живе зовнішній ключ).
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Багато товарів -> одна категорія.
     * <p>
     * @ManyToOne робить ЦЕЙ бік власником звʼязку: у таблиці products зʼявиться
     * стовпець-зовнішній-ключ, названий через @JoinColumn як category_id.
     * <p>
     * fetch = LAZY: категорія НЕ підвантажується разом із товаром; окремий SELECT
     * піде лише при першому зверненні до product.getCategory(). Для @ManyToOne
     * за замовчуванням EAGER, але LAZY — здоровіша звичка (менше зайвих JOIN-ів).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public Product(String name, BigDecimal price, Integer quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
