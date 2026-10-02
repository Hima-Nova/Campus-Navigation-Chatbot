/**
 * CampusNav AI — Main Application Controller
 * Handles UI events, chat state, navigation execution, directory filtering, and viva tools.
 */

const App = {
    currentTab: 'home',
    locations: [],
    edges: [],
    currentNavResult: null,

    async init() {
        console.log('Initializing CampusNav AI Application...');
        this.setupTabNavigation();
        this.setupChatEvents();
        this.setupNavEvents();
        this.setupDirectoryEvents();
        this.setupAdminEvents();
        
        CampusMap.init('campus-svg-map');

        // Load initial campus data
        await this.loadCampusData();
        await this.checkSystemHealth();

        // Check health every 15 seconds
        setInterval(() => this.checkSystemHealth(), 15000);
    },

    // -------------------------------------------------------------
    // Tab Navigation
    // -------------------------------------------------------------
    setupTabNavigation() {
        const navBtns = document.querySelectorAll('.nav-btn');
        navBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const target = btn.getAttribute('data-tab');
                this.switchTab(target);
            });
        });
    },

    switchTab(tabId) {
        this.currentTab = tabId;

        // Update nav buttons
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
        });

        // Update tab views
        document.querySelectorAll('.tab-view').forEach(view => {
            view.classList.toggle('active', view.id === `tab-${tabId}`);
        });

        // Special Tab activation hooks
        if (tabId === 'map') {
            setTimeout(() => {
                CampusMap.renderMapData(this.locations, this.edges);
            }, 100);
        } else if (tabId === 'locations') {
            this.renderLocationsDirectory(this.locations);
        } else if (tabId === 'admin') {
            this.loadAdminData();
        }

        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    // -------------------------------------------------------------
    // System Health & Data Loading
    // -------------------------------------------------------------
    async checkSystemHealth() {
        const health = await Api.checkHealth();
        const badge = document.getElementById('header-health-badge');
        const dot = document.getElementById('header-health-dot');
        const text = document.getElementById('header-health-text');

        if (health.status === 'UP') {
            if (badge) badge.style.borderColor = 'rgba(16, 185, 129, 0.4)';
            if (dot) dot.style.background = '#10b981';
            if (text) {
                const isPythonOk = health.pythonServiceStatus && health.pythonServiceStatus.includes('CONNECTED');
                text.textContent = isPythonOk ? 'AI Engine & Java Online' : 'Java Online (Fallback Mode)';
            }
        } else {
            if (badge) badge.style.borderColor = 'rgba(244, 63, 94, 0.4)';
            if (dot) dot.style.background = '#f43f5e';
            if (text) text.textContent = 'Backend Offline';
        }
    },

    async loadCampusData() {
        this.locations = await Api.getAllLocations();
        const adminGraph = await Api.getAdminGraph();
        this.edges = adminGraph.edges || [];

        this.populateLocationDropdowns();
        CampusMap.renderMapData(this.locations, this.edges);
        this.renderLocationsDirectory(this.locations);
    },

    populateLocationDropdowns() {
        const srcSelect = document.getElementById('nav-source-select');
        const dstSelect = document.getElementById('nav-dest-select');
        const compareSrc = document.getElementById('compare-source-select');
        const compareDst = document.getElementById('compare-dest-select');

        if (!srcSelect || !dstSelect) return;

        srcSelect.innerHTML = '<option value="">-- Select Starting Location --</option>';
        dstSelect.innerHTML = '<option value="">-- Select Destination --</option>';

        if (compareSrc && compareDst) {
            compareSrc.innerHTML = '<option value="">-- Select Starting Location --</option>';
            compareDst.innerHTML = '<option value="">-- Select Destination --</option>';
        }

        this.locations.forEach(loc => {
            const opt1 = document.createElement('option');
            opt1.value = loc.name;
            opt1.textContent = `${loc.name} (${loc.block || 'Campus'})`;
            srcSelect.appendChild(opt1);

            const opt2 = document.createElement('option');
            opt2.value = loc.name;
            opt2.textContent = `${loc.name} (${loc.block || 'Campus'})`;
            dstSelect.appendChild(opt2);

            if (compareSrc && compareDst) {
                compareSrc.appendChild(opt1.cloneNode(true));
                compareDst.appendChild(opt2.cloneNode(true));
            }
        });

        // Default selections
        srcSelect.value = 'Main Gate';
        dstSelect.value = 'AI Lab';
        if (compareSrc && compareDst) {
            compareSrc.value = 'Main Gate';
            compareDst.value = 'AI Lab';
        }
    },

    // -------------------------------------------------------------
    // Chatbot Component
    // -------------------------------------------------------------
    setupChatEvents() {
        const sendBtn = document.getElementById('btn-send-chat');
        const input = document.getElementById('chat-input-field');

        if (sendBtn && input) {
            sendBtn.addEventListener('click', () => this.handleSendMessage());
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') this.handleSendMessage();
            });
        }
    },

    async handleSendMessage(customQuery = null) {
        const input = document.getElementById('chat-input-field');
        const query = (customQuery || (input ? input.value : '')).trim();
        if (!query) return;

        if (input && !customQuery) input.value = '';

        // 1. Append User Message Bubble
        this.appendChatMessage('user', query);

        // 2. Show Thinking Indicator
        const thinkingId = this.showThinkingIndicator();

        // 3. Send to Java Chat API (which consults Python Intent Classifier)
        const response = await Api.sendChatMessage(query);

        // 4. Remove Thinking Indicator
        this.removeThinkingIndicator(thinkingId);

        // 5. Append Bot Response Bubble
        this.appendBotMessage(response);
    },

    appendChatMessage(sender, text) {
        const history = document.getElementById('chat-history-container');
        if (!history) return;

        const msgDiv = document.createElement('div');
        msgDiv.className = `chat-msg ${sender}`;

        msgDiv.innerHTML = `
            <div class="msg-bubble">
                <p>${this.escapeHtml(text)}</p>
            </div>
        `;

        history.appendChild(msgDiv);
        history.scrollTop = history.scrollHeight;
    },

    showThinkingIndicator() {
        const history = document.getElementById('chat-history-container');
        if (!history) return null;

        const id = 'thinking-' + Date.now();
        const div = document.createElement('div');
        div.className = 'chat-msg bot';
        div.id = id;

        div.innerHTML = `
            <div class="msg-bubble" style="display: flex; align-items: center; gap: 0.5rem; color: var(--text-muted);">
                <div class="status-dot" style="background: var(--primary);"></div>
                <span>Thinking & classifying intent...</span>
            </div>
        `;

        history.appendChild(div);
        history.scrollTop = history.scrollHeight;
        return id;
    },

    removeThinkingIndicator(id) {
        if (!id) return;
        const el = document.getElementById(id);
        if (el) el.remove();
    },

    appendBotMessage(resp) {
        const history = document.getElementById('chat-history-container');
        if (!history) return;

        const msgDiv = document.createElement('div');
        msgDiv.className = 'chat-msg bot';

        let innerHtml = `
            <div class="msg-bubble">
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; margin-bottom: 0.4rem;">
                    <span class="intent-chip">🎯 INTENT: ${resp.intent || 'UNKNOWN'}</span>
                    <span style="font-size: 0.7rem; color: ${resp.pythonClassifierStatus === 'CONNECTED' ? '#34d399' : '#f59e0b'};">
                        ${resp.pythonClassifierStatus === 'CONNECTED' ? '● Python NLP' : '▲ Java Fallback'}
                    </span>
                </div>
                <div style="white-space: pre-line;">${this.formatMarkdown(resp.response || '')}</div>
        `;

        // If Navigation result is embedded
        if (resp.navigationResult && resp.navigationResult.found) {
            const nav = resp.navigationResult;
            this.currentNavResult = nav;

            innerHtml += `
                <div class="route-inline-card">
                    <div style="font-size: 0.8rem; font-weight: 700; color: var(--secondary-light); display: flex; justify-content: space-between;">
                        <span>🧭 Route: ${nav.source} ➔ ${nav.destination}</span>
                        <span>[${nav.algorithm}]</span>
                    </div>
                    <div class="route-chain">
                        ${nav.path.map((node, i) => `
                            <span class="route-node-pill">${node}</span>
                            ${i < nav.path.length - 1 ? '<span class="route-arrow">➔</span>' : ''}
                        `).join('')}
                    </div>
                    <div class="route-metrics-bar">
                        <span>📏 <strong>${nav.distanceMeters}m</strong></span>
                        <span>⏱️ <strong>${nav.estimatedMinutes} min</strong></span>
                        <span>👣 <strong>${nav.steps} hops</strong></span>
                    </div>
                    <button class="btn-primary" style="margin-top: 0.6rem; width: 100%; padding: 0.45rem; font-size: 0.8rem;"
                            onclick="App.viewCurrentRouteOnMap()">
                        🗺️ View Highlighted on Campus Map
                    </button>
                </div>
            `;
        }

        // Quick action chips
        if (resp.quickActions && resp.quickActions.length > 0) {
            innerHtml += `
                <div class="chat-actions-row">
                    ${resp.quickActions.map(action => `
                        <button class="quick-action-btn" onclick="App.handleQuickAction('${action.replace(/'/g, "\\'")}')">
                            ${action}
                        </button>
                    `).join('')}
                </div>
            `;
        }

        innerHtml += `</div>`;
        msgDiv.innerHTML = innerHtml;

        history.appendChild(msgDiv);
        history.scrollTop = history.scrollHeight;
    },

    handleQuickAction(actionText) {
        if (actionText === 'View on Campus Map' && this.currentNavResult) {
            this.viewCurrentRouteOnMap();
        } else {
            this.handleSendMessage(actionText);
        }
    },

    viewCurrentRouteOnMap() {
        if (!this.currentNavResult || !this.currentNavResult.path) return;
        this.switchTab('map');
        setTimeout(() => {
            CampusMap.highlightRoute(
                this.currentNavResult.path,
                this.currentNavResult.source,
                this.currentNavResult.destination
            );
        }, 150);
    },

    // -------------------------------------------------------------
    // Navigation Component (BFS / DFS)
    // -------------------------------------------------------------
    setupNavEvents() {
        const findBtn = document.getElementById('btn-find-route');
        const swapBtn = document.getElementById('btn-swap-locations');
        const algoCards = document.querySelectorAll('.algo-radio-card');

        if (findBtn) {
            findBtn.addEventListener('click', () => this.handleCalculateRoute());
        }

        if (swapBtn) {
            swapBtn.addEventListener('click', () => {
                const src = document.getElementById('nav-source-select');
                const dst = document.getElementById('nav-dest-select');
                if (src && dst) {
                    const temp = src.value;
                    src.value = dst.value;
                    dst.value = temp;
                }
            });
        }

        algoCards.forEach(card => {
            card.addEventListener('click', () => {
                algoCards.forEach(c => c.classList.remove('active'));
                card.classList.add('active');
            });
        });

        // Compare button
        const compareBtn = document.getElementById('btn-run-comparison');
        if (compareBtn) {
            compareBtn.addEventListener('click', () => this.handleRunAlgorithmComparison());
        }
    },

    async handleCalculateRoute() {
        const srcSelect = document.getElementById('nav-source-select');
        const dstSelect = document.getElementById('nav-dest-select');
        const activeAlgoCard = document.querySelector('.algo-radio-card.active');

        const source = srcSelect ? srcSelect.value : '';
        const destination = dstSelect ? dstSelect.value : '';
        const algorithm = activeAlgoCard ? activeAlgoCard.getAttribute('data-algo') : 'BFS';

        if (!source || !destination) {
            alert('Please select both a Starting Location and a Destination.');
            return;
        }

        const resContainer = document.getElementById('nav-result-container');
        if (resContainer) {
            resContainer.innerHTML = `
                <div class="card" style="text-align: center; padding: 2rem;">
                    <div class="status-dot" style="background: var(--secondary-light); margin: 0 auto 1rem;"></div>
                    <p>Executing <strong>${algorithm} Pathfinding</strong> on campus graph...</p>
                </div>
            `;
        }

        const navResp = await Api.findRoute(source, destination, algorithm);
        this.currentNavResult = navResp;
        this.renderNavigationResult(navResp);
    },

    renderNavigationResult(nav) {
        const resContainer = document.getElementById('nav-result-container');
        if (!resContainer) return;

        if (!nav.found) {
            resContainer.innerHTML = `
                <div class="card" style="border-color: var(--accent-rose);">
                    <h3 style="color: var(--accent-rose); margin-bottom: 0.5rem;">⚠️ No Route Found</h3>
                    <p style="color: var(--text-secondary);">${nav.message || 'No walkable path connects these two locations.'}</p>
                </div>
            `;
            return;
        }

        let html = `
            <div class="nav-result-card">
                <div class="result-hero-box">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                        <span class="hero-pill" style="margin: 0;">ALGORITHM: ${nav.algorithm} PATHFINDING</span>
                        <span style="font-size: 0.8rem; font-family: var(--font-mono); color: var(--text-muted);">
                            Time: ${nav.timeComplexity || 'O(V+E)'} | Space: ${nav.spaceComplexity || 'O(V)'}
                        </span>
                    </div>
                    <h3 style="font-size: 1.35rem; font-weight: 800;">
                        ${nav.source} <span style="color: var(--secondary-light);">➔</span> ${nav.destination}
                    </h3>
                    <p style="color: var(--text-secondary); font-size: 0.9rem; margin-top: 0.25rem;">
                        ${nav.message}
                    </p>

                    <div class="result-metrics-grid">
                        <div class="metric-pill">
                            <div class="metric-val">${nav.steps}</div>
                            <div class="metric-name">Graph Hops</div>
                        </div>
                        <div class="metric-pill">
                            <div class="metric-val">${nav.distanceMeters}m</div>
                            <div class="metric-name">Total Distance</div>
                        </div>
                        <div class="metric-pill">
                            <div class="metric-val">${nav.estimatedMinutes} min</div>
                            <div class="metric-name">Walking Time</div>
                        </div>
                        <div class="metric-pill">
                            <div class="metric-val">${nav.path.length}</div>
                            <div class="metric-name">Nodes Visited</div>
                        </div>
                    </div>

                    <div style="display: flex; gap: 0.75rem; margin-top: 1.25rem;">
                        <button class="btn-primary" onclick="App.viewCurrentRouteOnMap()">
                            🗺️ Visualize Route on Campus Map
                        </button>
                    </div>
                </div>

                <!-- Turn by Turn Instructions -->
                <div class="card">
                    <h4 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem;">
                        👣 Step-by-Step Walking Directions
                    </h4>
                    <div class="turn-by-turn-list">
                        ${nav.turnByTurnDirections && nav.turnByTurnDirections.length > 0
                            ? nav.turnByTurnDirections.map((step, idx) => `
                                <div class="turn-step-item">
                                    <div class="step-num-badge">${idx + 1}</div>
                                    <div class="step-details">
                                        <h4>${step}</h4>
                                    </div>
                                </div>
                            `).join('')
                            : `<p style="color: var(--text-muted);">You have reached your destination.</p>`
                        }
                    </div>
                </div>
            </div>
        `;

        resContainer.innerHTML = html;
    },

    setNavSource(name) {
        const sel = document.getElementById('nav-source-select');
        if (sel) sel.value = name;
        this.switchTab('navigate');
    },

    setNavDestination(name) {
        const sel = document.getElementById('nav-dest-select');
        if (sel) sel.value = name;
        this.switchTab('navigate');
    },

    // -------------------------------------------------------------
    // Location Directory Component
    // -------------------------------------------------------------
    setupDirectoryEvents() {
        const searchInput = document.getElementById('directory-search-input');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                const q = e.target.value.toLowerCase().trim();
                const filtered = this.locations.filter(loc => 
                    loc.name.toLowerCase().includes(q) ||
                    (loc.description && loc.description.toLowerCase().includes(q)) ||
                    (loc.block && loc.block.toLowerCase().includes(q)) ||
                    (loc.category && loc.category.toLowerCase().includes(q))
                );
                this.renderLocationsDirectory(filtered);
            });
        }
    },

    filterByCategory(categoryName) {
        document.querySelectorAll('.cat-chip').forEach(c => {
            c.classList.toggle('active', c.getAttribute('data-category') === categoryName);
        });

        if (categoryName === 'ALL') {
            this.renderLocationsDirectory(this.locations);
        } else {
            const filtered = this.locations.filter(l => l.category === categoryName);
            this.renderLocationsDirectory(filtered);
        }
    },

    renderLocationsDirectory(locList) {
        const grid = document.getElementById('locations-grid-container');
        if (!grid) return;

        if (!locList || locList.length === 0) {
            grid.innerHTML = `<p style="color: var(--text-muted); grid-column: 1/-1;">No campus locations match your search query.</p>`;
            return;
        }

        grid.innerHTML = locList.map(loc => `
            <div class="location-item-card">
                <div>
                    <div class="loc-top-row">
                        <span class="loc-cat-badge">${loc.category || 'LOCATION'}</span>
                        <span class="loc-timing-badge">🕒 ${loc.timings ? loc.timings.split('(')[0] : 'Open'}</span>
                    </div>
                    <h3 class="loc-title">${loc.name}</h3>
                    <p class="loc-pos">📍 ${loc.block || 'Campus Wing'} • ${loc.floor || 'Ground'}</p>
                    <p class="loc-desc" style="margin-top: 0.5rem;">${loc.description || ''}</p>
                </div>

                <div>
                    ${loc.facilities && loc.facilities.length > 0 ? `
                        <div class="loc-facilities-tags" style="margin-bottom: 0.75rem;">
                            ${loc.facilities.slice(0, 3).map(f => `<span class="fac-tag">${f}</span>`).join('')}
                        </div>
                    ` : ''}

                    <div style="display: flex; gap: 0.5rem;">
                        <button class="btn-primary" style="flex:1; padding: 0.45rem; font-size: 0.8rem;"
                                onclick="App.setNavDestination('${loc.name.replace(/'/g, "\\'")}')">
                            🧭 Navigate Here
                        </button>
                        <button class="btn-secondary" style="padding: 0.45rem 0.75rem; font-size: 0.8rem;"
                                onclick="App.askAiAbout('${loc.name.replace(/'/g, "\\'")}')">
                            💬 Ask AI
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    },

    askAiAbout(locName) {
        this.switchTab('chat');
        this.handleSendMessage(`Tell me about ${locName}`);
    },

    // -------------------------------------------------------------
    // ADSA Visualizer & Algorithm Comparison
    // -------------------------------------------------------------
    async handleRunAlgorithmComparison() {
        const src = document.getElementById('compare-source-select').value;
        const dst = document.getElementById('compare-dest-select').value;

        if (!src || !dst) {
            alert('Please select both source and destination to compare.');
            return;
        }

        const container = document.getElementById('comparison-result-box');
        if (container) {
            container.innerHTML = '<p>Computing BFS and DFS paths...</p>';
        }

        const data = await Api.compareAlgorithms(src, dst);
        if (!data || !container) return;

        const bfs = data.bfs;
        const dfs = data.dfs;

        container.innerHTML = `
            <div class="algo-compare-grid" style="margin-top: 1rem;">
                <div class="algo-card" style="border-color: var(--primary);">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                        <span class="hero-pill">BFS (Shortest Path)</span>
                        <span style="color: #34d399; font-weight: 700;">Optimal</span>
                    </div>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.5rem;">
                        Path: <strong>${bfs.path.join(' ➔ ')}</strong>
                    </p>
                    <div class="result-metrics-grid" style="grid-template-columns: repeat(3, 1fr);">
                        <div class="metric-pill"><div class="metric-val">${bfs.steps}</div><div class="metric-name">Hops</div></div>
                        <div class="metric-pill"><div class="metric-val">${bfs.distanceMeters}m</div><div class="metric-name">Distance</div></div>
                        <div class="metric-pill"><div class="metric-val">${bfs.estimatedMinutes}m</div><div class="metric-name">Walk</div></div>
                    </div>
                </div>

                <div class="algo-card" style="border-color: var(--accent-purple);">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                        <span class="hero-pill" style="background: rgba(168, 85, 247, 0.15); color: var(--accent-purple); border-color: rgba(168, 85, 247, 0.3);">DFS (Exploration)</span>
                        <span style="color: var(--accent-amber); font-weight: 700;">Deep Traversal</span>
                    </div>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.5rem;">
                        Path: <strong>${dfs.path.join(' ➔ ')}</strong>
                    </p>
                    <div class="result-metrics-grid" style="grid-template-columns: repeat(3, 1fr);">
                        <div class="metric-pill"><div class="metric-val">${dfs.steps}</div><div class="metric-name">Hops</div></div>
                        <div class="metric-pill"><div class="metric-val">${dfs.distanceMeters}m</div><div class="metric-name">Distance</div></div>
                        <div class="metric-pill"><div class="metric-val">${dfs.estimatedMinutes}m</div><div class="metric-name">Walk</div></div>
                    </div>
                </div>
            </div>

            <div class="card" style="margin-top: 1rem; background: rgba(0,0,0,0.3);">
                <h4 style="color: var(--secondary-light); margin-bottom: 0.25rem;">💡 ADSA Viva Insight</h4>
                <p style="font-size: 0.85rem; color: var(--text-secondary);">
                    ${bfs.steps <= dfs.steps 
                        ? `BFS guarantees the minimum number of edge hops (${bfs.steps} hops vs ${dfs.steps} hops in DFS) because it expands uniformly level-by-level using a FIFO queue.` 
                        : `DFS traverses branch-first and backtracks, discovering a valid path but without shortest-path guarantees.`}
                </p>
            </div>
        `;
    },

    // -------------------------------------------------------------
    // Admin Component
    // -------------------------------------------------------------
    setupAdminEvents() {
        const formLoc = document.getElementById('admin-add-location-form');
        const formEdge = document.getElementById('admin-add-edge-form');
        const reloadBtn = document.getElementById('btn-admin-reload-graph');

        if (formLoc) {
            formLoc.addEventListener('submit', async (e) => {
                e.preventDefault();
                const locData = {
                    name: document.getElementById('admin-loc-name').value,
                    category: document.getElementById('admin-loc-cat').value,
                    block: document.getElementById('admin-loc-block').value,
                    floor: document.getElementById('admin-loc-floor').value,
                    description: document.getElementById('admin-loc-desc').value,
                    timings: document.getElementById('admin-loc-timings').value,
                    aliases: document.getElementById('admin-loc-aliases').value.split(',').map(s => s.trim()).filter(Boolean)
                };
                const res = await Api.addLocation(locData);
                alert(res.message || 'Location saved');
                formLoc.reset();
                await this.loadCampusData();
            });
        }

        if (formEdge) {
            formEdge.addEventListener('submit', async (e) => {
                e.preventDefault();
                const src = document.getElementById('admin-edge-src').value;
                const dst = document.getElementById('admin-edge-dst').value;
                const dist = parseInt(document.getElementById('admin-edge-dist').value) || 80;
                const time = parseInt(document.getElementById('admin-edge-time').value) || 1;

                const res = await Api.addEdge(src, dst, dist, time, true);
                alert(res.message || 'Edge connection created');
                formEdge.reset();
                await this.loadCampusData();
            });
        }

        if (reloadBtn) {
            reloadBtn.addEventListener('click', async () => {
                const res = await Api.reloadGraph();
                alert(res.message || 'Graph reloaded');
                await this.loadCampusData();
            });
        }
    },

    async loadAdminData() {
        const adminGraph = await Api.getAdminGraph();
        const locTable = document.getElementById('admin-locations-tbody');
        const edgeTable = document.getElementById('admin-edges-tbody');

        if (locTable) {
            locTable.innerHTML = (adminGraph.locations || []).map(l => `
                <tr>
                    <td><strong>${l.name}</strong></td>
                    <td>${l.category}</td>
                    <td>${l.block}</td>
                    <td>${l.floor}</td>
                    <td>${l.timings || '-'}</td>
                </tr>
            `).join('');
        }

        if (edgeTable) {
            edgeTable.innerHTML = (adminGraph.edges || []).map(e => `
                <tr>
                    <td>${e.source}</td>
                    <td>${e.destination}</td>
                    <td>${e.distanceMeters}m</td>
                    <td>${e.walkMinutes} min</td>
                </tr>
            `).join('');
        }
    },

    // -------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------
    escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    },

    formatMarkdown(text) {
        if (!text) return '';
        return text
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            .replace(/•/g, '&bull;');
    }
};

// Start application when DOM is ready
document.addEventListener('DOMContentLoaded', () => App.init());
