package com.algobrewery.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the Roles & Permissions Service.
 * Provides centralized authorization management for Task Silo services.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class RolesPermissionsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RolesPermissionsServiceApplication.class, args);
    }
}
