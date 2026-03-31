package com.pharmacy.admin;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Admin & Reporting Microservice - Port 8084
 *
 * Responsibilities:
 *  - Medicine catalog CRUD (admin only)
 *  - Inventory / expiry / low-stock alerts
 *  - Order lifecycle management and status transitions
 *  - Prescription approval queue
 *  - Dashboard KPIs
 *  - Sales, inventory, and prescription reports
 */
@SpringBootApplication
@EnableFeignClients
@EnableRabbit
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
