package com.shop.catalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Категорія товарів. Сторона "ОДИН" у звʼязку 1:N з Product.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Зворотний бік звʼязку "одна категорія — багато товарів".
     * <p>
     * mappedBy = "category" каже: ВЛАСНИК звʼязку — поле `category` в класі Product.
     * Саме там живе зовнішній ключ (стовпець category_id). Тут, на боці "один",
     * НІЯКОГО стовпця в таблиці не зʼявляється — лише зручний доступ до списку товарів.
     * <p>
     * Без mappedBy Hibernate вирішив би, що це ДВА різні звʼязки, і створив би зайву
     * проміжну таблицю. Це типова помилка — тому mappedBy тут обовʼязковий.
     * <p>
     * За замовчуванням колекції LAZY: товари підвантажаться лише при першому зверненні.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    /** Зручний метод: додати товар і одразу виставити зворотнє посилання. */
    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }
}
