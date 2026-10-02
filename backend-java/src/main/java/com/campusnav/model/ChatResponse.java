package com.campusnav.model;

import java.util.ArrayList;
import java.util.List;

public class ChatResponse {
    private String intent;
    private String query;
    private String response;
    private String source;
    private String destination;
    private NavigationResponse navigationResult;
    private CampusLocation matchedLocation;
    private List<CampusLocation> suggestedLocations = new ArrayList<>();
    private List<String> quickActions = new ArrayList<>();
    private double confidence;
    private String pythonClassifierStatus = "CONNECTED"; // "CONNECTED" or "FALLBACK"
    private String aiAgentExplanation;

    public ChatResponse() {}

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public NavigationResponse getNavigationResult() { return navigationResult; }
    public void setNavigationResult(NavigationResponse navigationResult) { this.navigationResult = navigationResult; }

    public CampusLocation getMatchedLocation() { return matchedLocation; }
    public void setMatchedLocation(CampusLocation matchedLocation) { this.matchedLocation = matchedLocation; }

    public List<CampusLocation> getSuggestedLocations() { return suggestedLocations; }
    public void setSuggestedLocations(List<CampusLocation> suggestedLocations) { 
        this.suggestedLocations = suggestedLocations != null ? suggestedLocations : new ArrayList<>(); 
    }

    public List<String> getQuickActions() { return quickActions; }
    public void setQuickActions(List<String> quickActions) { 
        this.quickActions = quickActions != null ? quickActions : new ArrayList<>(); 
    }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getPythonClassifierStatus() { return pythonClassifierStatus; }
    public void setPythonClassifierStatus(String pythonClassifierStatus) { this.pythonClassifierStatus = pythonClassifierStatus; }

    public String getAiAgentExplanation() { return aiAgentExplanation; }
    public void setAiAgentExplanation(String aiAgentExplanation) { this.aiAgentExplanation = aiAgentExplanation; }
}
