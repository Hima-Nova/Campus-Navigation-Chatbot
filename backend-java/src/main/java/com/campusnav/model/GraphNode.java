package com.campusnav.model;

import java.util.Objects;

/**
 * Represents a Vertex (Node) in the Campus Graph (ADSA).
 */
public class GraphNode {
    private final String id;
    private final String name;
    private CampusLocation location;

    public GraphNode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public GraphNode(CampusLocation location) {
        this.id = location.getId();
        this.name = location.getName();
        this.location = location;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public CampusLocation getLocation() { return location; }
    public void setLocation(CampusLocation location) { this.location = location; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return Objects.equals(name.toLowerCase(), graphNode.name.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name;
    }
}
