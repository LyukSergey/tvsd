package com.shop.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Демонструє проблему N+1 і два способи її усунення.
 * Кожен метод рахує сумарну кількість позицій у всіх замовленнях — для цього
 * мусить звернутись до order.getItems() кожного замовлення. Різниця лише в тому,
 * ЯК завантажено дані (і скільки SQL-запитів через це піде).
 */
@Service
public class NPlusOneDemoService {

    private final OrderRepository orderRepository;

    public NPlusOneDemoService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * ПОГАНО. findAll() -> 1 запит на замовлення. Далі getItems() для кожного ->
     * ще по одному SELECT. Разом: 1 + N. Дивіться лог: багато "select ... from order_items".
     */
    @Transactional(readOnly = true)
    public int countItemsNaive() {
        List<Order> orders = orderRepository.findAll();   // 1 запит
        int total = 0;
        for (Order o : orders) {
            total += o.getItems().size();                 // +1 запит на КОЖНЕ замовлення
        }
        return total;
    }

    /** ДОБРЕ (JOIN FETCH). Один запит із JOIN — позиції вже підвантажені. */
    @Transactional(readOnly = true)
    public int countItemsJoinFetch() {
        List<Order> orders = orderRepository.findAllWithItemsJoinFetch();  // 1 запит
        int total = 0;
        for (Order o : orders) {
            total += o.getItems().size();                                  // без додаткових запитів
        }
        return total;
    }

    /** ДОБРЕ (@EntityGraph). Те саме, що JOIN FETCH, але декларативно. */
    @Transactional(readOnly = true)
    public int countItemsEntityGraph() {
        List<Order> orders = orderRepository.findAllWithItemsEntityGraph();
        int total = 0;
        for (Order o : orders) {
            total += o.getItems().size();
        }
        return total;
    }
}
