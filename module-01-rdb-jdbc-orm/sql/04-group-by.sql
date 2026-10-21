-- =====================================================================
-- Модуль 01. Крок 4 — АГРЕГАЦІЯ: GROUP BY, COUNT/SUM/AVG, HAVING.
-- =====================================================================
-- Агрегація = «згорнути багато рядків в один підсумок».
-- Приклади: скільки товарів у категорії, на яку суму кожне замовлення.
--
-- Агрегатні функції:
--   COUNT(*)   — скільки рядків
--   SUM(x)     — сума значень
--   AVG(x)     — середнє
--   MIN(x)/MAX(x) — найменше / найбільше
--
-- GROUP BY <стовпець> — рахує агрегат ОКРЕМО для кожного значення цього стовпця.
-- Правило: усе, що у SELECT стоїть поза агрегатною функцією, МАЄ бути в GROUP BY.
-- =====================================================================


-- 1. Скільки взагалі товарів у таблиці (без групування — один підсумок).
SELECT count(*) AS total_products
FROM products;


-- 2. Скільки товарів У КОЖНІЙ категорії.
--    GROUP BY c.name збирає товари в групи за назвою категорії
--    і рахує COUNT окремо для кожної групи.
SELECT c.name            AS category,
       count(p.id)       AS products_count
FROM categories c
JOIN products p ON p.category_id = c.id
GROUP BY c.name
ORDER BY products_count DESC;
-- Очікуємо: Phones 3, Accessories 3, Laptops 2.


-- 3. Середня / мінімальна / максимальна ціна по категорії.
SELECT c.name          AS category,
       count(p.id)     AS products_count,
       round(avg(p.price), 2) AS avg_price,   -- round(..., 2) — округлити до 2 знаків
       min(p.price)    AS cheapest,
       max(p.price)    AS most_expensive
FROM categories c
JOIN products p ON p.category_id = c.id
GROUP BY c.name
ORDER BY avg_price DESC;


-- 4. Сума КОЖНОГО замовлення.
--    Вартість позиції = quantity * unit_price; сума позицій = вартість замовлення.
--    Групуємо за id замовлення.
SELECT o.id                                AS order_id,
       cu.full_name                        AS customer,
       sum(oi.quantity * oi.unit_price)    AS order_total
FROM orders o
JOIN customers cu   ON cu.id = o.customer_id
JOIN order_items oi ON oi.order_id = o.id
GROUP BY o.id, cu.full_name              -- cu.full_name теж у GROUP BY, бо він у SELECT поза агрегатом
ORDER BY order_total DESC;


-- 5. Скільки замовлень у кожного клієнта (LEFT JOIN, щоб врахувати й тих, хто без замовлень).
SELECT cu.full_name          AS customer,
       count(o.id)           AS orders_count
FROM customers cu
LEFT JOIN orders o ON o.customer_id = cu.id
GROUP BY cu.full_name
ORDER BY orders_count DESC;
-- У Carol буде 0 (count рахує реальні рядки o.id, а в неї їх немає).


-- =====================================================================
-- HAVING — фільтр ПІСЛЯ групування.
-- =====================================================================
-- Важливо: WHERE фільтрує ОКРЕМІ рядки ДО групування,
--          HAVING фільтрує вже ГОТОВІ ГРУПИ (можна використовувати агрегати).

-- 6. Категорії, де більш ніж 2 товари.
SELECT c.name        AS category,
       count(p.id)   AS products_count
FROM categories c
JOIN products p ON p.category_id = c.id
GROUP BY c.name
HAVING count(p.id) > 2            -- умова на агрегат -> це HAVING, а не WHERE
ORDER BY products_count DESC;
-- Очікуємо тільки Phones (3) і Accessories (3); Laptops (2) відсіється.


-- 7. WHERE + GROUP BY + HAVING разом.
--    Серед товарів дорожчих за 20 (WHERE) порахувати середню ціну по категоріях (GROUP BY)
--    і лишити категорії з середньою ціною понад 500 (HAVING).
SELECT c.name                   AS category,
       round(avg(p.price), 2)   AS avg_price
FROM categories c
JOIN products p ON p.category_id = c.id
WHERE p.price > 20                       -- фільтр рядків ДО групування
GROUP BY c.name
HAVING avg(p.price) > 500                -- фільтр груп ПІСЛЯ групування
ORDER BY avg_price DESC;
