package com.campusnav.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CampusLocation {
    private String id;
    private String name;
    private LocationCategory category;
    private String block;
    private String floor;
    private String description;
    private String timings;
    private List<String> facilities = new ArrayList<>();
    private List<String> aliases = new ArrayList<>();
    private Map<String, Double> mapCoordinates;

    public CampusLocation() {}

    public CampusLocation(String id, String name, LocationCategory category, String block, 
                          String floor, String description, String timings) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.block = block;
        this.floor = floor;
        this.description = description;
        this.timings = timings;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocationCategory getCategory() { return category; }
    public void setCategory(LocationCategory category) { this.category = category; }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimings() { return timings; }
    public void setTimings(String timings) { this.timings = timings; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities != null ? facilities : new ArrayList<>(); }

    public List<String> getAliases() { return aliases; }
    public void setAliases(List<String> aliases) { this.aliases = aliases != null ? aliases : new ArrayList<>(); }

    public Map<String, Double> getMapCoordinates() { return mapCoordinates; }
    public void setMapCoordinates(Map<String, Double> mapCoordinates) { this.mapCoordinates = mapCoordinates; }

    @Override
    public String toString() {
        return "CampusLocation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", block='" + block + '\'' +
                ", floor='" + floor + '\'' +
                '}';
    }
}
