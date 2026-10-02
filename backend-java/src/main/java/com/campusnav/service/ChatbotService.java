package com.campusnav.service;

import com.campusnav.client.PythonClassifierClient;
import com.campusnav.graph.CampusGraph;
import com.campusnav.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Intelligent Campus Agent Orchestrator.
 * Combines NLP Intent Classification (Python), Entity Extraction,
 * Graph Pathfinding (ADSA BFS/DFS), and Response Synthesis.
 */
@Service
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final PythonClassifierClient pythonClient;
    private final NavigationService navigationService;
    private final LocationService locationService;
    private final ResponseGenerator responseGenerator;
    private final CampusGraph campusGraph;

    public ChatbotService(PythonClassifierClient pythonClient,
                          NavigationService navigationService,
                          LocationService locationService,
                          ResponseGenerator responseGenerator,
                          CampusGraph campusGraph) {
        this.pythonClient = pythonClient;
        this.navigationService = navigationService;
        this.locationService = locationService;
        this.responseGenerator = responseGenerator;
        this.campusGraph = campusGraph;
    }

    public ChatResponse processQuery(ChatRequest request) {
        String query = request.getQuery();
        if (query == null || query.trim().isEmpty()) {
            ChatResponse emptyResp = new ChatResponse();
            emptyResp.setIntent("UNKNOWN");
            emptyResp.setResponse("Hello! Please ask a question about campus navigation, locations, or timings.");
            emptyResp.setQuickActions(getSampleQuickActions());
            return emptyResp;
        }

        logger.info("Processing user query: '{}'", query);

        // Step 1: Python Intent Classification
        IntentResult intentResult = pythonClient.classify(query);
        String intent = intentResult.getIntent();
        double confidence = intentResult.getConfidence();

        ChatResponse response = new ChatResponse();
        response.setIntent(intent);
        response.setQuery(query);
        response.setConfidence(confidence);
        response.setPythonClassifierStatus(intentResult.getStatus());
        response.setAiAgentExplanation(intentResult.getExplanation());

        // Step 2: Route according to classified intent
        switch (intent) {
            case "DIRECTIONS":
                handleDirectionsIntent(request, response);
                break;
            case "TIMING":
                handleTimingIntent(query, response);
                break;
            case "GENERAL":
                handleGeneralIntent(query, response);
                break;
            case "LOCATION_SEARCH":
                handleLocationSearchIntent(query, response);
                break;
            case "UNKNOWN":
            default:
                handleUnknownIntent(query, response);
                break;
        }

        return response;
    }

    private void handleDirectionsIntent(ChatRequest request, ChatResponse response) {
        String query = request.getQuery();
        String preferredAlgo = request.getAlgorithm() != null ? request.getAlgorithm() : "BFS";

        // Extract Source and Destination entities
        LocationPair pair = extractSourceAndDestination(query, request.getCurrentLocation());

        if (pair.destination != null && pair.source != null) {
            response.setSource(pair.source);
            response.setDestination(pair.destination);

            NavigationRequest navReq = new NavigationRequest(pair.source, pair.destination, preferredAlgo);
            NavigationResponse navResp = navigationService.calculateRoute(navReq);

            response.setNavigationResult(navResp);
            response.setResponse(responseGenerator.generateDirectionsResponse(navResp));
            response.setQuickActions(List.of(
                    "View on Campus Map",
                    "Library timings",
                    "Where is the canteen?",
                    "Explore via DFS"
            ));
        } else if (pair.destination != null) {
            // Destination found, but source is missing
            String defaultSource = request.getCurrentLocation() != null ? request.getCurrentLocation() : "Main Gate";
            response.setDestination(pair.destination);
            response.setSource(defaultSource);

            NavigationRequest navReq = new NavigationRequest(defaultSource, pair.destination, preferredAlgo);
            NavigationResponse navResp = navigationService.calculateRoute(navReq);

            response.setNavigationResult(navResp);
            response.setResponse(String.format("I've calculated the shortest route to **%s** starting from **%s**:\n\n%s",
                    pair.destination, defaultSource, responseGenerator.generateDirectionsResponse(navResp)));
            response.setQuickActions(List.of(
                    "Change Starting Point",
                    "View on Campus Map",
                    "Show Department Details"
            ));
        } else {
            // No destination identified
            List<CampusLocation> suggestions = locationService.searchLocations(query);
            if (!suggestions.isEmpty()) {
                response.setSuggestedLocations(suggestions.stream().limit(5).collect(Collectors.toList()));
                response.setResponse("Which campus location would you like directions to? Here are some matching spots:");
                response.setQuickActions(suggestions.stream().limit(4).map(l -> "Route to " + l.getName()).collect(Collectors.toList()));
            } else {
                response.setResponse("Please specify where you would like to go (for example: *'Route from Main Gate to AI Lab'* or *'How to reach Central Library'*).");
                response.setQuickActions(getSampleQuickActions());
            }
        }
    }

    private void handleTimingIntent(String query, ChatResponse response) {
        String locName = extractSingleLocation(query);
        if (locName != null) {
            CampusLocation loc = campusGraph.getLocation(locName);
            response.setMatchedLocation(loc);
            response.setDestination(loc != null ? loc.getName() : locName);
            response.setResponse(responseGenerator.generateTimingResponse(loc));
            response.setQuickActions(List.of(
                    "Route to " + locName,
                    "Where is " + locName + "?",
                    "Tell me about " + locName
            ));
        } else {
            // General timing overview
            response.setResponse("🕒 **Standard Campus Timings:**\n• Academic Blocks: 8:00 AM - 6:00 PM\n• Central Library: 8:00 AM - 9:00 PM\n• Canteen: 7:30 AM - 9:30 PM\n• Sports Ground: 6:00 AM - 8:30 PM\n\nAsk for specific location timings like *'What are the Library timings?'* or *'Canteen timings'*.");
            response.setQuickActions(List.of("Library timings", "Canteen timings", "Sports complex timings", "Exam cell timings"));
        }
    }

    private void handleGeneralIntent(String query, ChatResponse response) {
        String locName = extractSingleLocation(query);
        if (locName != null) {
            CampusLocation loc = campusGraph.getLocation(locName);
            response.setMatchedLocation(loc);
            response.setDestination(loc != null ? loc.getName() : locName);
            response.setResponse(responseGenerator.generateGeneralInfoResponse(loc));
            response.setQuickActions(List.of(
                    "Route to " + locName,
                    locName + " timings",
                    "View on Map"
            ));
        } else {
            response.setResponse("Apex Tech Campus spans 65 acres featuring state-of-the-art academic departments (CSE, AI & DS, AIML, Mechanical, ECE), specialized computing labs, a 3-floor Central Library, 1,500-seat auditorium, sports arena, and student dining facilities.\n\nWhich department or building would you like to know more about?");
            response.setQuickActions(List.of("About CSE department", "About AI Lab", "About Central Library", "About Canteen"));
        }
    }

    private void handleLocationSearchIntent(String query, ChatResponse response) {
        String locName = extractSingleLocation(query);
        if (locName != null) {
            CampusLocation loc = campusGraph.getLocation(locName);
            response.setMatchedLocation(loc);
            response.setDestination(loc != null ? loc.getName() : locName);
            response.setResponse(responseGenerator.generateLocationSearchResponse(loc));
            response.setQuickActions(List.of(
                    "Route from Main Gate to " + locName,
                    locName + " timings",
                    "View on Campus Map"
            ));
        } else {
            List<CampusLocation> results = locationService.searchLocations(query);
            if (!results.isEmpty()) {
                response.setSuggestedLocations(results.stream().limit(5).collect(Collectors.toList()));
                response.setResponse(responseGenerator.generateSuggestionsPrompt(results));
                response.setQuickActions(results.stream().limit(4).map(l -> "Where is " + l.getName()).collect(Collectors.toList()));
            } else {
                response.setResponse("I couldn't find a matching location. Please check the spelling or explore the **Campus Map** tab to browse all 27 campus facilities.");
                response.setQuickActions(getSampleQuickActions());
            }
        }
    }

    private void handleUnknownIntent(String query, ChatResponse response) {
        // Attempt fallback location entity recognition
        String locName = extractSingleLocation(query);
        if (locName != null) {
            CampusLocation loc = campusGraph.getLocation(locName);
            response.setMatchedLocation(loc);
            response.setResponse(String.format("I found **%s** (%s, %s).\n\nWhat would you like to know about it?",
                    loc.getName(), loc.getBlock(), loc.getFloor()));
            response.setQuickActions(List.of(
                    "Route to " + loc.getName(),
                    loc.getName() + " timings",
                    "About " + loc.getName()
            ));
            return;
        }

        List<CampusLocation> results = locationService.searchLocations(query);
        if (!results.isEmpty()) {
            response.setSuggestedLocations(results.stream().limit(4).collect(Collectors.toList()));
            response.setResponse("I'm not quite sure of your question, but I found these campus locations matching your keywords:");
            response.setQuickActions(results.stream().limit(4).map(l -> "Where is " + l.getName()).collect(Collectors.toList()));
        } else {
            response.setResponse("I'm your Campus AI Assistant! I can help you with:\n• **Directions:** *'How do I go from Main Gate to AI Lab?'*\n• **Location:** *'Where is the Seminar Hall?'*\n• **Timings:** *'What are the library timings?'*\n• **Info:** *'Tell me about CSE department'*");
            response.setQuickActions(getSampleQuickActions());
        }
    }

    /**
     * Extracts source and destination location names from query using NLP regex heuristics & alias lookup.
     */
    public LocationPair extractSourceAndDestination(String query, String defaultSource) {
        LocationPair pair = new LocationPair();
        String normalized = CampusGraph.normalizeText(query);

        // Pattern 1: from [X] to [Y]
        Pattern p1 = Pattern.compile("\\bfrom\\s+(.+?)\\s+to\\s+(.+)");
        Matcher m1 = p1.matcher(normalized);
        if (m1.find()) {
            pair.source = campusGraph.resolveCanonicalName(m1.group(1));
            pair.destination = campusGraph.resolveCanonicalName(m1.group(2));
        }

        // Pattern 2: between [X] and [Y]
        if (pair.destination == null) {
            Pattern p2 = Pattern.compile("\\bbetween\\s+(.+?)\\s+and\\s+(.+)");
            Matcher m2 = p2.matcher(normalized);
            if (m2.find()) {
                pair.source = campusGraph.resolveCanonicalName(m2.group(1));
                pair.destination = campusGraph.resolveCanonicalName(m2.group(2));
            }
        }

        // Pattern 3: to [Y] from [X] (e.g., "reach AI Lab from Main Gate")
        if (pair.destination == null) {
            Pattern p3 = Pattern.compile("\\b(?:to|reach|navigate)\\s+(.+?)\\s+from\\s+(.+)");
            Matcher m3 = p3.matcher(normalized);
            if (m3.find()) {
                pair.destination = campusGraph.resolveCanonicalName(m3.group(1));
                pair.source = campusGraph.resolveCanonicalName(m3.group(2));
            }
        }

        // Pattern 4: Single destination extraction
        if (pair.destination == null) {
            pair.destination = extractSingleLocation(query);
            if (pair.source == null && defaultSource != null) {
                pair.source = campusGraph.resolveCanonicalName(defaultSource);
            }
        }

        return pair;
    }

    /**
     * Extracts the longest matching campus location from a query string.
     */
    public String extractSingleLocation(String query) {
        if (query == null) return null;
        String normalized = CampusGraph.normalizeText(query);

        // Sort registered aliases by length descending so longer phrases match first (e.g. "ai lab" before "ai")
        List<Map.Entry<String, String>> sortedAliases = campusGraph.getAliasMap().entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()))
                .collect(Collectors.toList());

        for (Map.Entry<String, String> entry : sortedAliases) {
            String alias = entry.getKey();
            if (alias.length() < 2) continue; // Ignore very short noise
            // Word boundary match
            String regex = "\\b" + Pattern.quote(alias) + "\\b";
            if (Pattern.compile(regex).matcher(normalized).find()) {
                return entry.getValue();
            }
        }

        // Substring fallback
        for (Map.Entry<String, String> entry : sortedAliases) {
            String alias = entry.getKey();
            if (alias.length() >= 3 && normalized.contains(alias)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public List<String> getSampleQuickActions() {
        return List.of(
                "How to reach AI Lab from Main Gate?",
                "What are the Library timings?",
                "Where is the Canteen?",
                "Tell me about CSE Department",
                "Route from Library to Auditorium"
        );
    }

    public static class LocationPair {
        public String source;
        public String destination;
    }
}
