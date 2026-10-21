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
 * Наповнює базу прикладовими даними при старті застосунку — ті самі дані, що
 * вставляли руками в модулі 01. Завдяки cascade = ALL достатньо зберегти
 * "кореневі" обʼєкти (Category, Customer), а повʼязані збережуться автоматично.
 * <p>
 * Усе в одному методі з @Transactional — тобто в межах ОДНІЄЇ транзакції
 * (= однієї сесії Hibernate).
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
        // --- Категорії + товари (cascade збереже товари разом із категорією) ---
        Category phones = new Category("Phones");
        Product iphone = new Product("iPhone 15", new BigDecimal("999.00"), 10);
        Product samsung = new Product("Samsung Galaxy S24", new BigDecimal("899.00"), 15);
        phones.addProduct(iphone);
        phones.addProduct(samsung);

        Category laptops = new Category("Laptops");
        Product macbook = new Product("MacBook Air", new BigDecimal("1199.00"), 5);
        laptops.addProduct(macbook);

        categoryRepository.save(phones);
        categoryRepository.save(laptops);

        // --- Клієнт + замовлення + позиції (усе збережеться каскадом) ---
        Customer alice = new Customer("alice@example.com", "Alice Johnson");

        Order order = new Order(alice);
        order.setStatus("PAID");
        order.addItem(new OrderItem(iphone, 1, new BigDecimal("999.00")));
        order.addItem(new OrderItem(macbook, 1, new BigDecimal("1199.00")));

        // Зберігаємо КЛІЄНТА. Завдяки cascade = ALL на Customer.orders це каскадно
        // збереже і замовлення, а воно (теж cascade) — свої позиції.
        // Товари й категорії вже збережені вище, тож позиції посилаються на наявні рядки.
        alice.addOrder(order);
        customerRepository.save(alice);

        System.out.println(">>> DataSeeder: демо-дані завантажено.");
    }
}
