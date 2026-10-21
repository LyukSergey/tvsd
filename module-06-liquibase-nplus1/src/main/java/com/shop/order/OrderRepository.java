package com.shop.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ПОГАНО (N+1): успадкований findAll() завантажує лише замовлення (1 запит).
    // Коли потім для КОЖНОГО замовлення звернутись до order.getItems() (LAZY),
    // піде ще по одному SELECT на кожне замовлення -> 1 + N запитів.
    // Окремо його не оголошуємо — показуємо ефект у контролері.

    /**
     * ДОБРЕ, варіант 1 — JOIN FETCH: одним запитом тягнемо замовлення РАЗОМ із позиціями.
     * distinct прибирає дублі рядків, що виникають через join колекції.
     */
    @Query("select distinct o from Order o join fetch o.items")
    List<Order> findAllWithItemsJoinFetch();

    /**
     * ДОБРЕ, варіант 2 — @EntityGraph: декларативно вказуємо, які звʼязки підвантажити
     * одразу. Spring Data сам побудує fetch. Результат — той самий SQL із JOIN.
     */
    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o")
    List<Order> findAllWithItemsEntityGraph();
}
