package com.campusnav.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a Walkable Path (Edge) between two campus locations in the ADSA graph.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphEdge {
    private String source;
    private String destination;
    private int distanceMeters;
    private int walkMinutes;
    private String pathType; // e.g. "Walkway", "Stairs", "Elevator", "Corridor", "Outdoor Trail"

    public GraphEdge() {
        this.pathType = "Paved Walkway";
    }

    public GraphEdge(String source, String destination, int distanceMeters, int walkMinutes) {
        this.source = source;
        this.destination = destination;
        this.distanceMeters = distanceMeters;
        this.walkMinutes = walkMinutes;
        this.pathType = "Paved Walkway";
    }

    public GraphEdge(String source, String destination, int distanceMeters, int walkMinutes, String pathType) {
        this.source = source;
        this.destination = destination;
        this.distanceMeters = distanceMeters;
        this.walkMinutes = walkMinutes;
        this.pathType = pathType;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(int distanceMeters) { this.distanceMeters = distanceMeters; }

    public int getWalkMinutes() { return walkMinutes; }
    public void setWalkMinutes(int walkMinutes) { this.walkMinutes = walkMinutes; }

    public String getPathType() { return pathType; }
    public void setPathType(String pathType) { this.pathType = pathType; }

    @Override
    public String toString() {
        return source + " <--> " + destination + " (" + distanceMeters + "m, " + walkMinutes + "min)";
    }
}
