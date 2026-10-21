-- =====================================================================
-- Модуль 01. Крок 1 — створення таблиць РУКАМИ.
-- =====================================================================
-- Запуск цілого файлу:
--   docker compose exec -T db psql -U shop -d shop < sql/01-create-tables.sql
-- Або копіюйте блоки по одному у psql / DBeaver і виконуйте вручну,
-- читаючи пояснення нижче.
--
-- Порядок створення таблиць має значення: не можна послатися (REFERENCES)
-- на таблицю, якої ще немає. Тому спершу "батьківські" (categories, customers),
-- потім "дочірні" (products, orders), і наприкінці зв'язкова (order_items).
-- =====================================================================

-- Щоб скрипт можна було запускати повторно, спершу прибираємо старі таблиці.
-- CASCADE автоматично прибере й залежні об'єкти (зовнішні ключі).
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders      CASCADE;
DROP TABLE IF EXISTS products    CASCADE;
DROP TABLE IF EXISTS customers   CASCADE;
DROP TABLE IF EXISTS categories  CASCADE;


-- ---------------------------------------------------------------------
-- categories — категорії товарів (наприклад: Phones, Laptops).
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- автонумерація; PRIMARY KEY = унікальний ідентифікатор рядка
    name VARCHAR(100) NOT NULL UNIQUE                        -- текст до 100 символів; NOT NULL = обов'язкове; UNIQUE = без повторів
);
-- GENERATED ALWAYS AS IDENTITY — база сама проставляє id (1, 2, 3, ...).
-- PRIMARY KEY (первинний ключ) — як унікальний номер рядка: за ним знаходять конкретний запис.


-- ---------------------------------------------------------------------
-- products — товари. Кожен товар належить одній категорії (зв'язок 1:N).
-- ---------------------------------------------------------------------
CREATE TABLE products (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    price       NUMERIC(12, 2) NOT NULL,     -- гроші: 12 цифр усього, 2 після коми. НЕ використовуємо тип з комою (float) для грошей!
    quantity    INTEGER        NOT NULL DEFAULT 0,
    category_id BIGINT         REFERENCES categories (id)   -- ЗОВНІШНІЙ КЛЮЧ: вказує на categories.id
);
-- category_id — FOREIGN KEY (зовнішній ключ). Це зв'язок: значення в цьому стовпці
-- має дорівнювати якомусь id у таблиці categories. База не дасть вставити товар
-- із category_id, якого немає в categories. Так реалізується зв'язок "1 категорія — N товарів".


-- ---------------------------------------------------------------------
-- customers — клієнти.
-- ---------------------------------------------------------------------
CREATE TABLE customers (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL
);


-- ---------------------------------------------------------------------
-- orders — замовлення. Кожне належить одному клієнту (зв'язок 1:N).
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT      NOT NULL REFERENCES customers (id),  -- чиє замовлення
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),              -- коли створене; now() = поточний час
    status      VARCHAR(20) NOT NULL DEFAULT 'NEW'               -- NEW / PAID / SHIPPED / CANCELLED
);


-- ---------------------------------------------------------------------
-- order_items — позиції замовлення.
-- Це ЗВ'ЯЗКОВА таблиця: вона поєднує orders і products.
-- Одне замовлення має багато позицій; один товар може бути в багатьох позиціях.
-- Разом це дає зв'язок "багато-до-багатьох" (N:M) між orders і products.
-- ---------------------------------------------------------------------
CREATE TABLE order_items (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id),   -- до якого замовлення належить позиція
    product_id BIGINT         NOT NULL REFERENCES products (id), -- який товар
    quantity   INTEGER        NOT NULL,                          -- скільки одиниць
    unit_price NUMERIC(12, 2) NOT NULL                           -- ціна за одиницю НА МОМЕНТ замовлення
);
-- Чому зберігаємо unit_price окремо, а не беремо products.price?
-- Бо ціна товару з часом змінюється, а в замовленні має лишитися та ціна,
-- за якою реально купили. Це типове правило для реальних магазинів.


-- ---------------------------------------------------------------------
-- Перевірка: подивитися список створених таблиць.
-- У psql це робить команда \dt (без крапки з комою).
-- Через SQL — так:
-- ---------------------------------------------------------------------
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
-- Очікуємо 5 рядків: categories, customers, order_items, orders, products.
