package com.campusnav.service;

import com.campusnav.graph.CampusGraph;
import com.campusnav.model.CampusLocation;
import com.campusnav.model.GraphEdge;
import com.campusnav.model.LocationCategory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final CampusGraph campusGraph;

    public LocationService(CampusGraph campusGraph) {
        this.campusGraph = campusGraph;
    }

    public List<CampusLocation> getAllLocations() {
        return campusGraph.getAllLocations().stream()
                .sorted(Comparator.comparing(CampusLocation::getName))
                .collect(Collectors.toList());
    }

    public Optional<CampusLocation> getLocation(String idOrNameOrAlias) {
        return Optional.ofNullable(campusGraph.getLocation(idOrNameOrAlias));
    }

    public List<CampusLocation> searchLocations(String query) {
        return campusGraph.searchLocations(query);
    }

    public List<CampusLocation> getLocationsByCategory(LocationCategory category) {
        return campusGraph.getAllLocations().stream()
                .filter(loc -> loc.getCategory() == category)
                .sorted(Comparator.comparing(CampusLocation::getName))
                .collect(Collectors.toList());
    }

    public List<GraphEdge> getAllEdges() {
        return campusGraph.getAllEdges();
    }

    public String resolveCanonicalName(String query) {
        return campusGraph.resolveCanonicalName(query);
    }

    public void addLocation(CampusLocation location) {
        campusGraph.addLocation(location);
    }

    public void addEdge(GraphEdge edge, boolean bidirectional) {
        campusGraph.addEdge(edge, bidirectional);
    }

    public void reloadGraphData() {
        campusGraph.loadDataFromClasspath();
    }

    public Map<String, Object> getGraphStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLocations", campusGraph.getNodeCount());
        stats.put("totalEdges", campusGraph.getEdgeCount());
        stats.put("categories", Arrays.stream(LocationCategory.values())
                .map(LocationCategory::getDisplayName)
                .collect(Collectors.toList()));
        stats.put("campusInfo", campusGraph.getCampusInfo());
        return stats;
    }
}
