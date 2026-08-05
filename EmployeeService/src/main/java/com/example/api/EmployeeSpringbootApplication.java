package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
// Explicitly configures repositories to bind exclusively to the JPA engine
@EnableJpaRepositories(basePackages = "com.example.api.repository")
public class EmployeeSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeSpringbootApplication.class, args);
    }

}
