package com.campusnav.client;

import com.campusnav.model.IntentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * REST Client for communicating with the Python Keyword-Based Intent Classification Microservice.
 * Demonstrates Microservice Communication & Fault-Tolerant Fallback Architecture.
 */
@Component
public class PythonClassifierClient {
    private static final Logger logger = LoggerFactory.getLogger(PythonClassifierClient.class);

    private final RestTemplate restTemplate;

    @Value("${python.classifier.url:http://localhost:5000/classify}")
    private String classifierUrl;

    @Value("${python.classifier.health-url:http://localhost:5000/health}")
    private String healthUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofMillis(3000))
            .build();

    public PythonClassifierClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the raw query to the Python classifier service via REST POST.
     * If the Python service is offline, falls back gracefully to internal Java rule-based classification.
     */
    public IntentResult classify(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new IntentResult("UNKNOWN", 0.0, "Empty query received");
        }

        try {
            Map<String, String> payload = Map.of("query", query);
            String jsonPayload = objectMapper.writeValueAsString(payload);

            logger.info("Calling Python Intent Classifier at '{}' with query: '{}'", classifierUrl, query);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(classifierUrl))
                    .timeout(java.time.Duration.ofMillis(3000))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<String> response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body() != null) {
                IntentResult result = objectMapper.readValue(response.body(), IntentResult.class);
                result.setStatus("CONNECTED");
                logger.info("Python Classifier returned intent: '{}' (Confidence: {})",
                        result.getIntent(), result.getConfidence());
                return result;
            }
        } catch (Exception e) {
            logger.warn("Python Classifier microservice at '{}' is unavailable ({}). Activating Java Fallback Classifier.",
                    classifierUrl, e.getMessage());
        }

        // Fallback: Internal Java Keyword Classifier
        return fallbackClassify(query);
    }

    /**
     * Checks if Python microservice is online.
     */
    public boolean isHealthy() {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(healthUrl))
                    .timeout(java.time.Duration.ofMillis(2000))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Embedded Java Rule-Based Fallback Classifier to ensure zero system failure if Python service is stopped.
     */
    public IntentResult fallbackClassify(String query) {
        String normalized = query.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();

        // 1. Timing
        if (Pattern.compile("\\b(timings?|time|hours?|working hours?|open(?:ing)?|clos(?:ing|es?)|schedule|when does|what time)\\b").matcher(normalized).find()) {
            IntentResult res = new IntentResult("TIMING", 0.90, "Matched timing keywords (Java Fallback Engine)");
            res.setStatus("FALLBACK");
            return res;
        }

        // 2. Directions
        if (Pattern.compile("\\b(how (?:do|can) i (?:reach|get to|go to|find)|take me to|navigate to|guide me to|route|path|directions?|how to reach|shortest route|walk to|way to|go from|lead me to)\\b").matcher(normalized).find() ||
            normalized.contains("reach") || normalized.contains("route") || normalized.contains("navigate") || normalized.contains("direction")) {
            IntentResult res = new IntentResult("DIRECTIONS", 0.90, "Matched navigation keywords (Java Fallback Engine)");
            res.setStatus("FALLBACK");
            return res;
        }

        // 3. Location Search
        if (Pattern.compile("\\b(where is|where are|locate|find|location of|which block|which floor|room number|search for)\\b").matcher(normalized).find() ||
            normalized.startsWith("where ") || normalized.startsWith("locate ")) {
            IntentResult res = new IntentResult("LOCATION_SEARCH", 0.85, "Matched location search keywords (Java Fallback Engine)");
            res.setStatus("FALLBACK");
            return res;
        }

        // 4. General Info
        if (Pattern.compile("\\b(tell me about|what is|details?|information|info about|describe|about the|facilities in|overview|department)\\b").matcher(normalized).find() ||
            normalized.contains("about") || normalized.contains("details") || normalized.contains("info")) {
            IntentResult res = new IntentResult("GENERAL", 0.80, "Matched general info keywords (Java Fallback Engine)");
            res.setStatus("FALLBACK");
            return res;
        }

        IntentResult unknown = new IntentResult("UNKNOWN", 0.20, "No keywords matched in Java Fallback Engine");
        unknown.setStatus("FALLBACK");
        return unknown;
    }
}
