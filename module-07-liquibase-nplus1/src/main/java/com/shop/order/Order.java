package com.shop.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Замовлення. Одночасно:
 *  - сторона "БАГАТО" у звʼязку з Customer (багато замовлень -> один клієнт);
 *  - сторона "ОДИН" у звʼязку з OrderItem (одне замовлення -> багато позицій).
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Багато замовлень -> один клієнт. Власник звʼязку (стовпець customer_id). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    private String status = "NEW";

    /**
     * Одне замовлення -> багато позицій.
     * <p>
     * Власник звʼязку — поле order в OrderItem (там стовпець order_id).
     * LAZY (за замовчуванням для колекцій): позиції підвантажаться лише при зверненні
     * до getItems(). САМЕ ЦЯ колекція стане джерелом проблеми N+1 у модулі 07.
     * <p>
     * cascade = ALL + orphanRemoval: збереження/видалення замовлення автоматично
     * поширюється на його позиції; позиція, прибрана зі списку, видаляється з БД.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order(Customer customer) {
        setCustomer(customer);
    }

    /** Додати позицію й виставити зворотнє посилання. */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
