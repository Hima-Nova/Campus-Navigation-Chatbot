package com.campusnav.model;

public class NavigationRequest {
    private String source;
    private String destination;
    private String algorithm = "BFS"; // "BFS" or "DFS"

    public NavigationRequest() {}

    public NavigationRequest(String source, String destination, String algorithm) {
        this.source = source;
        this.destination = destination;
        this.algorithm = algorithm != null ? algorithm : "BFS";
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}
