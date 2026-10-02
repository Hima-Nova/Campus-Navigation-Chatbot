package com.campusnav.model;

public class ChatRequest {
    private String query;
    private String currentLocation; // Optional context: e.g. "Main Gate"
    private String algorithm = "BFS"; // Default algorithm

    public ChatRequest() {}

    public ChatRequest(String query) {
        this.query = query;
    }

    public ChatRequest(String query, String currentLocation) {
        this.query = query;
        this.currentLocation = currentLocation;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}
