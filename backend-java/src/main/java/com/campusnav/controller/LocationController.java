package com.campusnav.controller;

import com.campusnav.model.CampusLocation;
import com.campusnav.model.LocationCategory;
import com.campusnav.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<CampusLocation>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable String id) {
        return locationService.getLocation(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CampusLocation>> searchLocations(@RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(locationService.searchLocations(query));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CampusLocation>> getByCategory(@PathVariable String category) {
        try {
            LocationCategory cat = LocationCategory.valueOf(category.toUpperCase());
            return ResponseEntity.ok(locationService.getLocationsByCategory(cat));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, String>>> getCategories() {
        List<Map<String, String>> categories = Arrays.stream(LocationCategory.values())
                .map(cat -> Map.of(
                        "name", cat.name(),
                        "displayName", cat.getDisplayName(),
                        "icon", cat.getIconName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
