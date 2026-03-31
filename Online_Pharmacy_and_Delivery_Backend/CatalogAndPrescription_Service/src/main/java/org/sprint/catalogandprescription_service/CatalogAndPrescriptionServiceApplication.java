package org.sprint.catalogandprescription_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatalogAndPrescriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogAndPrescriptionServiceApplication.class, args);
    }

}