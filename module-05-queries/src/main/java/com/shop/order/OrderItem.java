package com.shop.order;

import com.shop.catalog.Product;
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
 * Позиція замовлення — АСОЦІАТИВНА сутність, що реалізує звʼязок N:M
 * між Order і Product.
 * <p>
 * Чому не @ManyToMany напряму між Order і Product? Бо нам треба зберігати
 * ДОДАТКОВІ дані на звʼязку: кількість і ціну на момент покупки. Чистий @ManyToMany
 * дав би лише проміжну таблицю з двома ключами, без можливості додати quantity/unit_price.
 * Тому робимо окрему сутність із двома @ManyToOne.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Багато позицій -> одне замовлення. Стовпець order_id. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Багато позицій -> один товар. Стовпець product_id. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    public OrderItem(Product product, Integer quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
