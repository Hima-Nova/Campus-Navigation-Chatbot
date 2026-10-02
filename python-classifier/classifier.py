"""
Campus Navigation Intent Classifier
Implements Rule-Based & Keyword-Based NLP Intent Classification
Demonstrating AI/NLP Fundamentals for College Project
"""

import re
import string

class KeywordIntentClassifier:
    """
    Keyword and Pattern matching Intent Classifier.
    Normalizes query strings and classifies intent into:
    - DIRECTIONS
    - TIMING
    - GENERAL
    - LOCATION_SEARCH
    - UNKNOWN
    """

    def __init__(self):
        # Ordered keyword patterns (prioritized)
        self.intent_keywords = {
            "TIMING": [
                r"\b(timings?|time|hours?|working hours?|open(?:ing)?|clos(?:ing|es?)|schedule|operating hours?|when does|what time)\b"
            ],
            "DIRECTIONS": [
                r"\b(how (?:do|can) i (?:reach|get to|go to|find)|take me to|navigate to|guide me to|route (?:from|to)|path (?:from|to|between)|direction(?:s)? (?:to|from)?|how to reach|how to get|shortest route|fastest way|walk to|way to|go from|lead me to)\b",
                r"\b(reach|navigate|route|path|directions?|way to)\b"
            ],
            "LOCATION_SEARCH": [
                r"\b(where is|where are|where can i find|locate|find|location of|which block|which floor|room number|search for)\b",
                r"\b(where)\b"
            ],
            "GENERAL": [
                r"\b(tell me about|what is|details? (?:of|about)|information (?:about|on)?|info about|describe|about the|facilities in|overview of|departments?)\b",
                r"\b(about|info|details|overview|facility|facilities)\b"
            ]
        }

    def normalize(self, text: str) -> str:
        """
        Normalize input text:
        - Lowercase
        - Replace punctuation with spaces
        - Normalize multiple whitespace characters
        """
        if not text:
            return ""
        text = text.lower().strip()
        # Remove punctuation except hyphen
        text = re.sub(r"[^\w\s-]", " ", text)
        # Collapse multiple spaces
        text = re.sub(r"\s+", " ", text)
        return text.strip()

    def classify(self, raw_query: str) -> dict:
        """
        Classifies the raw query into an intent.
        Returns a dict containing intent, confidence, normalized query, and matched pattern.
        """
        if not raw_query or not raw_query.strip():
            return {
                "intent": "UNKNOWN",
                "confidence": 0.0,
                "normalizedQuery": "",
                "matchedPattern": None,
                "explanation": "Empty query received"
            }

        normalized = self.normalize(raw_query)

        # Priority 1: Check TIMING first (e.g., "what is the library timing" -> TIMING, not GENERAL)
        for pattern in self.intent_keywords["TIMING"]:
            match = re.search(pattern, normalized)
            if match:
                return {
                    "intent": "TIMING",
                    "confidence": 0.95,
                    "normalizedQuery": normalized,
                    "matchedPattern": match.group(0),
                    "explanation": f"Matched timing keyword '{match.group(0)}'"
                }

        # Priority 2: Check DIRECTIONS (e.g., "how do I go from X to Y", "shortest route")
        for pattern in self.intent_keywords["DIRECTIONS"]:
            match = re.search(pattern, normalized)
            if match:
                return {
                    "intent": "DIRECTIONS",
                    "confidence": 0.95,
                    "normalizedQuery": normalized,
                    "matchedPattern": match.group(0),
                    "explanation": f"Matched navigation keyword '{match.group(0)}'"
                }

        # Priority 3: Check LOCATION_SEARCH (e.g., "where is AI Lab")
        for pattern in self.intent_keywords["LOCATION_SEARCH"]:
            match = re.search(pattern, normalized)
            if match:
                return {
                    "intent": "LOCATION_SEARCH",
                    "confidence": 0.90,
                    "normalizedQuery": normalized,
                    "matchedPattern": match.group(0),
                    "explanation": f"Matched location search keyword '{match.group(0)}'"
                }

        # Priority 4: Check GENERAL (e.g., "tell me about CSE department")
        for pattern in self.intent_keywords["GENERAL"]:
            match = re.search(pattern, normalized)
            if match:
                return {
                    "intent": "GENERAL",
                    "confidence": 0.85,
                    "normalizedQuery": normalized,
                    "matchedPattern": match.group(0),
                    "explanation": f"Matched general information keyword '{match.group(0)}'"
                }

        # Fallback: UNKNOWN
        return {
            "intent": "UNKNOWN",
            "confidence": 0.20,
            "normalizedQuery": normalized,
            "matchedPattern": None,
            "explanation": "No distinct keywords matched; defaulting to UNKNOWN"
        }

if __name__ == "__main__":
    classifier = KeywordIntentClassifier()
    test_queries = [
        "How do I reach the AI lab?",
        "What time does the library close?",
        "Tell me about the CSE department.",
        "Where is the canteen?",
        "Route from Main Gate to Auditorium",
        "Hello there",
        "Examination cell timings",
        "Find sports complex"
    ]
    for q in test_queries:
        res = classifier.classify(q)
        print(f"Query: '{q}' -> Intent: {res['intent']} (Reason: {res['explanation']})")
