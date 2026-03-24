/**
 * GeoReport - Map Utilities
 * Leaflet.js map helpers and location picker
 */

class MapManager {
    constructor(containerId, options = {}) {
        this.containerId = containerId;
        this.map = null;
        this.markers = [];
        this.selectedMarker = null;
        this.options = {
            center: [8.7139, 77.7567], // Default: Tirunelveli, Tamil Nadu
            zoom: 12,
            ...options
        };
    }

    /**
     * Initialize the map
     * @param {boolean} showLocationControl - Whether to show the "Use My Location" button
     */
    init(showLocationControl = true) {
        const container = document.getElementById(this.containerId);
        if (!container) {
            console.error(`Map container '${this.containerId}' not found`);
            return null;
        }

        if (typeof L === 'undefined') {
            console.error('Leaflet (L) is not defined. Check your internet connection or script imports.');
            const container = document.getElementById(this.containerId);
            if (container) {
                container.innerHTML = '<div class="flex items-center justify-center h-full text-red-400">Map library failed to load. Please check internet connection.</div>';
            }
            return null;
        }

        // Clear loading state
        container.innerHTML = '';

        try {
            this.map = L.map(this.containerId, {
                zoomControl: true,
                attributionControl: true
            }).setView(this.options.center, this.options.zoom);

            // Define multiple map tile layers (newest and most updated)
            const baseLayers = {
                // Google Maps style - most updated
                'Google Streets': L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                    attribution: '&copy; Google Maps',
                    maxZoom: 20
                }),
                // Google Satellite - real imagery
                'Google Satellite': L.tileLayer('https://mt1.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {
                    attribution: '&copy; Google Maps',
                    maxZoom: 20
                }),
                // Google Hybrid - satellite with labels
                'Google Hybrid': L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', {
                    attribution: '&copy; Google Maps',
                    maxZoom: 20
                }),
                // CartoDB - clean modern style
                'CartoDB Light': L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
                    attribution: '&copy; OpenStreetMap, &copy; CartoDB',
                    maxZoom: 20
                }),
                // OpenStreetMap - fallback
                'OpenStreetMap': L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '&copy; OpenStreetMap contributors',
                    maxZoom: 19
                })
            };

            // Add Google Streets as default (most updated)
            baseLayers['Google Streets'].addTo(this.map);

            // Add layer control to switch between map types
            L.control.layers(baseLayers, null, { position: 'bottomright' }).addTo(this.map);

            // Add "Use My Location" control if requested
            if (showLocationControl) {
                this.addMyLocationControl();
                this.addSearchControl(); // Add location search box
            }

            return this.map;
        } catch (e) {
            console.error('Error initializing map:', e);
            return null;
        }
    }

    /**
     * Add "Use My Location" button control to the map
     * This allows users to quickly center the map on their current GPS position
     */
    addMyLocationControl() {
        if (!this.map) return;

        const locationControl = L.control({ position: 'topleft' });
        const self = this;

        locationControl.onAdd = function () {
            const div = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
            div.innerHTML = `
                <a href="#" id="myLocationBtn_${self.containerId}" title="Go to my current location" style="
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 34px;
                    height: 34px;
                    background: white;
                    border-radius: 4px;
                    cursor: pointer;
                ">
                    <span class="material-icons" style="font-size: 18px; color: #333;">my_location</span>
                </a>
            `;

            // Prevent map click event from firing when clicking the button
            L.DomEvent.disableClickPropagation(div);

            div.querySelector(`#myLocationBtn_${self.containerId}`).addEventListener('click', async (e) => {
                e.preventDefault();
                e.stopPropagation();

                // Show loading state
                const btn = e.currentTarget;
                const icon = btn.querySelector('.material-icons');
                const originalIcon = icon.textContent;
                icon.textContent = 'hourglass_empty';
                icon.style.animation = 'spin 1s linear infinite';

                try {
                    const loc = await self.getCurrentLocation();
                    self.centerOn(loc.latitude, loc.longitude, 16);

                    // Add a pulsing marker at the current location
                    self.showCurrentLocationMarker(loc.latitude, loc.longitude);

                    if (window.showToast) {
                        showToast('Location found!', 'success');
                    }
                } catch (error) {
                    console.error('Error getting location:', error);
                    if (window.showToast) {
                        showToast('Could not get your location. Please enable location services.', 'error');
                    }
                } finally {
                    // Restore button state
                    icon.textContent = originalIcon;
                    icon.style.animation = '';
                }
            });

            return div;
        };

        locationControl.addTo(this.map);
    }

    /**
     * Show a pulsing marker at the current location
     */
    showCurrentLocationMarker(lat, lng) {
        // Remove existing current location marker if any
        if (this.currentLocationMarker) {
            this.map.removeLayer(this.currentLocationMarker);
        }

        // Create a pulsing marker for current location
        const pulsingIcon = L.divIcon({
            className: 'current-location-marker',
            html: `
                <div style="
                    position: relative;
                    width: 20px;
                    height: 20px;
                ">
                    <div style="
                        position: absolute;
                        width: 20px;
                        height: 20px;
                        background: #4285f4;
                        border: 3px solid white;
                        border-radius: 50%;
                        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
                        z-index: 2;
                    "></div>
                    <div style="
                        position: absolute;
                        width: 40px;
                        height: 40px;
                        background: rgba(66, 133, 244, 0.3);
                        border-radius: 50%;
                        top: -10px;
                        left: -10px;
                        animation: pulse 2s ease-out infinite;
                        z-index: 1;
                    "></div>
                </div>
            `,
            iconSize: [20, 20],
            iconAnchor: [10, 10]
        });

        this.currentLocationMarker = L.marker([lat, lng], {
            icon: pulsingIcon,
            zIndexOffset: 1000
        }).addTo(this.map);

        // Add popup with coordinates
        this.currentLocationMarker.bindPopup(`
            <div style="text-align: center; padding: 5px;">
                <strong>Your Current Location</strong><br>
                <small>Lat: ${lat.toFixed(6)}<br>Lng: ${lng.toFixed(6)}</small>
            </div>
        `).openPopup();

        // Auto-close popup after 3 seconds
        setTimeout(() => {
            if (this.currentLocationMarker) {
                this.currentLocationMarker.closePopup();
            }
        }, 3000);
    }

    /**
     * Get user's current location with improved accuracy
     */
    getCurrentLocation() {
        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                reject(new Error('Geolocation is not supported by your browser'));
                return;
            }

            // Show loading message
            if (window.showToast) {
                showToast('Getting your location...', 'info');
            }

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    resolve({
                        latitude: position.coords.latitude,
                        longitude: position.coords.longitude,
                        accuracy: position.coords.accuracy
                    });
                },
                (error) => {
                    let errorMessage = 'Could not get your location. ';
                    switch (error.code) {
                        case error.PERMISSION_DENIED:
                            errorMessage += 'Please allow location access in your browser settings.';
                            break;
                        case error.POSITION_UNAVAILABLE:
                            errorMessage += 'Location information is unavailable. Try the search box instead.';
                            break;
                        case error.TIMEOUT:
                            errorMessage += 'Location request timed out. Try again or use the search box.';
                            break;
                        default:
                            errorMessage += 'An unknown error occurred.';
                    }
                    reject(new Error(errorMessage));
                },
                {
                    enableHighAccuracy: true,
                    timeout: 15000,
                    maximumAge: 0 // Don't use cached position
                }
            );
        });
    }

    /**
     * Add a search box for location lookup
     */
    addSearchControl() {
        if (!this.map) return;

        const searchControl = L.control({ position: 'topright' });
        const self = this;

        searchControl.onAdd = function () {
            const div = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
            div.innerHTML = `
                <div style="
                    background: white;
                    padding: 5px;
                    border-radius: 4px;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.3);
                ">
                    <input type="text" id="locationSearch_${self.containerId}" 
                        placeholder="Search location..." 
                        style="
                            border: 1px solid #ddd;
                            padding: 8px 12px;
                            border-radius: 4px;
                            width: 200px;
                            font-size: 14px;
                        "
                    />
                    <button id="searchBtn_${self.containerId}" style="
                        background: #4285f4;
                        color: white;
                        border: none;
                        padding: 8px 12px;
                        border-radius: 4px;
                        cursor: pointer;
                        margin-left: 5px;
                    ">
                        <span class="material-icons" style="font-size: 16px; vertical-align: middle;">search</span>
                    </button>
                </div>
            `;

            L.DomEvent.disableClickPropagation(div);

            return div;
        };

        searchControl.addTo(this.map);

        // Add search functionality after control is added
        setTimeout(() => {
            const searchInput = document.getElementById(`locationSearch_${this.containerId}`);
            const searchBtn = document.getElementById(`searchBtn_${this.containerId}`);

            if (searchBtn && searchInput) {
                const doSearch = () => this.searchLocation(searchInput.value);

                searchBtn.addEventListener('click', doSearch);
                searchInput.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') doSearch();
                });
            }
        }, 100);
    }

    /**
     * Search for a location using OpenStreetMap Nominatim API
     */
    async searchLocation(query) {
        if (!query || query.trim() === '') {
            if (window.showToast) showToast('Please enter a location to search', 'warning');
            return;
        }

        try {
            if (window.showToast) showToast('Searching...', 'info');

            const response = await fetch(
                `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`
            );
            const results = await response.json();

            if (results && results.length > 0) {
                const place = results[0];
                const lat = parseFloat(place.lat);
                const lng = parseFloat(place.lon);

                this.centerOn(lat, lng, 16);
                this.showCurrentLocationMarker(lat, lng);

                if (window.showToast) {
                    showToast(`Found: ${place.display_name.substring(0, 50)}...`, 'success');
                }

                return { latitude: lat, longitude: lng, name: place.display_name };
            } else {
                if (window.showToast) showToast('Location not found. Try a different search.', 'error');
            }
        } catch (error) {
            console.error('Search error:', error);
            if (window.showToast) showToast('Search failed. Please try again.', 'error');
        }
    }

    /**
     * Center map on location
     */
    centerOn(lat, lng, zoom = 15) {
        if (this.map) {
            this.map.setView([lat, lng], zoom);
        }
    }

    /**
     * Add location picker functionality
     */
    enableLocationPicker(onLocationSelected) {
        if (!this.map) return;

        // Add click handler
        this.map.on('click', (e) => {
            const { lat, lng } = e.latlng;
            this.setPickedLocation(lat, lng);
            if (onLocationSelected) {
                onLocationSelected(lat, lng);
            }
        });

        // Add "Use My Location" control
        const locationControl = L.control({ position: 'topleft' });
        locationControl.onAdd = () => {
            const div = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
            div.innerHTML = `
                <a href="#" id="useMyLocation" title="Use my location" style="
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 34px;
                    height: 34px;
                    background: white;
                    border-radius: 4px;
                ">
                    <span class="material-icons" style="font-size: 18px; color: #333;">my_location</span>
                </a>
            `;

            div.querySelector('#useMyLocation').addEventListener('click', async (e) => {
                e.preventDefault();
                try {
                    const loc = await this.getCurrentLocation();
                    this.centerOn(loc.latitude, loc.longitude);
                    this.setPickedLocation(loc.latitude, loc.longitude);
                    if (onLocationSelected) {
                        onLocationSelected(loc.latitude, loc.longitude);
                    }
                } catch (error) {
                    console.error('Error getting location:', error);
                    if (window.showToast) {
                        showToast('Could not get your location', 'error');
                    }
                }
            });

            return div;
        };
        locationControl.addTo(this.map);
    }

    /**
     * Set picked location marker
     */
    setPickedLocation(lat, lng) {
        // Remove existing picked marker
        if (this.selectedMarker) {
            this.map.removeLayer(this.selectedMarker);
        }

        // Create new marker
        const icon = L.divIcon({
            className: 'custom-div-icon',
            html: `
                <div style="
                    width: 30px;
                    height: 30px;
                    background: #6366f1;
                    border: 3px solid white;
                    border-radius: 50%;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.3);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <span class="material-icons text-white text-sm">place</span>
                </div>
            `,
            iconSize: [30, 30],
            iconAnchor: [15, 30]
        });

        this.selectedMarker = L.marker([lat, lng], { icon, draggable: true })
            .addTo(this.map);

        // Handle drag
        this.selectedMarker.on('dragend', (e) => {
            const pos = e.target.getLatLng();
            if (this.options.onLocationChange) {
                this.options.onLocationChange(pos.lat, pos.lng);
            }
        });
    }

    /**
     * Add issue markers to map
     */
    addIssueMarkers(issues, onClick) {
        // Clear existing markers
        this.clearMarkers();

        issues.forEach(issue => {
            if (issue.latitude && issue.longitude) {
                const marker = GeoReport.createIssueMarker(issue, this.map, onClick);
                this.markers.push(marker);
            }
        });
    }

    /**
     * Clear all markers
     */
    clearMarkers() {
        this.markers.forEach(marker => {
            this.map.removeLayer(marker);
        });
        this.markers = [];
    }

    /**
     * Fit map to show all markers
     */
    fitToMarkers() {
        if (this.markers.length === 0) return;

        const group = new L.featureGroup(this.markers);
        this.map.fitBounds(group.getBounds().pad(0.1));
    }

    /**
     * Get current map bounds
     */
    getBounds() {
        if (!this.map) return null;
        const bounds = this.map.getBounds();
        return {
            minLng: bounds.getWest(),
            minLat: bounds.getSouth(),
            maxLng: bounds.getEast(),
            maxLat: bounds.getNorth()
        };
    }

    /**
     * Resize map (call after container size change)
     */
    invalidateSize() {
        if (this.map) {
            this.map.invalidateSize();
        }
    }

    /**
     * Destroy map instance
     */
    destroy() {
        if (this.map) {
            this.map.remove();
            this.map = null;
        }
    }
}

// Export
window.MapManager = MapManager;
