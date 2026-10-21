package com.shop.product;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторій для Product.
 * <p>
 * Ми лише ОГОЛОШУЄМО інтерфейс — реалізацію Spring Data згенерує автоматично.
 * Успадкувавши JpaRepository<Product, Long>, ми "безкоштовно" отримуємо методи:
 * <ul>
 *   <li>save(entity)        — INSERT або UPDATE</li>
 *   <li>findById(id)        — SELECT ... WHERE id = ?  (повертає Optional)</li>
 *   <li>findAll()           — SELECT ... (усі рядки)</li>
 *   <li>existsById(id)      — перевірка наявності</li>
 *   <li>deleteById(id)      — DELETE ... WHERE id = ?</li>
 *   <li>count()             — SELECT count(*)</li>
 * </ul>
 * Параметри типу: &lt;Product, Long&gt; = &lt;тип сутності, тип її @Id&gt;.
 *
 * Ієрархія інтерфейсів Spring Data:
 *   CrudRepository -> PagingAndSortingRepository -> JpaRepository
 * JpaRepository — найбагатший, його й використовуємо.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Поки що порожньо — усі потрібні методи вже успадковані.
    // Власні методи-запити додамо в модулі 06.
}
