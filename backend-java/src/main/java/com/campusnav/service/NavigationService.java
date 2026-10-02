package com.campusnav.service;

import com.campusnav.algorithm.PathFinder;
import com.campusnav.graph.CampusGraph;
import com.campusnav.model.NavigationRequest;
import com.campusnav.model.NavigationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service orchestrating graph navigation requests using OOP Strategy Pattern.
 */
@Service
public class NavigationService {
    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);

    private final CampusGraph campusGraph;
    private final PathFinder bfsPathFinder;
    private final PathFinder dfsPathFinder;

    public NavigationService(CampusGraph campusGraph,
                             @Qualifier("bfsPathFinder") PathFinder bfsPathFinder,
                             @Qualifier("dfsPathFinder") PathFinder dfsPathFinder) {
        this.campusGraph = campusGraph;
        this.bfsPathFinder = bfsPathFinder;
        this.dfsPathFinder = dfsPathFinder;
    }

    public NavigationResponse calculateRoute(NavigationRequest request) {
        if (request == null) {
            return NavigationResponse.notFound("BFS", null, null, "Invalid navigation request.");
        }

        String source = request.getSource();
        String destination = request.getDestination();
        String algorithm = request.getAlgorithm() != null ? request.getAlgorithm().toUpperCase() : "BFS";

        logger.info("Navigation Request: algorithm={}, source='{}', destination='{}'", algorithm, source, destination);

        PathFinder pathFinder = "DFS".equalsIgnoreCase(algorithm) ? dfsPathFinder : bfsPathFinder;
        return pathFinder.findPath(campusGraph, source, destination);
    }

    public NavigationResponse calculateBFS(String source, String destination) {
        return bfsPathFinder.findPath(campusGraph, source, destination);
    }

    public NavigationResponse calculateDFS(String source, String destination) {
        return dfsPathFinder.findPath(campusGraph, source, destination);
    }

    public Map<String, Object> compareAlgorithms(String source, String destination) {
        NavigationResponse bfs = calculateBFS(source, destination);
        NavigationResponse dfs = calculateDFS(source, destination);

        return Map.of(
                "bfs", bfs,
                "dfs", dfs,
                "analysis", Map.of(
                        "shortestPathAlgorithm", "BFS",
                        "bfsSteps", bfs.getSteps(),
                        "dfsSteps", dfs.getSteps(),
                        "bfsDistanceMeters", bfs.getDistanceMeters(),
                        "dfsDistanceMeters", dfs.getDistanceMeters(),
                        "comparisonConclusion", bfs.getSteps() <= dfs.getSteps()
                                ? "BFS found the optimal shortest path in " + bfs.getSteps() + " steps."
                                : "BFS produced fewer hops than DFS traversal."
                )
        );
    }
}
