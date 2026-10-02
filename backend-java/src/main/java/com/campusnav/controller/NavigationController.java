package com.campusnav.controller;

import com.campusnav.model.NavigationRequest;
import com.campusnav.model.NavigationResponse;
import com.campusnav.service.NavigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/navigation")
public class NavigationController {

    private final NavigationService navigationService;

    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @PostMapping("/bfs")
    public ResponseEntity<NavigationResponse> findShortestPathBFS(@RequestBody NavigationRequest request) {
        request.setAlgorithm("BFS");
        NavigationResponse response = navigationService.calculateRoute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/dfs")
    public ResponseEntity<NavigationResponse> findExplorationPathDFS(@RequestBody NavigationRequest request) {
        request.setAlgorithm("DFS");
        NavigationResponse response = navigationService.calculateRoute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/find")
    public ResponseEntity<NavigationResponse> findRoute(@RequestBody NavigationRequest request) {
        NavigationResponse response = navigationService.calculateRoute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareAlgorithms(@RequestBody NavigationRequest request) {
        Map<String, Object> comparison = navigationService.compareAlgorithms(request.getSource(), request.getDestination());
        return ResponseEntity.ok(comparison);
    }
}
