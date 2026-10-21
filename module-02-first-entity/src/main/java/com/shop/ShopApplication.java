package com.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входу застосунку Spring Boot.
 * <p>
 * Анотація @SpringBootApplication вмикає автоконфігурацію: Spring сам побачить
 * залежність spring-boot-starter-data-jpa, прочитає application.yml, підключиться
 * до бази й за потреби створить таблиці під наші @Entity.
 */
@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
