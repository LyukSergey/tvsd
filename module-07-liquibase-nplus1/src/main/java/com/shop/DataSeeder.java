package com.shop;

import com.shop.catalog.Category;
import com.shop.catalog.CategoryRepository;
import com.shop.catalog.Product;
import com.shop.order.Customer;
import com.shop.order.CustomerRepository;
import com.shop.order.Order;
import com.shop.order.OrderItem;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Наповнює базу тими самими даними, що й модуль 01 — щоб запити (і особливо GROUP BY)
 * давали знайомі результати. Три категорії, вісім товарів, один клієнт із замовленням.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;

    public DataSeeder(CategoryRepository categoryRepository, CustomerRepository customerRepository) {
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return; // уже заповнено
        }

        Category phones = new Category("Phones");
        Product iphone = new Product("iPhone 15", new BigDecimal("999.00"), 10);
        Product samsung = new Product("Samsung Galaxy S24", new BigDecimal("899.00"), 15);
        Product pixel = new Product("Google Pixel 8", new BigDecimal("699.00"), 8);
        phones.addProduct(iphone);
        phones.addProduct(samsung);
        phones.addProduct(pixel);

        Category laptops = new Category("Laptops");
        Product macbook = new Product("MacBook Air", new BigDecimal("1199.00"), 5);
        laptops.addProduct(macbook);
        laptops.addProduct(new Product("Dell XPS 13", new BigDecimal("1099.00"), 7));

        Category accessories = new Category("Accessories");
        Product charger = new Product("Wireless Charger", new BigDecimal("49.00"), 60);
        accessories.addProduct(new Product("USB-C Cable", new BigDecimal("19.00"), 200));
        accessories.addProduct(new Product("Phone Case", new BigDecimal("29.00"), 150));
        accessories.addProduct(charger);

        categoryRepository.save(phones);
        categoryRepository.save(laptops);
        categoryRepository.save(accessories);

        // Кілька замовлень — щоб побачити N+1 на списку замовлень з їхніми позиціями.
        Customer alice = new Customer("alice@example.com", "Alice Johnson");

        Order o1 = new Order(alice);
        o1.setStatus("PAID");
        o1.addItem(new OrderItem(iphone, 1, new BigDecimal("999.00")));
        o1.addItem(new OrderItem(macbook, 1, new BigDecimal("1199.00")));
        alice.addOrder(o1);

        Order o2 = new Order(alice);
        o2.setStatus("NEW");
        o2.addItem(new OrderItem(samsung, 2, new BigDecimal("899.00")));
        alice.addOrder(o2);

        Order o3 = new Order(alice);
        o3.setStatus("SHIPPED");
        o3.addItem(new OrderItem(pixel, 1, new BigDecimal("699.00")));
        o3.addItem(new OrderItem(charger, 1, new BigDecimal("49.00")));
        alice.addOrder(o3);

        customerRepository.save(alice);

        System.out.println(">>> DataSeeder: демо-дані завантажено (3 категорії, 8 товарів, 3 замовлення).");
    }
}
