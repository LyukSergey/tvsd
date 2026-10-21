-- =====================================================================
-- Модуль 01. Крок 2 — наповнення таблиць даними РУКАМИ (INSERT).
-- =====================================================================
-- Запуск:
--   docker compose exec -T db psql -U shop -d shop < sql/02-insert-data.sql
--
-- Порядок вставки той самий, що й створення: спершу батьківські таблиці,
-- потім ті, що на них посилаються. Не можна вставити товар у категорію,
-- якої ще немає.
-- =====================================================================

-- Почнемо з чистого аркуша (якщо дані вже є — приберемо).
-- TRUNCATE швидко очищає таблиці; RESTART IDENTITY скидає лічильник id на 1.
TRUNCATE order_items, orders, products, customers, categories RESTART IDENTITY CASCADE;


-- ---------------------------------------------------------------------
-- Категорії. id проставиться автоматично: 1, 2, 3.
-- ---------------------------------------------------------------------
INSERT INTO categories (name) VALUES
    ('Phones'),       -- отримає id = 1
    ('Laptops'),      -- id = 2
    ('Accessories');  -- id = 3


-- ---------------------------------------------------------------------
-- Товари. category_id посилається на categories.id вище.
-- ---------------------------------------------------------------------
INSERT INTO products (name, price, quantity, category_id) VALUES
    ('iPhone 15',          999.00, 10, 1),   -- Phones
    ('Samsung Galaxy S24', 899.00, 15, 1),   -- Phones
    ('Google Pixel 8',     699.00,  8, 1),   -- Phones
    ('MacBook Air',       1199.00,  5, 2),   -- Laptops
    ('Dell XPS 13',       1099.00,  7, 2),   -- Laptops
    ('USB-C Cable',         19.00, 200, 3),  -- Accessories
    ('Phone Case',          29.00, 150, 3),  -- Accessories
    ('Wireless Charger',    49.00, 60, 3);   -- Accessories


-- ---------------------------------------------------------------------
-- Клієнти.
-- ---------------------------------------------------------------------
INSERT INTO customers (email, full_name) VALUES
    ('alice@example.com', 'Alice Johnson'),  -- id = 1
    ('bob@example.com',   'Bob Smith'),      -- id = 2
    ('carol@example.com', 'Carol White');    -- id = 3


-- ---------------------------------------------------------------------
-- Замовлення. customer_id посилається на customers.id.
-- created_at і status не вказуємо — спрацюють DEFAULT (now() і 'NEW'),
-- окрім випадків, де статус задаємо явно.
-- ---------------------------------------------------------------------
INSERT INTO orders (customer_id, status) VALUES
    (1, 'PAID'),      -- id = 1, замовлення Alice
    (1, 'NEW'),       -- id = 2, ще одне замовлення Alice
    (2, 'SHIPPED');   -- id = 3, замовлення Bob
-- Зверніть увагу: Carol (id = 3) замовлень НЕ має. Це знадобиться пізніше,
-- щоб побачити різницю між INNER JOIN і LEFT JOIN.


-- ---------------------------------------------------------------------
-- Позиції замовлень. Поєднують orders і products.
-- unit_price беремо з поточної ціни товару (так було на момент покупки).
-- ---------------------------------------------------------------------
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
    -- Замовлення 1 (Alice, PAID): iPhone + чохол + кабель
    (1, 1, 1,  999.00),
    (1, 7, 1,   29.00),
    (1, 6, 2,   19.00),
    -- Замовлення 2 (Alice, NEW): MacBook
    (2, 4, 1, 1199.00),
    -- Замовлення 3 (Bob, SHIPPED): два телефони + бездротова зарядка
    (3, 2, 1,  899.00),
    (3, 3, 1,  699.00),
    (3, 8, 1,   49.00);


-- ---------------------------------------------------------------------
-- Швидка перевірка, що дані на місці.
-- ---------------------------------------------------------------------
SELECT 'categories'  AS table_name, count(*) AS rows FROM categories
UNION ALL SELECT 'products',    count(*) FROM products
UNION ALL SELECT 'customers',   count(*) FROM customers
UNION ALL SELECT 'orders',      count(*) FROM orders
UNION ALL SELECT 'order_items', count(*) FROM order_items;
-- Очікуємо: categories 3, products 8, customers 3, orders 3, order_items 7.
