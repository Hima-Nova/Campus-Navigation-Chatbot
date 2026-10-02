package com.campusnav.controller;

import com.campusnav.client.PythonClassifierClient;
import com.campusnav.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final PythonClassifierClient pythonClient;
    private final LocationService locationService;

    public HealthController(PythonClassifierClient pythonClient, LocationService locationService) {
        this.pythonClient = pythonClient;
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean pythonOnline = pythonClient.isHealthy();
        Map<String, Object> stats = locationService.getGraphStats();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "Campus Navigation Chatbot Backend",
                "version", "1.0.0",
                "academicPillars", Map.of(
                        "AI", "Intelligent Agent & Python Keyword NLP Classifier",
                        "ADSA", "Graph Representation, BFS Shortest Path, DFS Traversal",
                        "OOPJ", "Java Spring Boot, Strategy Pattern, Domain Model Encapsulation",
                        "Python", "Intent Classification Microservice (REST API)"
                ),
                "pythonServiceStatus", pythonOnline ? "CONNECTED" : "OFFLINE (Java Fallback Active)",
                "graphNodes", stats.get("totalLocations"),
                "graphEdges", stats.get("totalEdges")
        ));
    }
}
