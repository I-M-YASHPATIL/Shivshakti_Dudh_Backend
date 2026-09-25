package com.dairy.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Dairy Management System is running successfully!";
    }

    @GetMapping("/api")
    public String apiHome() {
        return "Dairy API is running successfully!";
    }
}