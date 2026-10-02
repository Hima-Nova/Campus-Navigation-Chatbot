package com.campusnav.model;

import java.util.ArrayList;
import java.util.List;

public class NavigationResponse {
    private boolean found;
    private String algorithm;
    private String source;
    private String destination;
    private List<String> path = new ArrayList<>();
    private List<CampusLocation> nodeDetails = new ArrayList<>();
    private int steps;
    private int distanceMeters;
    private int estimatedMinutes;
    private List<String> turnByTurnDirections = new ArrayList<>();
    private String message;
    private String timeComplexity;
    private String spaceComplexity;

    public NavigationResponse() {}

    public static NavigationResponse notFound(String algorithm, String source, String destination, String message) {
        NavigationResponse resp = new NavigationResponse();
        resp.setFound(false);
        resp.setAlgorithm(algorithm);
        resp.setSource(source);
        resp.setDestination(destination);
        resp.setMessage(message);
        return resp;
    }

    // Getters and Setters
    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public List<String> getPath() { return path; }
    public void setPath(List<String> path) { this.path = path != null ? path : new ArrayList<>(); }

    public List<CampusLocation> getNodeDetails() { return nodeDetails; }
    public void setNodeDetails(List<CampusLocation> nodeDetails) { this.nodeDetails = nodeDetails != null ? nodeDetails : new ArrayList<>(); }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public int getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(int distanceMeters) { this.distanceMeters = distanceMeters; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public List<String> getTurnByTurnDirections() { return turnByTurnDirections; }
    public void setTurnByTurnDirections(List<String> turnByTurnDirections) { this.turnByTurnDirections = turnByTurnDirections != null ? turnByTurnDirections : new ArrayList<>(); }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimeComplexity() { return timeComplexity; }
    public void setTimeComplexity(String timeComplexity) { this.timeComplexity = timeComplexity; }

    public String getSpaceComplexity() { return spaceComplexity; }
    public void setSpaceComplexity(String spaceComplexity) { this.spaceComplexity = spaceComplexity; }
}
