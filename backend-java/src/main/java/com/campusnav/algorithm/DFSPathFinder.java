package com.campusnav.algorithm;

import com.campusnav.graph.CampusGraph;
import com.campusnav.model.CampusLocation;
import com.campusnav.model.GraphEdge;
import com.campusnav.model.NavigationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Depth-First Search (DFS) Pathfinding Implementation.
 * 
 * Algorithm Characteristics:
 * - Traverses deep along each branch before backtracking.
 * - Does NOT guarantee the shortest path.
 * - Demonstrates graph exploration / connectivity in ADSA.
 * - Time Complexity: O(V + E)
 * - Space Complexity: O(V)
 */
@Component("dfsPathFinder")
public class DFSPathFinder implements PathFinder {
    private static final Logger logger = LoggerFactory.getLogger(DFSPathFinder.class);

    @Override
    public NavigationResponse findPath(CampusGraph graph, String source, String destination) {
        logger.info("Executing DFS Pathfinding from '{}' to '{}'", source, destination);

        // 1. Validate inputs
        CampusLocation srcLoc = graph.getLocation(source);
        CampusLocation dstLoc = graph.getLocation(destination);

        if (srcLoc == null) {
            return NavigationResponse.notFound(getAlgorithmName(), source, destination,
                    "Starting location '" + source + "' was not found on the campus map.");
        }
        if (dstLoc == null) {
            return NavigationResponse.notFound(getAlgorithmName(), source, destination,
                    "Destination location '" + destination + "' was not found on the campus map.");
        }

        String canonicalSource = srcLoc.getName();
        String canonicalDest = dstLoc.getName();

        if (canonicalSource.equalsIgnoreCase(canonicalDest)) {
            NavigationResponse resp = new NavigationResponse();
            resp.setFound(true);
            resp.setAlgorithm(getAlgorithmName());
            resp.setSource(canonicalSource);
            resp.setDestination(canonicalDest);
            resp.setPath(Collections.singletonList(canonicalSource));
            resp.setNodeDetails(Collections.singletonList(srcLoc));
            resp.setSteps(0);
            resp.setDistanceMeters(0);
            resp.setEstimatedMinutes(0);
            resp.setTurnByTurnDirections(Collections.singletonList("You are already at " + canonicalDest + "."));
            resp.setMessage("Starting point is the same as the destination.");
            resp.setTimeComplexity(getTimeComplexity());
            resp.setSpaceComplexity(getSpaceComplexity());
            return resp;
        }

        // 2. DFS Traversal using recursive helper with visited tracking
        Set<String> visited = new HashSet<>();
        List<String> pathAcc = new ArrayList<>();
        Map<String, GraphEdge> edgeMap = new HashMap<>();

        boolean found = dfsHelper(graph, canonicalSource, canonicalDest, visited, pathAcc, edgeMap);

        if (!found) {
            return NavigationResponse.notFound(getAlgorithmName(), canonicalSource, canonicalDest,
                    "No valid DFS path exists between " + canonicalSource + " and " + canonicalDest + ".");
        }

        // 3. Compute metrics for DFS path
        int totalDistance = 0;
        int totalMinutes = 0;
        List<CampusLocation> nodeDetails = new ArrayList<>();
        List<String> turnDirections = new ArrayList<>();

        for (int i = 0; i < pathAcc.size(); i++) {
            String locName = pathAcc.get(i);
            CampusLocation loc = graph.getLocation(locName);
            if (loc != null) {
                nodeDetails.add(loc);
            }

            if (i > 0) {
                String prev = pathAcc.get(i - 1);
                // Look up edge between prev and locName
                List<GraphEdge> neighbors = graph.getNeighbors(prev);
                GraphEdge connectingEdge = neighbors.stream()
                        .filter(e -> e.getDestination().equalsIgnoreCase(locName))
                        .findFirst()
                        .orElse(null);

                int dist = connectingEdge != null ? connectingEdge.getDistanceMeters() : 80;
                int time = connectingEdge != null ? connectingEdge.getWalkMinutes() : 1;
                totalDistance += dist;
                totalMinutes += time;

                turnDirections.add(String.format("Step %d: Traversed from %s to %s (~%dm, %d min)",
                        i, prev, locName, dist, time));
            }
        }

        if (totalMinutes == 0 && totalDistance > 0) {
            totalMinutes = Math.max(1, (int) Math.ceil(totalDistance / 70.0));
        }

        // 4. Build Response
        NavigationResponse response = new NavigationResponse();
        response.setFound(true);
        response.setAlgorithm(getAlgorithmName());
        response.setSource(canonicalSource);
        response.setDestination(canonicalDest);
        response.setPath(pathAcc);
        response.setNodeDetails(nodeDetails);
        response.setSteps(pathAcc.size() - 1);
        response.setDistanceMeters(totalDistance);
        response.setEstimatedMinutes(totalMinutes);
        response.setTurnByTurnDirections(turnDirections);
        response.setMessage(String.format("DFS exploration route found with %d steps (~%d meters). Note: DFS traverses branch-first and may not be the shortest route.",
                response.getSteps(), totalDistance));
        response.setTimeComplexity(getTimeComplexity());
        response.setSpaceComplexity(getSpaceComplexity());

        return response;
    }

    private boolean dfsHelper(CampusGraph graph, String current, String target,
                              Set<String> visited, List<String> currentPath,
                              Map<String, GraphEdge> edgeMap) {
        visited.add(current);
        currentPath.add(current);

        if (current.equalsIgnoreCase(target)) {
            return true;
        }

        List<GraphEdge> neighbors = graph.getNeighbors(current);
        for (GraphEdge edge : neighbors) {
            String neighbor = edge.getDestination();
            if (!visited.contains(neighbor)) {
                edgeMap.put(neighbor, edge);
                if (dfsHelper(graph, neighbor, target, visited, currentPath, edgeMap)) {
                    return true;
                }
            }
        }

        // Backtrack
        currentPath.remove(currentPath.size() - 1);
        return false;
    }

    @Override
    public String getAlgorithmName() {
        return "DFS";
    }

    @Override
    public String getTimeComplexity() {
        return "O(V + E)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(V)";
    }

    @Override
    public String getDescription() {
        return "Depth-First Search explores paths branch-first using backtracking. Useful for connectivity checking, but doesn't guarantee the shortest route.";
    }
}
