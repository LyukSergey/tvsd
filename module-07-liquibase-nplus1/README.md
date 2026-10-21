# Module 07 — Liquibase та проблема N+1

Фінальний модуль. Зробимо дві важливі для реальних проєктів речі:
1. Перейдемо від автоматичного створення схеми (`ddl-auto`) до **Liquibase** — міграцій
   схеми як коду в git.
2. Відтворимо найвідомішу пастку ORM — проблему **N+1** — і усунемо її, підтвердивши
   зниження кількості запитів у логах.

## Що навчимося
- Розуміти, чому `ddl-auto` не годиться для реальних проєктів.
- Описувати схему як **changeSets** Liquibase та застосовувати міграції.
- Перемикати Hibernate у режим `validate` (Liquibase — джерело істини).
- Діагностувати **N+1** за кількістю SQL у логах.
- Усувати N+1 через **`JOIN FETCH`** та **`@EntityGraph`**.

## Передумови
- Пройдений [module-06](../module-06-queries/): є модель, запити, демо-дані.

---

## Частина 1. Liquibase

### 1. Навіщо версіонувати схему

Досі ми користувались `ddl-auto: create`/`update`. Проблеми для реального проєкту:
- `create` **стирає дані** при кожному старті;
- `update` намагається «вгадати» зміни й **не вміє** перейменувань, складних міграцій,
  відкотів; на проді це небезпечно;
- немає **історії змін** схеми й немає способу відтворити схему крок за кроком.

**Liquibase** вирішує це: кожна зміна схеми — окремий **changeSet** у git. При старті
застосовуються лише **нові** changeSets, у строгому порядку. Схема стає відтворюваною
та версіонованою, як і код.

### 2. Підключення

- Залежність `liquibase-core` (у `pom.xml`).
- У `application.yml`:
  ```yaml
  spring:
    jpa:
      hibernate:
        ddl-auto: validate          # Hibernate більше НЕ створює схему — лише перевіряє
    liquibase:
      change-log: classpath:db/changelog/db.changelog-master.yaml
  ```

При першому старті Liquibase сам створює дві **службові таблиці**:
- `DATABASECHANGELOG` — історія застосованих changeSets;
- `DATABASECHANGELOGLOCK` — блокування, щоб два застосунки не мігрували одночасно.

### 3. ChangeSets

Структура у `src/main/resources/db/changelog/`:
```
db.changelog-master.yaml        головний журнал — підключає файли по порядку
├─ 001-create-catalog.yaml      categories + products (+ FK)
├─ 002-create-orders.yaml       customers + orders + order_items (+ FK)
└─ 003-add-indexes.yaml         індекси на зовнішні ключі
```

Кожен changeSet має **обовʼязкові `id` та `author`** (разом унікально його ідентифікують):
```yaml
- changeSet:
    id: 001-create-categories
    author: course
    changes:
      - createTable:
          tableName: categories
          columns:
            - column: { name: id,   type: bigint, autoIncrement: true,
                        constraints: { primaryKey: true, nullable: false } }
            - column: { name: name, type: varchar(100),
                        constraints: { nullable: false, unique: true } }
    rollback:                      # як відкотити цей changeSet
      - dropTable: { tableName: categories }
```

### 4. Rollback та узгодження з JPA

- **`rollback`-секція** описує, як відкотити зміну (для `createTable` — `dropTable`).
- **`ddl-auto: validate`** означає: Hibernate лише **перевіряє**, що таблиці з Liquibase
  відповідають сутностям, і нічого сам не створює. Якщо схема й сутності розійдуться —
  застосунок впаде на старті з помилкою schema-validation (це добре: ловимо розбіжність рано).

### 5. Запуск (ОБОВʼЯЗКОВО з чистої БД)

```bash
docker compose down -v     # видалити стару схему/дані (важливо!)
docker compose up -d
mvn spring-boot:run
```

У логах побачите, як Liquibase застосовує changeSets:
```
Table categories created
ChangeSet ...001-create-categories::course ran successfully
...
```

Перевірка історії міграцій:
```bash
docker compose exec db psql -U shop -d shop -c "SELECT id, author FROM databasechangelog ORDER BY orderexecuted;"
```
Очікуємо **7 рядків** (усі наші changeSets).

> **Ідемпотентність.** Перезапустіть застосунок без `down -v` — Liquibase скаже
> *«Database is up to date, no changesets to execute»* і нічого не застосує повторно.
> Це й є суть міграцій: кожна зміна застосовується рівно один раз.

---

## Частина 2. Проблема N+1

### 6. Що це і як відтворити

Нагадаємо: `Order.items` — **LAZY**. Якщо завантажити список замовлень звичайним `findAll()`,
а потім для кожного звернутись до `order.getItems()`, Hibernate піде в базу **окремо** за
позиціями кожного замовлення:

```
1 запит:  select ... from orders
+ N запитів: select ... from order_items where order_id = ?   (по одному на КОЖНЕ замовлення)
```

Разом **1 + N**. На трьох замовленнях це 4 запити; на тисячі — 1001. Система «тихо»
деградує з ростом даних.

Код (`NPlusOneDemoService.countItemsNaive`):
```java
List<Order> orders = orderRepository.findAll();   // 1 запит
for (Order o : orders) total += o.getItems().size();  // +1 запит на кожне замовлення
```

**Виміряно в цьому модулі** (3 замовлення в демо-даних):

| Стратегія | Кількість SQL-запитів |
|---|---|
| naive (`findAll` + LAZY) | **4** = 1 + 3 (N+1) |
| `JOIN FETCH` | **1** |
| `@EntityGraph` | **1** |

Усі три повертають однаковий правильний результат (`totalItems = 5`).

### 7. Усунення N+1

**Варіант 1 — `JOIN FETCH`** (JPQL): одним запитом тягнемо замовлення РАЗОМ із позиціями.
```java
@Query("select distinct o from Order o join fetch o.items")
List<Order> findAllWithItemsJoinFetch();
```
`distinct` прибирає дублі рядків, що виникають при join колекції.

**Варіант 2 — `@EntityGraph`** (декларативно): кажемо, які звʼязки підвантажити одразу.
```java
@EntityGraph(attributePaths = "items")
@Query("select o from Order o")
List<Order> findAllWithItemsEntityGraph();
```

Обидва дають **один** запит із `JOIN` замість 1+N.

### 8. SQL-місток: ДО і ПІСЛЯ

**ДО (N+1):**
```sql
select ... from orders                                 -- 1
select ... from order_items where order_id = ?         -- +1 (замовлення №1)
select ... from order_items where order_id = ?         -- +1 (замовлення №2)
select ... from order_items where order_id = ?         -- +1 (замовлення №3)
```

**ПІСЛЯ (JOIN FETCH / @EntityGraph):**
```sql
select distinct o.*, i.*
from orders o
join order_items i on i.order_id = o.id                -- один запит
```

### 9. Перевірка

```bash
curl -s http://localhost:8080/api/nplus1/naive         # порахуйте "select ... from order_items" у логах
curl -s http://localhost:8080/api/nplus1/join-fetch    # буде один запит
curl -s http://localhost:8080/api/nplus1/entity-graph  # буде один запит
```

Після кожного виклику **порахуйте в консолі** рядки `Hibernate: ` — так і діагностують N+1.

---

## Типові помилки

| Симптом | Причина | Як виправити |
|---|---|---|
| `Schema-validation: missing table/column` | Схема Liquibase не збігається із сутностями | Привести changeSet у відповідність до `@Entity` |
| Таблиці вже існують, Liquibase падає | БД не чиста (лишилась від `ddl-auto`) | `docker compose down -v`, потім старт |
| `Validation Failed: ... checksum` | Відредагували **вже застосований** changeSet | Не редагувати застосовані; додати **новий** changeSet. У крайньому разі — `clearCheckSums` |
| `MultipleBagFetchException` | Два `JOIN FETCH` List-колекцій одночасно | Використати `Set` або `@EntityGraph`; одна колекція на запит |
| N+1 «не зникає» | Доступ до LAZY поза fetch-запитом | Переконайтесь, що звертаєтесь до `findAllWithItems...`, а не `findAll` |

---

## Завдання для самостійного виконання

1. **Liquibase.** Перенесіть схему проєкту на Liquibase (щонайменше 2 changeSets),
   переведіть Hibernate у `validate` і запустіть з **чистої** БД. Переконайтесь, що
   `DATABASECHANGELOG` заповнена, а застосунок стартує без помилок валідації.
2. **N+1.** Відтворіть N+1 на `Order → OrderItems` (ендпоінт `/naive`), порахувавши запити
   в логах. Усуньте його через `@EntityGraph` **або** `JOIN FETCH` і підтвердіть зниження
   кількості запитів (до одного) у логах.

---

## Чеклист DoD

- [ ] Міграції застосовуються з **чистої** БД; усі 7 changeSets у `DATABASECHANGELOG`.
- [ ] `ddl-auto: validate` не падає (схема відповідає сутностям).
- [ ] Повторний старт не застосовує changeSets удруге («up to date»).
- [ ] N+1 відтворено: у логах видно 1 + N запитів на `/naive`.
- [ ] Після `JOIN FETCH` / `@EntityGraph` — **один** запит; результат той самий.

---

## Підсумок курсу

Ви пройшли шлях від **чистого SQL руками** до повноцінного рівня доступу до даних:

1. **SQL руками** — таблиці, звʼязки, запити, `GROUP BY`.
2. **JDBC vs ORM** — зʼєднання, сесія, транзакція.
3. **Перша `@Entity`** — анотація → `CREATE TABLE`.
4. **CRUD** — репозиторій, сервіс, контролер, DTO.
5. **Асоціації** — звʼязки, життєвий цикл, LAZY/EAGER.
6. **Запити** — derived, JPQL, `@Query`, проєкції, пагінація, `GROUP BY`.
7. **Liquibase + N+1** — міграції схеми та продуктивність.

Наскрізна ідея лишалась одна: **ORM — не магія**. За кожною анотацією й методом стоїть
конкретний SQL, який ви вмієте побачити в логах і, за потреби, виправити.
