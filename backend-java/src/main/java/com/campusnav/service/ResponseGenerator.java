package com.campusnav.service;

import com.campusnav.model.CampusLocation;
import com.campusnav.model.NavigationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Intelligent Agent Response Synthesis Component.
 * Formats natural language responses for campus visitors, students, and faculty.
 */
@Component
public class ResponseGenerator {

    public String generateDirectionsResponse(NavigationResponse nav) {
        if (!nav.isFound()) {
            return nav.getMessage();
        }

        if (nav.getSteps() == 0) {
            return String.format("You are already at %s! No walking required.", nav.getDestination());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Here is the %s route from **%s** to **%s**:\n\n",
                "BFS".equalsIgnoreCase(nav.getAlgorithm()) ? "shortest" : "exploratory",
                nav.getSource(), nav.getDestination()));

        sb.append("📍 **Route Path:** ").append(String.join(" ➔ ", nav.getPath())).append("\n\n");
        sb.append(String.format("📊 **Trip Details:** %d steps | ~%d meters | ~%d min walk (via %s)",
                nav.getSteps(), nav.getDistanceMeters(), nav.getEstimatedMinutes(), nav.getAlgorithm()));

        return sb.toString();
    }

    public String generateTimingResponse(CampusLocation loc) {
        if (loc == null) {
            return "I couldn't find timing details for that specific location. Most campus academic blocks are open 8:00 AM - 6:00 PM.";
        }
        return String.format("🕒 **%s Timings:** %s\n📍 Located in: **%s (%s)**\nℹ️ %s",
                loc.getName(), loc.getTimings(), loc.getBlock(), loc.getFloor(), loc.getDescription());
    }

    public String generateGeneralInfoResponse(CampusLocation loc) {
        if (loc == null) {
            return "I could not find specific details for that department. Please check the Locations tab for the full directory.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🏢 **%s**\n\n", loc.getName()));
        sb.append(String.format("📍 **Location:** %s, %s\n", loc.getBlock(), loc.getFloor()));
        sb.append(String.format("📝 **About:** %s\n", loc.getDescription()));
        sb.append(String.format("🕒 **Operating Hours:** %s\n", loc.getTimings()));
        if (loc.getFacilities() != null && !loc.getFacilities().isEmpty()) {
            sb.append(String.format("✨ **Facilities:** %s", String.join(", ", loc.getFacilities())));
        }
        return sb.toString();
    }

    public String generateLocationSearchResponse(CampusLocation loc) {
        if (loc == null) {
            return "I couldn't find that location in the campus database. Try searching for 'Block A', 'Central Library', 'AI Lab', or 'Canteen'.";
        }
        return String.format("📍 **%s** is located in **%s**, %s.\n\nDescription: %s\nTimings: %s\n\nWould you like me to find the shortest route there?",
                loc.getName(), loc.getBlock(), loc.getFloor(), loc.getDescription(), loc.getTimings());
    }

    public String generateSuggestionsPrompt(List<CampusLocation> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return "I'm not sure which campus location you meant. Try asking:\n• 'Where is the AI Lab?'\n• 'How to reach Canteen from Main Gate?'\n• 'Library timings'\n• 'Tell me about CSE department'";
        }
        StringBuilder sb = new StringBuilder("I found multiple matching campus locations. Did you mean:\n");
        for (int i = 0; i < Math.min(4, suggestions.size()); i++) {
            CampusLocation loc = suggestions.get(i);
            sb.append(String.format("• **%s** (%s - %s)\n", loc.getName(), loc.getBlock(), loc.getFloor()));
        }
        sb.append("\nYou can tap any location above to navigate!");
        return sb.toString();
    }
}
