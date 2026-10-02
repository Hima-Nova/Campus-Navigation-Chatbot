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
 * Breadth-First Search (BFS) Pathfinding Implementation.
 * 
 * Algorithm Characteristics:
 * - Guarantees shortest path (minimum edge hops) in an unweighted graph.
 * - Explores level-by-level using a FIFO Queue.
 * - Time Complexity: O(V + E)
 * - Space Complexity: O(V)
 */
@Component("bfsPathFinder")
public class BFSPathFinder implements PathFinder {
    private static final Logger logger = LoggerFactory.getLogger(BFSPathFinder.class);

    @Override
    public NavigationResponse findPath(CampusGraph graph, String source, String destination) {
        logger.info("Executing BFS Pathfinding from '{}' to '{}'", source, destination);

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

        // Edge case: Source and Destination are identical
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

        // 2. BFS Traversal Data Structures
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parentMap = new HashMap<>();
        Map<String, GraphEdge> traversedEdgeMap = new HashMap<>();

        // 3. Initialize BFS
        queue.add(canonicalSource);
        visited.add(canonicalSource);
        boolean pathFound = false;

        // 4. BFS Exploration Loop
        while (!queue.isEmpty()) {
            String current = queue.poll();

            // Destination reached!
            if (current.equalsIgnoreCase(canonicalDest)) {
                pathFound = true;
                break;
            }

            // Explore all walkable adjacent neighbors
            List<GraphEdge> neighbors = graph.getNeighbors(current);
            for (GraphEdge edge : neighbors) {
                String neighborName = edge.getDestination();
                if (!visited.contains(neighborName)) {
                    visited.add(neighborName);
                    parentMap.put(neighborName, current);
                    traversedEdgeMap.put(neighborName, edge);
                    queue.add(neighborName);
                }
            }
        }

        // 5. Check if route was reachable
        if (!pathFound) {
            return NavigationResponse.notFound(getAlgorithmName(), canonicalSource, canonicalDest,
                    "No walkable path exists between " + canonicalSource + " and " + canonicalDest + ".");
        }

        // 6. Reconstruct Path by backtracking from Destination to Source
        List<String> reconstructedPath = new LinkedList<>();
        String curr = canonicalDest;
        while (curr != null) {
            reconstructedPath.add(0, curr);
            curr = parentMap.get(curr);
        }

        // 7. Calculate total metrics and turn-by-turn directions
        int totalDistance = 0;
        int totalMinutes = 0;
        List<CampusLocation> nodeDetails = new ArrayList<>();
        List<String> turnDirections = new ArrayList<>();

        for (int i = 0; i < reconstructedPath.size(); i++) {
            String locName = reconstructedPath.get(i);
            CampusLocation loc = graph.getLocation(locName);
            if (loc != null) {
                nodeDetails.add(loc);
            }

            if (i > 0) {
                String prev = reconstructedPath.get(i - 1);
                GraphEdge edge = traversedEdgeMap.get(locName);
                int dist = edge != null ? edge.getDistanceMeters() : 80;
                int time = edge != null ? edge.getWalkMinutes() : 1;
                totalDistance += dist;
                totalMinutes += time;

                String stepInstruction = String.format("Step %d: Walk from %s to %s (~%dm, %d min)",
                        i, prev, locName, dist, time);
                turnDirections.add(stepInstruction);
            }
        }

        // Ensure minimum 1 minute walk time if distance > 0
        if (totalMinutes == 0 && totalDistance > 0) {
            totalMinutes = Math.max(1, (int) Math.ceil(totalDistance / 70.0));
        }

        // 8. Build and Return Response
        NavigationResponse response = new NavigationResponse();
        response.setFound(true);
        response.setAlgorithm(getAlgorithmName());
        response.setSource(canonicalSource);
        response.setDestination(canonicalDest);
        response.setPath(reconstructedPath);
        response.setNodeDetails(nodeDetails);
        response.setSteps(reconstructedPath.size() - 1);
        response.setDistanceMeters(totalDistance);
        response.setEstimatedMinutes(totalMinutes);
        response.setTurnByTurnDirections(turnDirections);
        response.setMessage(String.format("Shortest route found with %d steps (~%d meters, %d min walk).",
                response.getSteps(), totalDistance, totalMinutes));
        response.setTimeComplexity(getTimeComplexity());
        response.setSpaceComplexity(getSpaceComplexity());

        return response;
    }

    @Override
    public String getAlgorithmName() {
        return "BFS";
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
        return "Breadth-First Search traverses graph level-by-level using a FIFO Queue, guaranteeing the shortest path in unweighted graphs.";
    }
}
