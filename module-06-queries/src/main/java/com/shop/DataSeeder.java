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
        phones.addProduct(iphone);
        phones.addProduct(new Product("Samsung Galaxy S24", new BigDecimal("899.00"), 15));
        phones.addProduct(new Product("Google Pixel 8", new BigDecimal("699.00"), 8));

        Category laptops = new Category("Laptops");
        Product macbook = new Product("MacBook Air", new BigDecimal("1199.00"), 5);
        laptops.addProduct(macbook);
        laptops.addProduct(new Product("Dell XPS 13", new BigDecimal("1099.00"), 7));

        Category accessories = new Category("Accessories");
        accessories.addProduct(new Product("USB-C Cable", new BigDecimal("19.00"), 200));
        accessories.addProduct(new Product("Phone Case", new BigDecimal("29.00"), 150));
        accessories.addProduct(new Product("Wireless Charger", new BigDecimal("49.00"), 60));

        categoryRepository.save(phones);
        categoryRepository.save(laptops);
        categoryRepository.save(accessories);

        // Клієнт із замовленням на два товари.
        Customer alice = new Customer("alice@example.com", "Alice Johnson");
        Order order = new Order(alice);
        order.setStatus("PAID");
        order.addItem(new OrderItem(iphone, 1, new BigDecimal("999.00")));
        order.addItem(new OrderItem(macbook, 1, new BigDecimal("1199.00")));
        alice.addOrder(order);
        customerRepository.save(alice);

        System.out.println(">>> DataSeeder: демо-дані завантажено (3 категорії, 8 товарів).");
    }
}
