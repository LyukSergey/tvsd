# Module 04 — CRUD через Spring Data

Тепер навчимо застосунок **створювати, читати, оновлювати й видаляти** товари
(англ. **CRUD** — Create, Read, Update, Delete) через REST API. Побачимо, як мало коду
для цього потрібно завдяки Spring Data — і який **SQL** генерується для кожної операції.

## Що навчимося
- Отримувати готові методи роботи з БД через `JpaRepository`.
- Будувати застосунок шарами: **Controller → Service → Repository**.
- Використовувати **DTO** замість того, щоб віддавати сутність назовні.
- Повертати коректні **HTTP-коди** (200/201/204/404/400).
- Звіряти кожну операцію з її **SQL** у логах.

## Передумови
- Пройдений [module-03](../module-03-first-entity/): є сутність `Product` і робочий проєкт.

---

## 1. Репозиторій: методи «безкоштовно»

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

Ми лише **оголосили інтерфейс** — реалізацію Spring Data згенерує сам. Успадкувавши
`JpaRepository<Product, Long>` (`<тип сутності, тип id>`), ми отримали готові методи:

| Метод | Що робить | SQL |
|---|---|---|
| `save(p)` | зберегти (новий — вставити, наявний — оновити) | `INSERT` / `UPDATE` |
| `findById(id)` | знайти за id (повертає `Optional`) | `SELECT ... WHERE id = ?` |
| `findAll()` | усі рядки | `SELECT ...` |
| `existsById(id)` | чи є такий | `SELECT count(*) ... WHERE id = ?` |
| `deleteById(id)` | видалити | `DELETE ... WHERE id = ?` |
| `count()` | скільки всього | `SELECT count(*)` |

---

## 2. Шари застосунку

```
Controller (HTTP)  →  Service (логіка, транзакції)  →  Repository (БД)
```

- **`ProductController`** — приймає HTTP-запити, делегує сервісу, повертає відповіді.
- **`ProductService`** — бізнес-логіка; тут `@Transactional` (межі транзакцій).
- **`ProductRepository`** — доступ до бази.

Чому не ходити з контролера прямо в репозиторій? Щоб кожен шар мав **одну відповідальність**:
контролер знає тільки про HTTP, репозиторій — тільки про БД, а логіка й транзакції живуть
у сервісі.

---

## 3. DTO: не віддаємо сутність назовні

Замість того щоб приймати й повертати саму `@Entity`, використовуємо окремі обʼєкти:

- **`ProductRequest`** — те, що клієнт **надсилає** (без `id`, з валідацією).
- **`ProductResponse`** — те, що застосунок **повертає** (з `id`).

Навіщо так:
- клієнт не задає `id` (його проставляє база);
- вхідні дані валідуються (`@NotBlank`, `@PositiveOrZero`) окремо від моделі БД;
- сутність не «витікає» назовні — у [module-05](../module-05-associations/) це врятує нас
  від `LazyInitializationException` та нескінченної JSON-рекурсії при звʼязках.

---

## 4. Запуск і перевірка

```bash
docker compose up -d       # 1. база
mvn spring-boot:run        # 2. застосунок (слухає http://localhost:8080)
```

Перевірити всі операції можна файлом **`http/product-crud.http`** (у IntelliJ — кнопка ▶
над кожним запитом) або через `curl`:

```bash
# створити
curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","price":999.00,"quantity":10}'

# усі товари
curl -s http://localhost:8080/api/products

# один товар
curl -s http://localhost:8080/api/products/1

# оновити
curl -s -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","price":949.00,"quantity":8}'

# видалити
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE http://localhost:8080/api/products/1
```

---

## 5. SQL-місток: операція → SQL

Під час запитів дивіться в консоль застосунку. **Один виклик репозиторію = один SQL.**

| Операція (HTTP) | Метод репозиторію | SQL у логах |
|---|---|---|
| `POST /api/products` | `save(new)` | `insert into products (name, price, quantity) values (?, ?, ?)` |
| `GET /api/products` | `findAll()` | `select ... from products p1_0` |
| `GET /api/products/1` | `findById(1)` | `select ... from products p1_0 where p1_0.id=?` |
| `PUT /api/products/1` | `findById` + зміна полів | `select ...` потім `update products set name=?, price=?, quantity=? where id=?` |
| `DELETE /api/products/2` | `existsById` + `deleteById` | `select count(*) ...` потім `delete from products where id=?` |

> **Важлива деталь про `UPDATE`.** У методі `update` ми **не викликаємо** `save()`. Поки код
> виконується всередині `@Transactional`, завантажений обʼєкт «керований» (managed), і Hibernate
> сам помічає зміну полів (**dirty checking**) та генерує `UPDATE` при завершенні транзакції.
> Це поведінку детально розберемо в [module-05](../module-05-associations/).

Порівняйте ці запити з тими, що ви писали **руками** в
[module-01](../module-01-sql-by-hand/) (`03-select-basics.sql`). Це той самий SQL —
тільки тепер його пише Hibernate.

---

## 6. HTTP-коди відповідей

| Ситуація | Код | Де задано |
|---|---|---|
| Успішне читання / оновлення | `200 OK` | за замовчуванням |
| Успішне створення | `201 Created` | `ResponseEntity.status(CREATED)` у контролері |
| Успішне видалення | `204 No Content` | `ResponseEntity.noContent()` |
| Товар не знайдено | `404 Not Found` | `ProductNotFoundException` → `GlobalExceptionHandler` |
| Невалідні вхідні дані | `400 Bad Request` | `@Valid` → `GlobalExceptionHandler` |

---

## Типові помилки

| Симптом | Причина | Як виправити |
|---|---|---|
| `Connection refused` | База не запущена | `docker compose up -d` |
| `Port 8080 ... already in use` | Застосунок уже запущений | Зупиніть попередній процес або змініть `server.port` |
| `404` на явно наявному товарі | Невірний id у шляху | Спершу `GET /api/products` і візьміть реальний id |
| `400` при створенні | Не пройшла валідація | Перевірте тіло: `name` не порожнє, `price`/`quantity` ≥ 0 |
| Повертається сутність із зайвими полями | Віддаєте `@Entity`, а не DTO | Повертайте `ProductResponse` |

---

## Завдання для самостійного виконання

Реалізуйте **повний CRUD** для товару самостійно (цей модуль — еталон для звірки):

1. Створіть 3 товари через `POST`.
2. Отримайте список (`GET`) і один товар за id.
3. Оновіть ціну одного з них (`PUT`) і переконайтесь у логах, що пішов `UPDATE` **без**
   окремого `SELECT`+`save` з вашого боку.
4. Видаліть один товар (`DELETE`), переконайтесь у коді `204`.
5. Запросіть неіснуючий id — переконайтесь у коді `404`.
6. Для кожної операції **знайдіть відповідний SQL** у консолі.

---

## Чеклист DoD

- [ ] Усі 5 операцій (POST/GET-all/GET-one/PUT/DELETE) працюють.
- [ ] Неіснуючий id повертає `404`, невалідні дані — `400`.
- [ ] У логах видно відповідний SQL для кожної операції.
- [ ] Назовні повертається `ProductResponse` (DTO), а не сутність.
- [ ] Код розділено на Controller / Service / Repository.

---

## Що далі

У **[module-05-associations](../module-05-associations/)** додамо решту сутностей
(`Category`, `Customer`, `Order`, `OrderItem`) і **звʼязки** між ними, розберемо
**сесію**, **життєвий цикл сутності** та **транзакції** глибше.
