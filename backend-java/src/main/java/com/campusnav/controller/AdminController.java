package com.campusnav.controller;

import com.campusnav.model.CampusLocation;
import com.campusnav.model.GraphEdge;
import com.campusnav.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final LocationService locationService;

    public AdminController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getGraph() {
        return ResponseEntity.ok(Map.of(
                "locations", locationService.getAllLocations(),
                "edges", locationService.getAllEdges(),
                "stats", locationService.getGraphStats()
        ));
    }

    @PostMapping("/locations")
    public ResponseEntity<?> addLocation(@RequestBody CampusLocation location) {
        if (location.getName() == null || location.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location name is required."));
        }
        if (location.getId() == null) {
            location.setId("loc_" + location.getName().toLowerCase().replaceAll("[^a-z0-9]", "_"));
        }
        locationService.addLocation(location);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Location '" + location.getName() + "' registered successfully.",
                "location", location
        ));
    }

    @PostMapping("/edges")
    public ResponseEntity<?> addEdge(@RequestBody GraphEdge edge,
                                     @RequestParam(defaultValue = "true") boolean bidirectional) {
        if (edge.getSource() == null || edge.getDestination() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Source and Destination are required."));
        }
        locationService.addEdge(edge, bidirectional);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Edge between '" + edge.getSource() + "' and '" + edge.getDestination() + "' added.",
                "edge", edge
        ));
    }

    @PostMapping("/reload")
    public ResponseEntity<?> reloadGraph() {
        locationService.reloadGraphData();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Campus graph dataset reloaded from classpath."
        ));
    }
}
