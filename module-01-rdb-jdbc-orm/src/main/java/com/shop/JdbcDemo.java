package com.shop;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Демонстрація роботи з базою "голим" JDBC — без жодного ORM.
 * <p>
 * Мета — показати ДВІ речі:
 * <ol>
 *   <li>скільки ручного коду (boilerplate) потрібно навіть для простого читання одного рядка;</li>
 *   <li>що таке ЗʼЄДНАННЯ (Connection) і ТРАНЗАКЦІЯ (commit/rollback) на найнижчому рівні.</li>
 * </ol>
 * Пізніше той самий результат ми отримаємо одним рядком через Spring Data,
 * але розумітимемо, що відбувається "під капотом".
 * <p>
 * Перед запуском підніміть базу:  docker compose up -d
 * і заповніть її даними з модуля 01 (sql/01 та sql/02).
 * Запуск демо:  mvn compile exec:java
 */
public class JdbcDemo {

    // Ті самі параметри, що в docker-compose.yml.
    private static final String URL = "jdbc:postgresql://localhost:5438/shop";
    private static final String USER = "shop";
    private static final String PASSWORD = "shop";

    public static void main(String[] args) throws Exception {
//        System.out.println("=== 1. Читання одного товару через JDBC ===");
//        Product product = findProductById(1L);
//        System.out.println("Знайдено: " + product);

        System.out.println();
        System.out.println("=== 2. Транзакція: створення замовлення з двох кроків ===");
        createOrderWithItem();
    }

    /**
     * Прочитати один товар за id. Зверніть увагу, СКІЛЬКИ коду потрібно для одного простого SELECT: відкрити зʼєднання, підготувати запит,
     * підставити параметр, пройтись по ResultSet, ВРУЧНУ перекласти кожен стовпець у поле обʼєкта, і все це — акуратно закрити.
     */
    static Product findProductById(long id) throws SQLException {
        String sql = "SELECT id, name, price, quantity FROM products WHERE id = ?";

        // try-with-resources сам закриє Connection/Statement/ResultSet.
        // Connection — це і є "зʼєднання" з базою: відкритий канал, через який
        // ходять запити. Його відкриття коштує дорого, тому в реальних застосунках
        // зʼєднання тримають у пулі (connection pool) і перевикористовують.
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);                 // підставляємо значення замість "?"

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // РУЧНИЙ мапінг: стовпець бази -> поле обʼєкта. Назви й типи
                    // ми зобовʼязані знати й вказати самі. Помилка в імені стовпця
                    // або типі виявиться лише під час виконання.
                    Product p = new Product();
                    p.id = rs.getLong("id");
                    p.name = rs.getString("name");
                    p.price = rs.getBigDecimal("price");
                    p.quantity = rs.getInt("quantity");
                    return p;
                }
                return null; // нічого не знайдено
            }
        }
    }

    /**
     * Створити замовлення й одразу додати до нього позицію — ДВА INSERT, які мають виконатись РАЗОМ або не виконатись зовсім.
     * <p>
     * Це і є ТРАНЗАКЦІЯ. Якщо після створення замовлення другий крок впаде, ми робимо rollback — і в базі не лишається "напівзамовлення"
     * без позицій.
     */
    static void createOrderWithItem() throws Exception {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // За замовчуванням JDBC робить COMMIT після кожної команди (autocommit).
            // Вимикаємо його, щоб керувати транзакцією вручну — це і є "BEGIN".
            conn.setAutoCommit(false);

            // Крок 1: нове замовлення для клієнта 1. RETURNING id поверне згенерований id.
            long orderId;
            String insertOrder = "INSERT INTO orders (customer_id, status) VALUES (?, 'NEW') RETURNING id";
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ps.setLong(1, 1L);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    orderId = rs.getLong("id");
                }
            }

            final String price = "1001.00";
            if (Double.parseDouble(price) > 1000) {
                throw new RuntimeException("error");
            }

            // Крок 2: позиція замовлення — 1 шт. товару 1 за ціною 999.00.
            String insertItem =
                    "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                ps.setLong(1, orderId);
                ps.setLong(2, 1L);
                ps.setInt(3, 1);
                ps.setBigDecimal(4, new BigDecimal(price));
                ps.executeUpdate();
            }

            // Обидва кроки вдалися — фіксуємо зміни. Це "COMMIT".
            conn.commit();
            System.out.println("Транзакцію зафіксовано. Створено замовлення id=" + orderId + " з однією позицією.");

        } catch (Exception e) {
            // Будь-яка помилка -> скасовуємо ВСЮ транзакцію. Це "ROLLBACK".
            // У базі не лишиться ані замовлення, ані позиції.
            if (conn != null) {
                conn.rollback();
                System.out.println("Сталася помилка — транзакцію скасовано (rollback). У базі нічого не змінилось.");
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Простий носій даних для рядка з products (у "великому" коді це була б @Entity).
     */
    static class Product {

        long id;
        String name;
        BigDecimal price;
        int quantity;

        @Override
        public String toString() {
            return "Product{id=" + id + ", name='" + name + "', price=" + price + ", quantity=" + quantity + '}';
        }
    }
}
