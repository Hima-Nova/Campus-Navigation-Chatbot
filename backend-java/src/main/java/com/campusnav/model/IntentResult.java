package com.campusnav.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntentResult {
    private String intent = "UNKNOWN";
    private double confidence = 0.0;
    private String normalizedQuery = "";
    private String matchedPattern;
    private String explanation;
    private String status = "success";

    public IntentResult() {}

    public IntentResult(String intent, double confidence, String explanation) {
        this.intent = intent;
        this.confidence = confidence;
        this.explanation = explanation;
    }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getNormalizedQuery() { return normalizedQuery; }
    public void setNormalizedQuery(String normalizedQuery) { this.normalizedQuery = normalizedQuery; }

    public String getMatchedPattern() { return matchedPattern; }
    public void setMatchedPattern(String matchedPattern) { this.matchedPattern = matchedPattern; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
