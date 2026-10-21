package com.shop.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Ендпоінти для спостереження за N+1. Викликайте кожен і РАХУЙТЕ в консолі
 * кількість рядків "Hibernate: select ... from order_items".
 */
@RestController
@RequestMapping("/api/nplus1")
public class NPlusOneDemoController {

    private final NPlusOneDemoService service;

    public NPlusOneDemoController(NPlusOneDemoService service) {
        this.service = service;
    }

    /** ПОГАНО: 1 + N запитів. */
    @GetMapping("/naive")
    public Map<String, Object> naive() {
        return Map.of("strategy", "naive (N+1)", "totalItems", service.countItemsNaive());
    }

    /** ДОБРЕ: 1 запит (JOIN FETCH). */
    @GetMapping("/join-fetch")
    public Map<String, Object> joinFetch() {
        return Map.of("strategy", "JOIN FETCH", "totalItems", service.countItemsJoinFetch());
    }

    /** ДОБРЕ: 1 запит (@EntityGraph). */
    @GetMapping("/entity-graph")
    public Map<String, Object> entityGraph() {
        return Map.of("strategy", "@EntityGraph", "totalItems", service.countItemsEntityGraph());
    }
}
