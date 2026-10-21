# Module 06 — Запити у Spring Data (включно з GROUP BY)

Тепер, коли є повна модель зі звʼязками, навчимося **вибирати дані різними способами**:
від методів, які Spring будує з назви, до JPQL, нативного SQL, проєкцій, пагінації
та агрегації через **`GROUP BY`**. Для кожного способу звіримо згенерований SQL —
і побачимо, що `GROUP BY` через JPQL дає **той самий SQL**, який ми писали руками в модулі 01.

## Що навчимося
- Писати **derived-методи** (запит із назви методу).
- Писати **JPQL** та **нативний SQL** через `@Query`.
- Використовувати **проєкції** (тягнути лише потрібні стовпці).
- Робити **пагінацію** й сортування (`Pageable`, `Page`).
- Писати **агрегації**: `COUNT`, `AVG`, **`GROUP BY`**, **`HAVING`** — через JPQL.

## Передумови
- Пройдений [module-05](../module-05-associations/): є всі сутності та звʼязки.
- Корисно тримати поруч `04-group-by.sql` із [module-01](../module-01-sql-by-hand/) для звірки.

---

## 1. Derived query methods — запит із назви

Spring Data будує SQL, **розбираючи назву методу**. Реалізацію писати не треба — лише
правильно назвати. Приклади з `ProductRepository`:

```java
List<Product> findByName(String name);                        // where name = ?
List<Product> findByPriceGreaterThan(BigDecimal price);       // where price > ?
List<Product> findByNameContainingIgnoreCase(String part);    // where lower(name) like lower('%?%')
List<Product> findByCategoryName(String categoryName);        // join categories ... where category.name = ?
List<Product> findByPriceBetweenOrderByPriceAsc(BigDecimal from, BigDecimal to);
List<Product> findTop3ByOrderByPriceDesc();                   // order by price desc, limit 3
```

Правила іменування: `findBy` + поле + умова (`GreaterThan`, `Between`, `Containing`,
`IgnoreCase`), зʼєднання `And`/`Or`, сортування `OrderBy...Asc/Desc`, обмеження `Top`/`First`.
Навігація по асоціації — через назву поля: `findByCategoryName` → `p.category.name`.

> **Межі підходу.** Для складних запитів назва методу стає нечитабельною. Тоді переходять
> на JPQL / `@Query`.

---

## 2. JPQL та нативний SQL через `@Query`

**JPQL** схожий на SQL, але оперує **сутностями та їхніми полями**, а не таблицями:

```java
@Query("select p from Product p where p.category.name = :name")
List<Product> findInCategory(@Param("name") String name);
```

- `Product`, `p.category.name` — це **клас і поля**, не таблиці/стовпці.
- `:name` — іменований параметр (альтернатива — позиційний `?1`).

Коли треба саме «чистий» SQL — `nativeQuery = true`:

```java
@Query(value = "select * from products where price > :price", nativeQuery = true)
List<Product> findExpensiveNative(@Param("price") BigDecimal price);
```

---

## 3. Проєкції — не тягнути всю сутність

Якщо потрібні лише кілька полів, вибираємо їх у **DTO** замість цілої сутності:

```java
@Query("select new com.shop.catalog.dto.ProductSummary(p.name, p.price) " +
       "from Product p order by p.price desc")
List<ProductSummary> findPriceList();
```

У SQL це дає `select name, price` **замість** `select *` — менше даних з бази.

---

## 4. Пагінація та сортування

Передаємо `Pageable`, отримуємо `Page` (сторінка + загальна кількість):

```java
Page<Product> findByPriceGreaterThan(BigDecimal price, Pageable pageable);
```

```java
Page<Product> p = repo.findByPriceGreaterThan(
        BigDecimal.ZERO, PageRequest.of(0, 3, Sort.by("price").descending()));
```

Spring сам додасть у SQL обмеження сторінки **та окремий `count(*)`** (щоб знати, скільки
всього сторінок). `Page` знає загальну кількість; `Slice` — ні (легший, коли total не потрібен).

---

## 5. GROUP BY через JPQL — те саме, що руками в модулі 01

```java
@Query("""
       select new com.shop.catalog.dto.CategoryStats(c.name, count(p), avg(p.price))
       from Category c join c.products p
       group by c.name
       having count(p) > :min
       order by count(p) desc
       """)
List<CategoryStats> categoryStats(@Param("min") long min);
```

> **Важливо про типи DTO.** `count(...)` повертає `Long`, `avg(...)` — `Double`. Конструктор
> `CategoryStats(String, Long, Double)` має точно їм відповідати, інакше Hibernate кине
> `Missing constructor`.

**Що Hibernate згенерував** (з логів застосунку):

```sql
select
    c1_0.name,
    count(p1_0.id),
    avg(p1_0.price)
from categories c1_0
join products p1_0 on c1_0.id = p1_0.category_id
group by c1_0.name
having count(p1_0.id) > ?
order by count(p1_0.id) desc
```

Порівняйте це з `04-group-by.sql` із [module-01](../module-01-sql-by-hand/) — **той самий SQL**.
Різниця лише в тому, що тепер ми описали його мовою JPQL (по сутностях), а Hibernate
переклав у SQL.

---

## 6. SQL-місток: спосіб → SQL → коли використовувати

| Спосіб | Приклад | SQL (спрощено) | Коли |
|---|---|---|---|
| Derived | `findByPriceGreaterThan` | `... where price > ?` | прості умови |
| Derived + навігація | `findByCategoryName` | `join categories ... where c.name = ?` | фільтр по звʼязку |
| `Top`/`First` | `findTop3ByOrderByPriceDesc` | `order by price desc fetch first 3 rows only` | «найдорожчі N» |
| JPQL | `@Query("select p ...")` | звичайний `select` | складніші умови |
| Нативний SQL | `nativeQuery = true` | SQL як є | специфіка СУБД |
| Проєкція | `ProductSummary` | `select name, price` | треба кілька полів |
| Пагінація | `Pageable` | `... fetch first ? rows only` + `count(*)` | великі списки |
| Агрегація | `GROUP BY` + `HAVING` | `group by ... having ...` | підсумки по групах |

---

## 7. Запуск і перевірка

```bash
docker compose up -d
mvn spring-boot:run
```

`DataSeeder` завантажить 3 категорії та 8 товарів. Перевіряйте запити через
**`http/queries.http`** або `curl` — і дивіться SQL у консолі:

```bash
curl -s "http://localhost:8080/api/query/expensive?price=500"
curl -s "http://localhost:8080/api/query/by-category?name=Phones"
curl -s "http://localhost:8080/api/query/price-list"
curl -s "http://localhost:8080/api/query/category-stats?min=2"
curl -s "http://localhost:8080/api/query/page?price=0&size=3"
```

Очікувані результати (збігаються з модулем 01):
- `category-stats?min=2` → **Accessories** (3) та **Phones** (3); Laptops (2) відсіюється `HAVING`.
- `category-stats?min=0` → усі три категорії.

---

## Типові помилки

| Симптом | Причина | Як виправити |
|---|---|---|
| `Missing constructor for type ...` | Типи в DTO не збігаються з тим, що повертає JPQL | `count`→`Long`, `avg`→`Double`; звірте конструктор |
| Derived-метод не знаходить поле | Помилка в назві (напр. `findByPrice` замість `findByPriceGreaterThan`) | Дотримуйтесь правил іменування |
| Проєкція робить `select *` | Повертаєте сутність, а не DTO | Використайте `select new ...DTO(...)` |
| `Page` робить зайвий `count` | Це нормально — так рахується кількість сторінок | Якщо total не треба — поверніть `Slice` |
| `LazyInitializationException` при віддачі результату | Серіалізуєте сутність зі звʼязком | Повертайте прості поля / DTO (як у `QueryDemoController`) |

---

## Завдання для самостійного виконання

Реалізуйте **пʼять** запитів по каталогу магазину (з них **≥2 агрегатні з `GROUP BY`**):

1. Товари дешевші за задану ціну (derived).
2. Товари заданої категорії (JPQL).
3. Прайс-лист «назва + ціна» як проєкція (без `select *`).
4. **GROUP BY:** кількість товарів у кожній категорії.
5. **GROUP BY + HAVING:** категорії із середньою ціною понад задане значення.

Для кожного знайдіть згенерований SQL у логах і звіртесь із модулем 01.

---

## Чеклист DoD

- [ ] Усі запити повертають коректні дані.
- [ ] Для кожного способу бачу у логах очікуваний SQL.
- [ ] Агрегатний запит (`GROUP BY`) дає правильні кількості/середні; `HAVING` відсіює групи.
- [ ] Проєкція НЕ робить `select *`.
- [ ] Пагінація додає обмеження сторінки та окремий `count(*)`.

---

## Що далі

У **[module-07-liquibase-nplus1](../module-07-liquibase-nplus1/)** замінимо автоматичне
створення схеми (`ddl-auto`) на **Liquibase** (міграції як код) і розберемо найвідомішу
пастку ORM — проблему **N+1**: відтворимо її на `Order → OrderItems` і усунемо через
`JOIN FETCH` / `@EntityGraph`, підтвердивши зниження кількості запитів у логах.
