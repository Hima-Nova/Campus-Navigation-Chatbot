package com.campusnav.graph;

import com.campusnav.model.CampusLocation;
import com.campusnav.model.GraphEdge;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Campus Graph Representation (ADSA).
 * Encapsulates the campus topology as an undirected adjacency-list graph.
 * Provides vertex and edge registries, alias lookup, and location metadata.
 */
@Component
public class CampusGraph {
    private static final Logger logger = LoggerFactory.getLogger(CampusGraph.class);

    // Adjacency List: Location Name (Canonical) -> List of Outgoing Walkable Edges
    private final Map<String, List<GraphEdge>> adjacencyList = new ConcurrentHashMap<>();

    // Location Registry: Canonical Name -> CampusLocation Metadata
    private final Map<String, CampusLocation> locationRegistry = new ConcurrentHashMap<>();

    // ID Registry: ID -> CampusLocation
    private final Map<String, CampusLocation> idRegistry = new ConcurrentHashMap<>();

    // Alias Lookup Map: Normalized Alias String -> Canonical Location Name
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>();

    // Campus General Info
    private final Map<String, Object> campusInfo = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        loadDataFromClasspath();
    }

    public synchronized void loadDataFromClasspath() {
        try (InputStream is = getClass().getResourceAsStream("/campus-data.json")) {
            if (is == null) {
                logger.error("campus-data.json not found in classpath!");
                return;
            }
            loadFromJson(is);
        } catch (Exception e) {
            logger.error("Failed to load campus graph data: {}", e.getMessage(), e);
        }
    }

    public synchronized void loadFromJson(InputStream is) throws Exception {
        JsonNode root = objectMapper.readTree(is);

        // Clear existing state
        adjacencyList.clear();
        locationRegistry.clear();
        idRegistry.clear();
        aliasMap.clear();
        campusInfo.clear();

        // 1. Campus Info
        if (root.has("campusInfo")) {
            JsonNode info = root.get("campusInfo");
            info.fields().forEachRemaining(entry -> campusInfo.put(entry.getKey(), entry.getValue().asText()));
        }

        // 2. Load Locations (Vertices)
        if (root.has("locations")) {
            for (JsonNode locNode : root.get("locations")) {
                CampusLocation loc = objectMapper.treeToValue(locNode, CampusLocation.class);
                addLocation(loc);
            }
        }

        // 3. Load Edges (Connections)
        if (root.has("edges")) {
            for (JsonNode edgeNode : root.get("edges")) {
                GraphEdge edge = objectMapper.treeToValue(edgeNode, GraphEdge.class);
                addEdge(edge, true); // Campus walkways are bidirectional
            }
        }

        logger.info("Campus Graph loaded: {} Vertices, {} Edges, {} Aliases registered.",
                locationRegistry.size(), getEdgeCount(), aliasMap.size());
    }

    public synchronized void addLocation(CampusLocation loc) {
        if (loc == null || loc.getName() == null) return;

        locationRegistry.put(loc.getName(), loc);
        if (loc.getId() != null) {
            idRegistry.put(loc.getId(), loc);
        }

        adjacencyList.putIfAbsent(loc.getName(), new ArrayList<>());

        // Register default aliases
        registerAlias(loc.getName(), loc.getName());
        registerAlias(loc.getName().toLowerCase(), loc.getName());

        if (loc.getAliases() != null) {
            for (String alias : loc.getAliases()) {
                registerAlias(alias, loc.getName());
            }
        }
    }

    public synchronized void addEdge(GraphEdge edge, boolean bidirectional) {
        if (edge == null || edge.getSource() == null || edge.getDestination() == null) return;

        // Ensure nodes are recognized
        adjacencyList.putIfAbsent(edge.getSource(), new ArrayList<>());
        adjacencyList.putIfAbsent(edge.getDestination(), new ArrayList<>());

        // Add forward edge
        adjacencyList.get(edge.getSource()).removeIf(e -> e.getDestination().equalsIgnoreCase(edge.getDestination()));
        adjacencyList.get(edge.getSource()).add(edge);

        if (bidirectional) {
            // Add reverse edge
            GraphEdge reverseEdge = new GraphEdge(
                    edge.getDestination(),
                    edge.getSource(),
                    edge.getDistanceMeters(),
                    edge.getWalkMinutes(),
                    edge.getPathType()
            );
            adjacencyList.get(edge.getDestination()).removeIf(e -> e.getDestination().equalsIgnoreCase(edge.getSource()));
            adjacencyList.get(edge.getDestination()).add(reverseEdge);
        }
    }

    public void registerAlias(String alias, String canonicalName) {
        if (alias == null || canonicalName == null) return;
        String normalized = normalizeText(alias);
        if (!normalized.isEmpty()) {
            aliasMap.put(normalized, canonicalName);
        }
    }

    /**
     * Resolves user query string or alias to canonical Location Name.
     */
    public String resolveCanonicalName(String query) {
        if (query == null) return null;
        String normalized = normalizeText(query);

        // Exact alias lookup
        if (aliasMap.containsKey(normalized)) {
            return aliasMap.get(normalized);
        }

        // Direct case-insensitive match on registered names
        for (String name : locationRegistry.keySet()) {
            if (name.equalsIgnoreCase(query.trim()) || normalizeText(name).equals(normalized)) {
                return name;
            }
        }

        // Substring / Contains search
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            if (normalized.contains(entry.getKey()) || entry.getKey().contains(normalized)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Finds location by canonical name, alias, or ID.
     */
    public CampusLocation getLocation(String nameOrIdOrAlias) {
        if (nameOrIdOrAlias == null) return null;
        if (locationRegistry.containsKey(nameOrIdOrAlias)) {
            return locationRegistry.get(nameOrIdOrAlias);
        }
        if (idRegistry.containsKey(nameOrIdOrAlias)) {
            return idRegistry.get(nameOrIdOrAlias);
        }
        String canonical = resolveCanonicalName(nameOrIdOrAlias);
        if (canonical != null && locationRegistry.containsKey(canonical)) {
            return locationRegistry.get(canonical);
        }
        return null;
    }

    public List<GraphEdge> getNeighbors(String canonicalName) {
        return adjacencyList.getOrDefault(canonicalName, Collections.emptyList());
    }

    public List<CampusLocation> searchLocations(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(locationRegistry.values());
        }
        String term = normalizeText(keyword);
        List<CampusLocation> results = new ArrayList<>();

        for (CampusLocation loc : locationRegistry.values()) {
            boolean match = normalizeText(loc.getName()).contains(term) ||
                    (loc.getDescription() != null && normalizeText(loc.getDescription()).contains(term)) ||
                    (loc.getBlock() != null && normalizeText(loc.getBlock()).contains(term)) ||
                    (loc.getCategory() != null && normalizeText(loc.getCategory().name()).contains(term)) ||
                    loc.getAliases().stream().anyMatch(a -> normalizeText(a).contains(term));

            if (match) {
                results.add(loc);
            }
        }
        return results;
    }

    public List<CampusLocation> getAllLocations() {
        return new ArrayList<>(locationRegistry.values());
    }

    public List<GraphEdge> getAllEdges() {
        Set<String> seen = new HashSet<>();
        List<GraphEdge> edges = new ArrayList<>();
        for (List<GraphEdge> edgeList : adjacencyList.values()) {
            for (GraphEdge edge : edgeList) {
                String key1 = edge.getSource() + "->" + edge.getDestination();
                String key2 = edge.getDestination() + "->" + edge.getSource();
                if (!seen.contains(key1) && !seen.contains(key2)) {
                    seen.add(key1);
                    edges.add(edge);
                }
            }
        }
        return edges;
    }

    public int getNodeCount() {
        return locationRegistry.size();
    }

    public int getEdgeCount() {
        return getAllEdges().size();
    }

    public Map<String, Object> getCampusInfo() {
        return campusInfo;
    }

    public Map<String, String> getAliasMap() {
        return Collections.unmodifiableMap(aliasMap);
    }

    public static String normalizeText(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
