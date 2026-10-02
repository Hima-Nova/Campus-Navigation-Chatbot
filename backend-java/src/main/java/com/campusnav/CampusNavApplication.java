package com.campusnav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Campus Navigation Chatbot Application
 * 
 * Main Entry Point for the Java Spring Boot Backend.
 * Demonstrates AI Agent, ADSA Graph BFS/DFS, and OOP Principles.
 */
@SpringBootApplication
public class CampusNavApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusNavApplication.class, args);
        System.out.println("===============================================================");
        System.out.println(" 🚀 Campus Navigation AI Backend Started on http://localhost:8080");
        System.out.println(" 🌐 Web UI Available at: http://localhost:8080");
        System.out.println(" 📡 API Base Endpoint:   http://localhost:8080/api");
        System.out.println("===============================================================");
    }
}
