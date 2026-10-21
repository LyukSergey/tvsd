# Module 00 — Підготовка середовища

Нульовий модуль. Тут немає нової теорії з баз даних — лише встановлюємо все потрібне
й перевіряємо, що воно працює. Пройдіть його один раз, і всі наступні модулі
запускатимуться без сюрпризів.

## Що навчимося
- Встановити й перевірити **JDK 17**, **Docker**, середовище розробки (**IntelliJ IDEA**).
- Отримати код курсу (клонувати git-репозиторій або завантажити архів).
- Запустити базу даних **PostgreSQL** у Docker.
- Вмикати й **читати лог згенерованого SQL** — ключовий інструмент усього курсу.

---

## 1. Встановлення JDK 17

Java Development Kit (JDK) — те, чим компілюється й запускається Java-код. Нам потрібна версія **17**.

### Встановлення

- **Windows / macOS / Linux:** завантажте «Temurin 17 (LTS)» з https://adoptium.net/ і встановіть
  стандартним інсталятором.
- **macOS (через Homebrew):**
  ```bash
  brew install --cask temurin@17
  ```
- **Linux (Debian/Ubuntu):**
  ```bash
  sudo apt update && sudo apt install -y temurin-17-jdk
  ```

### Перевірка

```bash
java -version
```

Очікуваний вивід (цифри після `17.` можуть відрізнятися):

```
openjdk version "17.0.11" 2024-04-16
OpenJDK Runtime Environment Temurin-17.0.11+9 ...
```

> **Якщо бачите версію 8, 11, 21 чи 25** — на комп'ютері активна інша Java. Встановіть саме 17
> і переконайтеся, що змінна середовища `JAVA_HOME` вказує на неї. В IntelliJ IDEA версію JDK
> можна обрати окремо для проєкту (див. крок 4), тож навіть за «неправильної» системної Java
> проєкт збереться коректно.
>
> **Важливо (новіші JDK + Lombok).** На JDK 21/25 збірка модулів із Lombok може впасти з
> помилкою `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`.
> Причина — Lombok ще не підтримує найновіші JDK. Рішення: збирати саме під **JDK 17**.
> Якщо 17 встановлено поряд з іншими, оберіть її для конкретної команди:
> ```bash
> # macOS/Linux — підставити JDK 17 лише для поточного терміналу:
> export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS
> mvn spring-boot:run
> ```
> В IntelliJ IDEA це не проблема: у **Project Structure → Project SDK** оберіть 17.

---

## 2. Встановлення Docker

**Docker** дозволяє запустити PostgreSQL в ізольованому контейнері — не треба встановлювати
саму базу в систему. Один файл `docker-compose.yml` описує, яку базу підняти, і вона
стартує однією командою.

### Встановлення

- **Windows / macOS:** завантажте **Docker Desktop** з https://www.docker.com/products/docker-desktop/
  і встановіть. Після встановлення **запустіть Docker Desktop** — у треї має з'явитися значок кита.
- **Linux:** встановіть Docker Engine за інструкцією https://docs.docker.com/engine/install/
  та плагін `docker compose`.

### Перевірка

```bash
docker --version
docker compose version
```

Очікуваний вивід (приблизно):

```
Docker version 27.0.3, build ...
Docker Compose version v2.28.1
```

> **`command not found` або помилка з'єднання** — Docker Desktop не запущений. Відкрийте застосунок
> Docker Desktop і дочекайтеся, поки значок кита перестане «крутитися» (статус *Running*).

---

## 3. Інструмент для перегляду бази (необов'язково, але рекомендовано)

Щоб бачити таблиці й виконувати SQL у зручному вікні, встановіть **DBeaver Community** —
безкоштовний клієнт для будь-яких баз: https://dbeaver.io/download/

Альтернатива: вбудований **Database tool** в IntelliJ IDEA (вкладка *Database* праворуч).

Параметри підключення до нашої бази (однакові для всього курсу):

| Параметр | Значення |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `shop` |
| User | `shop` |
| Password | `shop` |

---

## 4. Середовище розробки — IntelliJ IDEA

Для модулів із Java-кодом (починаючи з `module-02`) знадобиться IDE.

1. Завантажте **IntelliJ IDEA Community Edition** (безкоштовна):
   https://www.jetbrains.com/idea/download/
2. Встановіть і запустіть.
3. Відкрийте теку модуля: **File → Open** → оберіть теку, наприклад `module-02-first-entity`
   (ту, де лежить `pom.xml`). IntelliJ сам розпізнає Maven-проєкт і підтягне залежності.
4. Переконайтеся, що обрано **JDK 17**: **File → Project Structure → Project → SDK → 17**.
   Якщо 17 немає у списку — натисніть **Add SDK → Download JDK → Version 17**.

> Частина A модуля `module-01` (SQL руками) IDE не потребує — достатньо Docker і клієнта
> бази. IDE знадобиться з частини B (JDBC-демо) та в наступних модулях.

---

## 5. Як отримати код курсу

### Варіант А — клонувати git-репозиторій (рекомендовано)

`git` — система контролю версій. Якщо його ще немає:
- Windows: https://git-scm.com/download/win
- macOS: `brew install git` (або встановиться разом із Xcode Command Line Tools)
- Linux: `sudo apt install git`

Перевірка:
```bash
git --version
```

Клонування (замініть `<URL>` на адресу репозиторію, яку вам дали):
```bash
git clone <URL>
cd tvsd
```

`git clone` **завантажує весь репозиторій** у нову теку `tvsd`. Команда `cd tvsd` переходить
усередину неї — далі всі команди виконуємо звідти.

### Варіант Б — завантажити архів

Якщо git встановлювати не хочете: на сторінці репозиторію натисніть **Code → Download ZIP**,
розпакуйте архів — отримаєте ту саму структуру тек.

### Структура після отримання коду

```
tvsd/
├─ README.md               ← загальний опис курсу (почніть із нього)
├─ module-00-setup/        ← ви тут
├─ module-01-rdb-jdbc-orm/
├─ module-02-first-entity/
└─ ...
```

---

## 6. Запуск бази даних PostgreSQL

У кожному модулі, де потрібна база, лежить файл `docker-compose.yml`. Еталонний варіант —
поруч із цією методичкою. Щоб підняти базу:

```bash
docker compose up -d
```

- `up` — створити й запустити контейнери, описані в `docker-compose.yml`.
- `-d` (detached) — у фоновому режимі, щоб термінал залишився вільним.

Перевірте, що контейнер працює:

```bash
docker ps
```

Очікуваний вивід — рядок із образом `postgres:16` і статусом `Up`:

```
CONTAINER ID   IMAGE         ...   STATUS         PORTS                    NAMES
a1b2c3d4e5f6   postgres:16   ...   Up 5 seconds   0.0.0.0:5432->5432/tcp   shop-postgres
```

Зупинити базу, **зберігши дані**:
```bash
docker compose down
```

Зупинити й **повністю видалити дані** (знадобиться, щоб почати з чистого аркуша —
особливо в модулі про Liquibase):
```bash
docker compose down -v
```

> `-v` видаляє *volume* — місце, де PostgreSQL зберігає файли бази. Після цього база
> стартує абсолютно порожньою.

---

## 7. Як читати лог згенерованого SQL

Це — головний інструмент курсу. У модулях із Hibernate ми завжди хочемо бачити,
**який SQL насправді виконується**. За це відповідають налаштування в `application.yml`:

```yaml
spring:
  jpa:
    show-sql: true                 # друкувати SQL у консоль
    properties:
      hibernate:
        format_sql: true           # форматувати SQL (з переносами рядків, читабельно)

logging:
  level:
    org.hibernate.SQL: DEBUG                         # сам текст SQL-запитів
    org.hibernate.orm.jdbc.bind: TRACE               # значення параметрів замість "?"
```

Що це дає в консолі:

```
Hibernate:
    select
        p1_0.id,
        p1_0.name,
        p1_0.price
    from
        products p1_0
    where
        p1_0.id=?
binding parameter [1] as [BIGINT] - [42]
```

- рядок `Hibernate: ... select ...` — сам запит, який пішов у базу;
- рядок `binding parameter [1] ... [42]` — яке значення підставилось замість `?`.

**Навіщо це вміти:** коли на пізніх заняттях з'явиться проблема **N+1** (один зайвий запит
перетворюється на сотні), ми діагностуватимемо її саме так — **рахуючи кількість рядків
`Hibernate: select`** у логах.

---

## Типові помилки

| Симптом | Причина | Що робити |
|---|---|---|
| `docker: command not found` | Docker не встановлено / не запущено | Запустіть Docker Desktop, дочекайтеся статусу *Running* |
| `Error ... port is already allocated` (порт 5432) | Порт зайнятий іншою базою | Зупиніть стару базу або змініть порт у `docker-compose.yml` (напр. `5433:5432`). Перевірити, хто зайняв: `lsof -i :5432` (macOS/Linux) |
| `java: error: release version 17 not supported` | Обрано не ту JDK | У IntelliJ: **Project Structure → Project SDK → 17** |
| Контейнер стартує і одразу падає | Пошкоджений volume | `docker compose down -v`, потім `docker compose up -d` |

---

## Чеклист готовності

- [ ] `java -version` показує версію 17.
- [ ] `docker --version` і `docker compose version` працюють.
- [ ] Docker Desktop запущений (статус *Running*).
- [ ] Код курсу завантажено, видно теки `module-00`…`module-07`.
- [ ] `docker compose up -d` у цій теці піднімає контейнер, `docker ps` показує `postgres:16` зі статусом `Up`.
- [ ] (Рекомендовано) DBeaver або Database tool підключається до `localhost:5432`, база `shop`.

---

## Що далі

Переходьте до **[module-01-rdb-jdbc-orm](../module-01-rdb-jdbc-orm/)** — почнемо працювати
з базою напряму мовою SQL: створимо таблиці, наповнимо їх і навчимося писати запити,
включно з агрегацією через `GROUP BY`.
