-- =====================================================================
-- Модуль 01. Крок 3 — базові запити SELECT: вибірка, фільтр, сортування, JOIN.
-- =====================================================================
-- Виконуйте запити ПО ОДНОМУ (кожен закінчується крапкою з комою) і дивіться
-- результат. Коментарі пояснюють кожну секцію.
--
-- ЯК БУДУЄТЬСЯ ЗАПИТ. Повна форма SELECT має такий порядок секцій:
--
--     SELECT   які стовпці вивести
--     FROM     з якої таблиці
--     JOIN     які таблиці приєднати і за якою умовою
--     WHERE    якими рядками обмежитись (фільтр ДО групування)
--     GROUP BY як згрупувати рядки (див. файл 04)
--     HAVING   фільтр ПІСЛЯ групування (див. файл 04)
--     ORDER BY як відсортувати результат
--     LIMIT    скільки рядків повернути
--
-- Пишемо ми в цьому порядку, але база ЛОГІЧНО виконує інакше:
-- спочатку FROM/JOIN, потім WHERE, потім GROUP BY/HAVING, потім SELECT,
-- і лише наприкінці ORDER BY та LIMIT. Тому, наприклад, у WHERE ще не можна
-- користуватись псевдонімами зі SELECT — їх база «ще не бачить».
-- =====================================================================


-- 1. Найпростіше: усі стовпці, усі рядки.
SELECT * FROM products;


-- 2. Лише потрібні стовпці (так майже завжди краще, ніж *).
SELECT name, price FROM products;


-- 3. WHERE — фільтр рядків. Товари дорожчі за 500.
SELECT name, price
FROM products
WHERE price > 500;


-- 4. Кілька умов: AND / OR. Телефони (category_id = 1) дешевші за 900.
SELECT name, price
FROM products
WHERE category_id = 1
  AND price < 900;


-- 5. Пошук за текстом. LIKE шукає за шаблоном, % = «будь-які символи».
--    ILIKE — те саме, але без урахування регістру.
SELECT name, price
FROM products
WHERE name ILIKE '%phone%';


-- 6. ORDER BY — сортування. DESC = за спаданням, ASC (за замовчуванням) = за зростанням.
SELECT name, price
FROM products
ORDER BY price DESC;


-- 7. LIMIT — обмежити кількість рядків. Три найдорожчі товари.
SELECT name, price
FROM products
ORDER BY price DESC
LIMIT 3;


-- =====================================================================
-- JOIN — поєднання таблиць за зв'язком (FOREIGN KEY).
-- =====================================================================

-- 8. INNER JOIN: показати товар РАЗОМ із назвою його категорії.
--    Умова з'єднання: products.category_id = categories.id.
--    INNER JOIN лишає тільки ті рядки, для яких є пара в ОБОХ таблицях.
SELECT p.name    AS product,
       p.price,
       c.name    AS category
FROM products p
JOIN categories c ON c.id = p.category_id
ORDER BY c.name, p.name;
-- p і c — псевдоніми (alias) таблиць, щоб писати коротше: p.name замість products.name.


-- 9. JOIN трьох таблиць: клієнт -> замовлення -> позиції.
--    Хто що замовив.
SELECT cu.full_name AS customer,
       o.id         AS order_id,
       pr.name      AS product,
       oi.quantity,
       oi.unit_price
FROM orders o
JOIN customers cu   ON cu.id = o.customer_id
JOIN order_items oi ON oi.order_id = o.id
JOIN products pr    ON pr.id = oi.product_id
ORDER BY o.id;


-- 10. LEFT JOIN: показати ВСІХ клієнтів, навіть без замовлень.
--     INNER JOIN викинув би Carol (у неї немає замовлень).
--     LEFT JOIN лишає всі рядки лівої таблиці (customers); якщо пари немає —
--     праві стовпці будуть NULL (порожні).
SELECT cu.full_name AS customer,
       o.id         AS order_id,
       o.status
FROM customers cu
LEFT JOIN orders o ON o.customer_id = cu.id
ORDER BY cu.full_name;
-- У Carol рядок буде, але order_id і status = NULL — порівняйте з запитом №9.
