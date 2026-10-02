package com.campusnav;

import com.campusnav.algorithm.BFSPathFinder;
import com.campusnav.algorithm.DFSPathFinder;
import com.campusnav.graph.CampusGraph;
import com.campusnav.model.ChatRequest;
import com.campusnav.model.ChatResponse;
import com.campusnav.model.NavigationRequest;
import com.campusnav.model.NavigationResponse;
import com.campusnav.service.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CampusNavApplicationTests {

    @Autowired
    private CampusGraph campusGraph;

    @Autowired
    private BFSPathFinder bfsPathFinder;

    @Autowired
    private DFSPathFinder dfsPathFinder;

    @Autowired
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        assertNotNull(campusGraph);
    }

    @Test
    void testCampusGraphLoadedProperly() {
        assertTrue(campusGraph.getNodeCount() >= 20, "Campus graph should have at least 20 locations");
        assertTrue(campusGraph.getEdgeCount() >= 25, "Campus graph should have at least 25 edges");
        assertNotNull(campusGraph.getLocation("Main Gate"));
        assertNotNull(campusGraph.getLocation("AI Lab"));
        assertNotNull(campusGraph.getLocation("Central Library"));
    }

    @Test
    void testAliasResolution() {
        assertEquals("AI Lab", campusGraph.resolveCanonicalName("ai lab"));
        assertEquals("Central Library", campusGraph.resolveCanonicalName("library"));
        assertEquals("CSE Department", campusGraph.resolveCanonicalName("cse dept"));
        assertEquals("Central Canteen & Cafeteria", campusGraph.resolveCanonicalName("canteen"));
    }

    @Test
    void testBFSShortestPathMainGateToAILab() {
        NavigationResponse response = bfsPathFinder.findPath(campusGraph, "Main Gate", "AI Lab");
        assertTrue(response.isFound(), "Route must be found");
        assertEquals("BFS", response.getAlgorithm());
        assertEquals("Main Gate", response.getSource());
        assertEquals("AI Lab", response.getDestination());
        assertFalse(response.getPath().isEmpty());
        assertEquals("Main Gate", response.getPath().get(0));
        assertEquals("AI Lab", response.getPath().get(response.getPath().size() - 1));
        assertTrue(response.getSteps() > 0);
        assertTrue(response.getDistanceMeters() > 0);
        assertTrue(response.getEstimatedMinutes() > 0);
    }

    @Test
    void testDFSPathFinding() {
        NavigationResponse response = dfsPathFinder.findPath(campusGraph, "Main Gate", "AI Lab");
        assertTrue(response.isFound(), "DFS route must be found");
        assertEquals("DFS", response.getAlgorithm());
        assertEquals("Main Gate", response.getSource());
        assertEquals("AI Lab", response.getDestination());
    }

    @Test
    void testSameSourceAndDestination() {
        NavigationResponse response = bfsPathFinder.findPath(campusGraph, "Central Library", "Central Library");
        assertTrue(response.isFound());
        assertEquals(0, response.getSteps());
        assertEquals(0, response.getDistanceMeters());
    }

    @Test
    void testChatbotDirectionsQuery() {
        ChatRequest req = new ChatRequest("How do I reach the AI lab from Main Gate?");
        ChatResponse resp = chatbotService.processQuery(req);

        assertEquals("DIRECTIONS", resp.getIntent());
        assertNotNull(resp.getNavigationResult());
        assertTrue(resp.getNavigationResult().isFound());
        assertEquals("Main Gate", resp.getSource());
        assertEquals("AI Lab", resp.getDestination());
    }

    @Test
    void testChatbotTimingQuery() {
        ChatRequest req = new ChatRequest("What are the library timings?");
        ChatResponse resp = chatbotService.processQuery(req);

        assertEquals("TIMING", resp.getIntent());
        assertNotNull(resp.getMatchedLocation());
        assertTrue(resp.getResponse().contains("Library"));
    }

    @Test
    void testChatbotGeneralQuery() {
        ChatRequest req = new ChatRequest("Tell me about the CSE department.");
        ChatResponse resp = chatbotService.processQuery(req);

        assertEquals("GENERAL", resp.getIntent());
        assertNotNull(resp.getMatchedLocation());
        assertTrue(resp.getResponse().toLowerCase().contains("computer science") || resp.getResponse().contains("CSE"));
    }

    @Test
    void testChatbotLocationSearchQuery() {
        ChatRequest req = new ChatRequest("Where is the canteen?");
        ChatResponse resp = chatbotService.processQuery(req);

        assertEquals("LOCATION_SEARCH", resp.getIntent());
        assertNotNull(resp.getMatchedLocation());
        assertTrue(resp.getResponse().toLowerCase().contains("canteen"));
    }
}
