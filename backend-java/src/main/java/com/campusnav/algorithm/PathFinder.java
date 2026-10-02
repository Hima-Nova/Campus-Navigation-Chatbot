package com.campusnav.algorithm;

import com.campusnav.graph.CampusGraph;
import com.campusnav.model.NavigationResponse;

/**
 * Common Strategy Interface for Graph Pathfinding Algorithms (ADSA / OOP).
 * Enables interchangeable algorithm execution (BFS, DFS) following Open-Closed & Strategy Patterns.
 */
public interface PathFinder {

    /**
     * Executes pathfinding on the provided campus graph from source to destination.
     *
     * @param graph CampusGraph instance containing adjacency lists and node metadata.
     * @param source Canonical source node name.
     * @param destination Canonical destination node name.
     * @return NavigationResponse containing path, distance, metrics, and turn-by-turn cues.
     */
    NavigationResponse findPath(CampusGraph graph, String source, String destination);

    /**
     * Returns the academic name of the algorithm.
     */
    String getAlgorithmName();

    /**
     * Returns the theoretical time complexity string (e.g. O(V + E)).
     */
    String getTimeComplexity();

    /**
     * Returns the theoretical space complexity string (e.g. O(V)).
     */
    String getSpaceComplexity();

    /**
     * Brief academic description of how the algorithm operates.
     */
    String getDescription();
}
