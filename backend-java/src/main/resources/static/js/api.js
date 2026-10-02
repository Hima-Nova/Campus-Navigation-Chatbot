/**
 * CampusNav API Client
 * Manages all REST API interactions with the Java Spring Boot Backend.
 * Uses relative URLs by default (/api/...) for seamless same-origin deployment in production and local dev.
 */

const API_BASE = (typeof window !== 'undefined' && window.CAMPUS_API_BASE) 
    ? window.CAMPUS_API_BASE 
    : '';

const Api = {
    async checkHealth() {
        try {
            const res = await fetch(`${API_BASE}/api/health`);
            if (!res.ok) throw new Error('Health check failed');
            return await res.json();
        } catch (e) {
            console.warn('API Health Check Error:', e);
            return { status: 'DOWN', error: e.message };
        }
    },

    async sendChatMessage(query, currentLocation = null, algorithm = 'BFS') {
        try {
            const res = await fetch(`${API_BASE}/api/chat`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query, currentLocation, algorithm })
            });
            if (!res.ok) throw new Error(`HTTP error ${res.status}`);
            return await res.json();
        } catch (e) {
            console.error('Chat API Error:', e);
            return {
                intent: 'UNKNOWN',
                response: 'Unable to reach the campus server. Please ensure the Java backend is running on port 8080.',
                quickActions: ['Retry Query', 'Check System Status']
            };
        }
    },

    async getChatSuggestions() {
        try {
            const res = await fetch(`${API_BASE}/api/chat/suggestions`);
            return await res.json();
        } catch (e) {
            return [
                "How to reach AI Lab from Main Gate?",
                "What are the Library timings?",
                "Where is the Canteen?",
                "Tell me about CSE Department"
            ];
        }
    },

    async findRoute(source, destination, algorithm = 'BFS') {
        const endpoint = algorithm.toUpperCase() === 'DFS' 
            ? `${API_BASE}/api/navigation/dfs` 
            : `${API_BASE}/api/navigation/bfs`;

        try {
            const res = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ source, destination, algorithm })
            });
            if (!res.ok) throw new Error(`HTTP error ${res.status}`);
            return await res.json();
        } catch (e) {
            console.error('Navigation API Error:', e);
            return {
                found: false,
                message: 'Failed to compute route. Make sure the Java backend is active.'
            };
        }
    },

    async compareAlgorithms(source, destination) {
        try {
            const res = await fetch(`${API_BASE}/api/navigation/compare`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ source, destination })
            });
            return await res.json();
        } catch (e) {
            console.error('Comparison API Error:', e);
            return null;
        }
    },

    async getAllLocations() {
        try {
            const res = await fetch(`${API_BASE}/api/locations`);
            return await res.json();
        } catch (e) {
            console.error('Locations API Error:', e);
            return [];
        }
    },

    async searchLocations(query) {
        try {
            const res = await fetch(`${API_BASE}/api/locations/search?query=${encodeURIComponent(query)}`);
            return await res.json();
        } catch (e) {
            console.error('Search API Error:', e);
            return [];
        }
    },

    async getCategories() {
        try {
            const res = await fetch(`${API_BASE}/api/locations/categories`);
            return await res.json();
        } catch (e) {
            return [];
        }
    },

    async getAdminGraph() {
        try {
            const res = await fetch(`${API_BASE}/api/admin/graph`);
            return await res.json();
        } catch (e) {
            return { locations: [], edges: [], stats: {} };
        }
    },

    async addLocation(locData) {
        try {
            const res = await fetch(`${API_BASE}/api/admin/locations`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(locData)
            });
            return await res.json();
        } catch (e) {
            return { error: e.message };
        }
    },

    async addEdge(source, destination, distanceMeters, walkMinutes, bidirectional = true) {
        try {
            const res = await fetch(`${API_BASE}/api/admin/edges?bidirectional=${bidirectional}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ source, destination, distanceMeters, walkMinutes })
            });
            return await res.json();
        } catch (e) {
            return { error: e.message };
        }
    },

    async reloadGraph() {
        try {
            const res = await fetch(`${API_BASE}/api/admin/reload`, { method: 'POST' });
            return await res.json();
        } catch (e) {
            return { error: e.message };
        }
    }
};
