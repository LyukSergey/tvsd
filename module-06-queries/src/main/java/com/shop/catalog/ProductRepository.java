package com.shop.catalog;

import com.shop.catalog.dto.CategoryStats;
import com.shop.catalog.dto.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Репозиторій Product — вітрина СПОСОБІВ писати запити у Spring Data.
 * Для кожного способу в README наведено згенерований SQL ("SQL-місток").
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    // =================================================================
    // 1. DERIVED QUERY METHODS — Spring будує запит із НАЗВИ методу.
    //    Нічого не реалізуємо: достатньо правильно назвати метод.
    // =================================================================

    /** -> where name = ? */
    List<Product> findByName(String name);

    /** -> where price > ? */
    List<Product> findByPriceGreaterThan(BigDecimal price);

    /** -> where lower(name) like lower('%?%') */
    List<Product> findByNameContainingIgnoreCase(String part);

    /** Навігація по асоціації: -> join categories ... where category.name = ? */
    List<Product> findByCategoryName(String categoryName);

    /** Кілька умов + сортування: -> where price between ? and ? order by price */
    List<Product> findByPriceBetweenOrderByPriceAsc(BigDecimal from, BigDecimal to);

    /** Обмеження кількості: -> order by price desc limit 3 */
    List<Product> findTop3ByOrderByPriceDesc();

    // =================================================================
    // 2. JPQL через @Query — пишемо запит по СУТНОСТЯХ (не по таблицях).
    // =================================================================

    /** JPQL: навігація p.category.name; :name — іменований параметр. */
    @Query("select p from Product p where p.category.name = :name")
    List<Product> findInCategory(@Param("name") String name);

    // =================================================================
    // 3. ПРОЄКЦІЯ — тягнемо лише потрібні стовпці у DTO (не всю сутність).
    //    -> select name, price (а не select *)
    // =================================================================

    @Query("select new com.shop.catalog.dto.ProductSummary(p.name, p.price) " +
           "from Product p order by p.price desc")
    List<ProductSummary> findPriceList();

    // =================================================================
    // 4. АГРЕГАЦІЯ / GROUP BY — ті самі підсумки, що руками в модулі 01,
    //    але через JPQL. Повертаємо у DTO CategoryStats.
    // =================================================================

    /**
     * Кількість товарів і середня ціна по кожній категорії.
     * HAVING фільтрує вже ГОТОВІ групи (категорії, де > :min товарів).
     */
    @Query("""
           select new com.shop.catalog.dto.CategoryStats(c.name, count(p), avg(p.price))
           from Category c join c.products p
           group by c.name
           having count(p) > :min
           order by count(p) desc
           """)
    List<CategoryStats> categoryStats(@Param("min") long min);

    // =================================================================
    // 5. ПАГІНАЦІЯ — Spring сам додасть limit/offset та окремий count(*).
    // =================================================================

    /** Передаємо Pageable (сторінка/розмір/сортування) — отримуємо Page. */
    Page<Product> findByPriceGreaterThan(BigDecimal price, Pageable pageable);

    // =================================================================
    // 6. НАТИВНИЙ SQL — коли треба "чистий" SQL (напр., специфічний для СУБД).
    // =================================================================

    @Query(value = "select * from products where price > :price", nativeQuery = true)
    List<Product> findExpensiveNative(@Param("price") BigDecimal price);
}
