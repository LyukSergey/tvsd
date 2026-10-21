package com.shop.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Невеликий сервіс для демонстрації понять модуля: dirty checking і межі сесії.
 */
@Service
public class OrderDemoService {

    private final OrderRepository orderRepository;

    public OrderDemoService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * DIRTY CHECKING: змінюємо поле керованої (managed) сутності — і БЕЗ виклику save()
     * Hibernate сам згенерує UPDATE при завершенні транзакції.
     */
    @Transactional
    public void markOrderShipped(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus("SHIPPED");   // просто змінили поле
        // save() НЕ викликаємо! UPDATE піде автоматично на commit транзакції.
    }

    /**
     * БЕЗПЕЧНЕ читання позицій: доступ до LAZY-колоекції order.getItems()
     * відбувається ВСЕРЕДИНІ транзакції (поки сесія відкрита) — тому підвантаження
     * спрацює без помилки.
     */
    @Transactional(readOnly = true)
    public int countItemsSafely(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        return order.getItems().size();   // LAZY-підвантаження в межах сесії — OK
    }

    /**
     * НЕБЕЗПЕЧНИЙ варіант (демонстрація LazyInitializationException).
     * Тут ми ПОВЕРТАЄМО сутність із транзакції, а до LAZY-колекції звертаємось
     * уже ПІСЛЯ її завершення (у контролері). Див. README — цей метод навмисно
     * показує, як виникає виняток.
     */
    @Transactional(readOnly = true)
    public Order loadOrderWithoutItems(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow();
        // items НЕ чіпаємо тут -> поза транзакцією вони вже недоступні.
    }
}
