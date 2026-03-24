/**
 * GeoReport - Main Application JavaScript
 * Utility functions and shared code
 */

// API Configuration
const API_BASE = '';

/**
 * Get auth token from localStorage
 */
function getAuthToken() {
    return localStorage.getItem('token');
}

/**
 * Get current user from localStorage
 */
function getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

/**
 * Check if user is authenticated
 */
function isAuthenticated() {
    return !!getAuthToken();
}

/**
 * Check if user is admin
 */
function isAdmin() {
    const user = getCurrentUser();
    return user && user.roles && user.roles.includes('ROLE_ADMIN');
}

/**
 * Logout user
 */
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login.html';
}

/**
 * Make authenticated API request
 */
async function apiRequest(endpoint, options = {}) {
    const token = getAuthToken();

    const headers = {
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Don't set Content-Type for FormData
    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    // Handle 401 Unauthorized
    if (response.status === 401) {
        logout();
        throw new Error('Session expired. Please login again.');
    }

    return response.json();
}

/**
 * Show toast notification
 */
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    if (!toast) return;

    const icon = type === 'success' ? 'check_circle' :
        type === 'error' ? 'error' : 'warning';

    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span class="material-icons">${icon}</span>
        <span>${message}</span>
    `;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 4000);
}

/**
 * Format date
 */
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Format relative time
 */
function formatRelativeTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;

    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;

    return formatDate(dateString);
}

/**
 * Get status badge HTML
 */
function getStatusBadge(status) {
    const statusLower = status.toLowerCase().replace('_', '-');
    return `<span class="status-badge status-${statusLower}">${status.replace('_', ' ')}</span>`;
}

/**
 * Get priority badge HTML
 */
function getPriorityBadge(priority) {
    const priorityLower = priority.toLowerCase();
    return `<span class="priority-${priorityLower}">${priority}</span>`;
}

/**
 * Get category icon
 */
function getCategoryIcon(icon) {
    const iconMap = {
        'road': 'directions_car',
        'water': 'water_drop',
        'trash': 'delete',
        'lightbulb': 'lightbulb',
        'warning': 'warning',
        'traffic-light': 'traffic',
        'building': 'apartment',
        'question': 'help'
    };
    return iconMap[icon] || 'help';
}

/**
 * Create issue marker for Leaflet map
 */
function createIssueMarker(issue, map, onClick) {
    const color = issue.categoryColor || '#6366f1';

    const markerHtml = `
        <div class="custom-marker" style="background-color: ${color};">
            <span class="material-icons text-white text-lg">${getCategoryIcon(issue.categoryIcon)}</span>
        </div>
    `;

    const icon = L.divIcon({
        className: 'custom-div-icon',
        html: markerHtml,
        iconSize: [40, 40],
        iconAnchor: [20, 40],
        popupAnchor: [0, -40]
    });

    const marker = L.marker([issue.latitude, issue.longitude], { icon })
        .addTo(map);

    const popupContent = `
        <div class="issue-popup">
            <h4 class="text-white font-semibold">${issue.title}</h4>
            <p>${issue.categoryName}</p>
            <div class="mt-2">${getStatusBadge(issue.status)}</div>
        </div>
    `;

    marker.bindPopup(popupContent);

    if (onClick) {
        marker.on('click', () => onClick(issue));
    }

    return marker;
}

/**
 * Debounce function
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Require authentication for a page
 */
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

/**
 * Require admin role for a page
 */
function requireAdmin() {
    if (!requireAuth()) return false;
    if (!isAdmin()) {
        window.location.href = '/citizen-dashboard.html';
        return false;
    }
    return true;
}

// Export for use in other files
window.GeoReport = {
    getAuthToken,
    getCurrentUser,
    isAuthenticated,
    isAdmin,
    logout,
    apiRequest,
    showToast,
    formatDate,
    formatRelativeTime,
    getStatusBadge,
    getPriorityBadge,
    getCategoryIcon,
    createIssueMarker,
    debounce,
    requireAuth,
    requireAdmin
};
