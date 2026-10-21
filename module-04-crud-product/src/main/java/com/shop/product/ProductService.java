package com.shop.product;

import com.shop.product.dto.ProductRequest;
import com.shop.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Шар бізнес-логіки. Стоїть МІЖ контролером і репозиторієм.
 * <p>
 * Навіщо окремий шар, чому не ходити з контролера прямо в репозиторій?
 * <ul>
 *   <li>тут зосереджена логіка й межі транзакцій (@Transactional);</li>
 *   <li>контролер лишається тонким (тільки HTTP), репозиторій — тільки доступ до БД;</li>
 *   <li>логіку легко тестувати окремо від вебу.</li>
 * </ul>
 * Нагадування про транзакції: @Transactional працює лише на PUBLIC-методах
 * і не спрацює при виклику методу "самого себе" зсередини того ж класу.
 */
@Service
@Transactional(readOnly = true)   // за замовчуванням методи лише читають; для запису перевизначаємо нижче
public class ProductService {

    private final ProductRepository repository;

    // Конструкторне впровадження залежності (Spring сам передасть репозиторій).
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    /** Усі товари. -> SELECT ... FROM products */
    public List<ProductResponse> findAll() {
        return repository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    /** Один товар за id. -> SELECT ... WHERE id = ? ; якщо немає — 404. */
    public ProductResponse findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductResponse.from(product);
    }

    /** Створити товар. -> INSERT. @Transactional без readOnly, бо це запис. */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name(), request.price(), request.quantity());
        Product saved = repository.save(product);   // save нового обʼєкта -> INSERT
        return ProductResponse.from(saved);
    }

    /**
     * Оновити товар. -> SELECT (завантажити) + UPDATE.
     * Зверніть увагу: явного save() НЕ викликаємо. Поки ми в транзакції, обʼєкт
     * "керований" (managed), і Hibernate сам помітить зміни полів (dirty checking)
     * та згенерує UPDATE при завершенні транзакції. Детально — у модулі 05.
     */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setName(request.name());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        return ProductResponse.from(product);
    }

    /** Видалити товар. -> DELETE ; якщо немає — 404. */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
