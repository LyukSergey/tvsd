package com.shop.catalog;

import com.shop.catalog.dto.CategoryStats;
import com.shop.catalog.dto.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Демонстраційний контролер — кожен ендпоінт показує один СПОСІБ запиту.
 * Повертаємо лише прості поля (назви/ціни/статистику), щоб не серіалізувати
 * сутності зі звʼязками (уникаємо LazyInitializationException).
 */
@RestController
@RequestMapping("/api/query")
public class QueryDemoController {

    private final ProductRepository products;

    public QueryDemoController(ProductRepository products) {
        this.products = products;
    }

    /** Derived: дорожчі за заданий поріг (тільки назви). */
    @GetMapping("/expensive")
    public List<String> expensive(@RequestParam(defaultValue = "500") BigDecimal price) {
        return products.findByPriceGreaterThan(price).stream().map(Product::getName).toList();
    }

    /** Derived: пошук за частиною назви без урахування регістру. */
    @GetMapping("/search")
    public List<String> search(@RequestParam String q) {
        return products.findByNameContainingIgnoreCase(q).stream().map(Product::getName).toList();
    }

    /** JPQL: товари заданої категорії. */
    @GetMapping("/by-category")
    public List<String> byCategory(@RequestParam String name) {
        return products.findInCategory(name).stream().map(Product::getName).toList();
    }

    /** Проєкція: прайс-лист (назва + ціна), без зайвих стовпців. */
    @GetMapping("/price-list")
    public List<ProductSummary> priceList() {
        return products.findPriceList();
    }

    /** GROUP BY: статистика по категоріях (к-сть + середня ціна), лише групи > min. */
    @GetMapping("/category-stats")
    public List<CategoryStats> categoryStats(@RequestParam(defaultValue = "0") long min) {
        return products.categoryStats(min);
    }

    /** Пагінація: сторінка товарів дорожчих за поріг, із сортуванням за ціною. */
    @GetMapping("/page")
    public List<String> page(@RequestParam(defaultValue = "0") BigDecimal price,
                             @RequestParam(defaultValue = "0") int pageNum,
                             @RequestParam(defaultValue = "3") int size) {
        Page<Product> p = products.findByPriceGreaterThan(
                price, PageRequest.of(pageNum, size, Sort.by("price").descending()));
        return p.getContent().stream()
                .map(pr -> pr.getName() + " (" + pr.getPrice() + ")")
                .toList();
    }
}
