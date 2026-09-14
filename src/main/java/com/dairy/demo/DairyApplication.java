package com.dairy.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DairyApplication {
    public static void main(String[] args) {
        SpringApplication.run(DairyApplication.class, args);
        System.out.println("=========================================");
        System.out.println(" Dairy Management System Started!");
        System.out.println(" दूध संकलन व्यवस्थापन प्रणाली सुरू झाली!");
        System.out.println(" API: http://localhost:8082/api");
        System.out.println("=========================================");
    }
}