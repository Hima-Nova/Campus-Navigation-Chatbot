/**
 * Campus Interactive SVG Map Engine
 * Handles dynamic rendering, zoom/pan, node selection, and route animation.
 */

const CampusMap = {
    svg: null,
    gMap: null,
    locations: [],
    edges: [],
    currentRoute: [],
    activeSource: null,
    activeDest: null,

    // Pan & Zoom state
    zoom: 1,
    panX: 0,
    panY: 0,
    isDragging: false,
    startX: 0,
    startY: 0,

    categoryColors: {
        ENTRY: '#10b981',
        ACADEMIC: '#818cf8',
        LABORATORY: '#38bdf8',
        ADMINISTRATION: '#f59e0b',
        LIBRARY: '#a855f7',
        FOOD: '#34d399',
        HOSTEL: '#f43f5e',
        SPORTS: '#fb923c',
        HEALTH: '#ef4444',
        AMENITIES: '#c084fc',
        PARKING: '#64748b',
        OTHER: '#94a3b8'
    },

    init(svgElementId = 'campus-svg-map') {
        this.svg = document.getElementById(svgElementId);
        if (!this.svg) return;

        this.svg.innerHTML = ''; // Clear

        // Create main scalable group
        this.gMap = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        this.gMap.setAttribute('id', 'map-viewport-group');
        this.svg.appendChild(this.gMap);

        this.attachEventListeners();
    },

    attachEventListeners() {
        if (!this.svg) return;

        this.svg.addEventListener('mousedown', (e) => {
            if (e.target.closest('.map-node')) return; // Allow node clicking
            this.isDragging = true;
            this.startX = e.clientX - this.panX;
            this.startY = e.clientY - this.panY;
            this.svg.style.cursor = 'grabbing';
        });

        window.addEventListener('mousemove', (e) => {
            if (!this.isDragging) return;
            this.panX = e.clientX - this.startX;
            this.panY = e.clientY - this.startY;
            this.updateTransform();
        });

        window.addEventListener('mouseup', () => {
            this.isDragging = false;
            if (this.svg) this.svg.style.cursor = 'grab';
        });

        this.svg.addEventListener('wheel', (e) => {
            e.preventDefault();
            const delta = e.deltaY > 0 ? -0.1 : 0.1;
            this.setZoom(this.zoom + delta);
        }, { passive: false });
    },

    setZoom(newZoom) {
        this.zoom = Math.min(Math.max(0.6, newZoom), 2.5);
        this.updateTransform();
    },

    resetView() {
        this.zoom = 1;
        this.panX = 0;
        this.panY = 0;
        this.updateTransform();
    },

    updateTransform() {
        if (this.gMap) {
            this.gMap.setAttribute('transform', `translate(${this.panX}, ${this.panY}) scale(${this.zoom})`);
        }
    },

    renderMapData(locations, edges) {
        this.locations = locations || [];
        this.edges = edges || [];

        if (!this.gMap) return;
        this.gMap.innerHTML = '';

        // 1. Defs for glow filters & markers
        const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
        defs.innerHTML = `
            <filter id="glow-route" x="-30%" y="-30%" width="160%" height="160%">
                <feGaussianBlur stdDeviation="3" result="blur" />
                <feMerge>
                    <feMergeNode in="blur" />
                    <feMergeNode in="SourceGraphic" />
                </feMerge>
            </filter>
            <filter id="glow-node" x="-40%" y="-40%" width="180%" height="180%">
                <feGaussianBlur stdDeviation="4" result="blur" />
                <feMerge>
                    <feMergeNode in="blur" />
                    <feMergeNode in="SourceGraphic" />
                </feMerge>
            </filter>
        `;
        this.gMap.appendChild(defs);

        // Group for Background Grid
        const gGrid = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        gGrid.setAttribute('opacity', '0.15');
        for (let x = 0; x <= 1000; x += 100) {
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', x); line.setAttribute('y1', 0);
            line.setAttribute('x2', x); line.setAttribute('y2', 900);
            line.setAttribute('stroke', '#6366f1'); line.setAttribute('stroke-width', '1');
            gGrid.appendChild(line);
        }
        for (let y = 0; y <= 900; y += 100) {
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', 0); line.setAttribute('y1', y);
            line.setAttribute('x2', 1000); line.setAttribute('y2', y);
            line.setAttribute('stroke', '#6366f1'); line.setAttribute('stroke-width', '1');
            gGrid.appendChild(line);
        }
        this.gMap.appendChild(gGrid);

        // 2. Render Walkway Edges
        const gEdges = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        gEdges.setAttribute('id', 'edges-group');

        this.edges.forEach((edge, idx) => {
            const src = this.locations.find(l => l.name === edge.source);
            const dst = this.locations.find(l => l.name === edge.destination);
            if (src && dst && src.mapCoordinates && dst.mapCoordinates) {
                const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                line.setAttribute('x1', src.mapCoordinates.x);
                line.setAttribute('y1', src.mapCoordinates.y);
                line.setAttribute('x2', dst.mapCoordinates.x);
                line.setAttribute('y2', dst.mapCoordinates.y);
                line.setAttribute('class', 'map-edge');
                line.setAttribute('data-source', src.name);
                line.setAttribute('data-dest', dst.name);
                line.setAttribute('id', `edge-${idx}`);
                gEdges.appendChild(line);
            }
        });
        this.gMap.appendChild(gEdges);

        // 3. Render Campus Location Nodes
        const gNodes = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        gNodes.setAttribute('id', 'nodes-group');

        this.locations.forEach(loc => {
            if (!loc.mapCoordinates) return;
            const x = loc.mapCoordinates.x;
            const y = loc.mapCoordinates.y;
            const color = this.categoryColors[loc.category] || '#94a3b8';

            const gNode = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            gNode.setAttribute('class', 'map-node');
            gNode.setAttribute('data-name', loc.name);
            gNode.setAttribute('transform', `translate(${x}, ${y})`);

            // Outer Pulse Ring
            const outerCircle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            outerCircle.setAttribute('r', '14');
            outerCircle.setAttribute('fill', color);
            outerCircle.setAttribute('opacity', '0.2');
            outerCircle.setAttribute('class', 'node-pulse');

            // Inner Core Circle
            const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            circle.setAttribute('r', '9');
            circle.setAttribute('fill', color);
            circle.setAttribute('stroke', '#ffffff');
            circle.setAttribute('stroke-width', '1.5');

            // Label
            const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.setAttribute('y', '22');
            text.textContent = loc.name;

            gNode.appendChild(outerCircle);
            gNode.appendChild(circle);
            gNode.appendChild(text);

            // Click Handler for Popover
            gNode.addEventListener('click', (e) => {
                e.stopPropagation();
                this.showNodePopover(loc, x, y);
            });

            gNodes.appendChild(gNode);
        });
        this.gMap.appendChild(gNodes);

        // Re-apply route highlight if already selected
        if (this.currentRoute && this.currentRoute.length > 0) {
            this.highlightRoute(this.currentRoute, this.activeSource, this.activeDest);
        }
    },

    highlightRoute(path, source, destination) {
        this.currentRoute = path || [];
        this.activeSource = source;
        this.activeDest = destination;

        if (!this.gMap) return;

        // Reset all edges and nodes
        const edges = this.gMap.querySelectorAll('.map-edge');
        edges.forEach(e => e.classList.remove('active-route'));

        const nodes = this.gMap.querySelectorAll('.map-node');
        nodes.forEach(n => {
            n.classList.remove('active-start', 'active-dest', 'active-path');
        });

        if (!path || path.length === 0) return;

        // Highlight nodes along the path
        path.forEach((locName, idx) => {
            const nodeEl = this.gMap.querySelector(`.map-node[data-name="${CSS.escape(locName)}"]`);
            if (nodeEl) {
                if (idx === 0) {
                    nodeEl.classList.add('active-start');
                } else if (idx === path.length - 1) {
                    nodeEl.classList.add('active-dest');
                } else {
                    nodeEl.classList.add('active-path');
                }
            }
        });

        // Highlight edges between path nodes
        for (let i = 0; i < path.length - 1; i++) {
            const u = path[i];
            const v = path[i + 1];

            edges.forEach(edge => {
                const s = edge.getAttribute('data-source');
                const d = edge.getAttribute('data-dest');
                if ((s === u && d === v) || (s === v && d === u)) {
                    edge.classList.add('active-route');
                }
            });
        }
    },

    clearRoute() {
        this.highlightRoute([], null, null);
        const popover = document.getElementById('map-popover');
        if (popover) popover.style.display = 'none';
    },

    showNodePopover(loc, x, y) {
        let popover = document.getElementById('map-popover');
        if (!popover) {
            popover = document.createElement('div');
            popover.setAttribute('id', 'map-popover');
            popover.className = 'map-popover';
            const wrapper = document.querySelector('.map-canvas-wrapper');
            if (wrapper) wrapper.appendChild(popover);
        }

        popover.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                <span class="loc-cat-badge">${loc.category || 'LOCATION'}</span>
                <button onclick="document.getElementById('map-popover').style.display='none'" style="background:none; border:none; color:var(--text-muted); cursor:pointer; font-size:1.1rem;">&times;</button>
            </div>
            <h4 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.2rem;">${loc.name}</h4>
            <p style="font-size: 0.8rem; color: var(--secondary-light); margin-bottom: 0.5rem;">📍 ${loc.block || ''} • ${loc.floor || ''}</p>
            <p style="font-size: 0.8rem; color: var(--text-secondary); line-height: 1.4; margin-bottom: 0.75rem;">${loc.description || ''}</p>
            <p style="font-size: 0.75rem; color: #34d399; margin-bottom: 1rem;">🕒 ${loc.timings || 'Open Regular Hours'}</p>
            <div style="display: flex; gap: 0.5rem;">
                <button class="btn-primary" style="flex:1; padding:0.45rem 0.6rem; font-size:0.8rem;" onclick="App.setNavDestination('${loc.name.replace(/'/g, "\\'")}')">
                    🧭 Route To
                </button>
                <button class="btn-secondary" style="flex:1; padding:0.45rem 0.6rem; font-size:0.8rem;" onclick="App.setNavSource('${loc.name.replace(/'/g, "\\'")}')">
                    📍 Start From
                </button>
            </div>
        `;
        popover.style.display = 'block';
    }
};
