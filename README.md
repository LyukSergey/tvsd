# Технології взаємодії серверного додатку з базою даних на Java

Практичний навчальний курс: від **чистого SQL руками** до повноцінного рівня доступу до даних
на **Spring Data JPA / Hibernate / Liquibase** з базою **PostgreSQL**.

Весь курс побудований навколо одного наскрізного прикладу — **інтернет-магазину**.
Ті самі таблиці й запити, які ви напишете руками на першому занятті, пізніше згенерує за вас
Hibernate — і ви щоразу бачитимете, **який саме SQL** за цим стоїть.

---

## Доменна модель (наскрізний приклад — інтернет-магазин)

```
Category 1 ──< N Product          товар належить одній категорії
Customer 1 ──< N Order            клієнт має багато замовлень
Order    1 ──< N OrderItem        замовлення складається з позицій
Product  1 ──< N OrderItem        товар трапляється в багатьох позиціях
Product  N >──< N Order           «багато-до-багатьох» через OrderItem
```

| Таблиця / сутність | Поля |
|---|---|
| `Category`  | `id`, `name` |
| `Product`   | `id`, `name`, `price`, `quantity`, `category_id` |
| `Customer`  | `id`, `email`, `full_name` |
| `Order`     | `id`, `customer_id`, `created_at`, `status` |
| `OrderItem` | `id`, `order_id`, `product_id`, `quantity`, `unit_price` |

---

## Структура курсу

Кожен модуль — **окрема самодостатня тека**: методичка (`README.md`) + код/скрипти.
Модулі можна відкривати й запускати незалежно один від одного.

| Модуль | Тема | Що всередині |
|---|---|---|
| [`module-00-setup`](module-00-setup/)           | Підготовка середовища | Встановлення JDK, Docker, IDE; як отримати код; як читати лог Hibernate |
| [`module-01-sql-by-hand`](module-01-sql-by-hand/) | SQL руками | `CREATE TABLE`, `INSERT`, `SELECT`, `JOIN`, `GROUP BY` — без Java |
| [`module-02-jdbc-orm`](module-02-jdbc-orm/)       | JDBC vs ORM | З'єднання, сесія, транзакція, зв'язки; навіщо потрібен ORM |
| [`module-03-first-entity`](module-03-first-entity/) | Перша `@Entity` | Перша сутність `Product`; анотація → `CREATE TABLE` |
| [`module-04-crud-product`](module-04-crud-product/) | CRUD | Репозиторій, сервіс, REST-контролер, DTO |
| [`module-05-associations`](module-05-associations/) | Асоціації | Зв'язки між сутностями, життєвий цикл, транзакції |
| [`module-06-queries`](module-06-queries/)         | Запити | derived-методи, JPQL, `@Query`, проєкції, пагінація, `GROUP BY` |
| [`module-07-liquibase-nplus1`](module-07-liquibase-nplus1/) | Liquibase + N+1 | Версіонування схеми; діагностика й усунення проблеми N+1 |

> Рекомендований порядок проходження — зверху вниз. Кожен наступний модуль спирається
> на розуміння, здобуте в попередніх, але код у кожній теці — повний і самостійний.

---

## З чого почати

1. Пройдіть **[module-00-setup](module-00-setup/)** — встановіть усе потрібне й перевірте версії.
2. Переходьте до **[module-01-sql-by-hand](module-01-sql-by-hand/)** — тут починається робота з базою.

---

## Технологічний стек

| Компонент | Версія |
|---|---|
| Java (JDK) | 17 |
| Spring Boot | 3.3.x |
| Spring Data JPA / Hibernate | іде разом зі Spring Boot |
| Liquibase | іде разом зі Spring Boot |
| PostgreSQL | 16 (у Docker) |
| Збірка | Maven |

Точні версії та перевірена комбінація зафіксовані в [module-00-setup](module-00-setup/)
і у файлах `pom.xml` кожного модуля з кодом.
