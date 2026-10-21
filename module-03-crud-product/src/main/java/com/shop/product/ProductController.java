package com.shop.product;

import com.shop.product.dto.ProductRequest;
import com.shop.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-контролер для товарів. Тонкий шар: приймає HTTP-запити, делегує сервісу,
 * повертає відповіді з коректними HTTP-кодами.
 *
 * Карта ендпоінтів:
 *   GET    /api/products        -> список           (200)
 *   GET    /api/products/{id}   -> один товар        (200 / 404)
 *   POST   /api/products        -> створити          (201)
 *   PUT    /api/products/{id}   -> оновити           (200 / 404)
 *   DELETE /api/products/{id}   -> видалити          (204 / 404)
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = service.create(request);
        // 201 Created — стандартний код для успішного створення ресурсу.
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        // 204 No Content — успішно, тіла відповіді немає.
        return ResponseEntity.noContent().build();
    }
}
