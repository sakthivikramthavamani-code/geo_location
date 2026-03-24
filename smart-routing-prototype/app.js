// ============================================================
//  SmartCity Flow - Department-Based Smart Routing System
//  All categories sourced from DataInitializer.java
// ============================================================

const STATE_KEY = 'smart_city_complaints_v2';
let complaints = JSON.parse(localStorage.getItem(STATE_KEY)) || [];

// ============================================================
//  SMART ROUTING MAP
//  Maps each issue category to the correct department.
//  Source: DataInitializer.java -> createCategories()
// ============================================================
const ROUTING_MAP = {
    'Road Damage':       { dept: 'Road Department',       color: '#e74c3c', icon: '🛣️' },
    'Water Leakage':     { dept: 'Water Department',      color: '#3498db', icon: '💧' },
    'Garbage Overflow':  { dept: 'Sanitation Department', color: '#27ae60', icon: '🗑️' },
    'Street Light':      { dept: 'Electricity Department',color: '#f39c12', icon: '💡' },
    'Sanitation':        { dept: 'Sanitation Department', color: '#9b59b6', icon: '🚿' },
    'Traffic':           { dept: 'Traffic Department',    color: '#e67e22', icon: '🚦' },
    'Public Property':   { dept: 'Public Works Department',color: '#1abc9c',icon: '🏛️' },
    'Other':             { dept: 'General Administration', color: '#95a5a6', icon: '📋' }
};

// Department → Dashboard grid/stats IDs
const DEPT_GRID_MAP = {
    'Road Department':        { gridId: 'grid-road',        statsId: 'stats-road' },
    'Water Department':       { gridId: 'grid-water',       statsId: 'stats-water' },
    'Sanitation Department':  { gridId: 'grid-sanitation',  statsId: 'stats-sanitation' },
    'Electricity Department': { gridId: 'grid-electricity', statsId: 'stats-electricity' },
    'Traffic Department':     { gridId: 'grid-traffic',     statsId: 'stats-traffic' },
    'Public Works Department':{ gridId: 'grid-publicworks', statsId: 'stats-publicworks' },
    'General Administration': { gridId: 'grid-general',     statsId: 'stats-general' }
};

// ============================================================
//  DOM References
// ============================================================
const navButtons   = document.querySelectorAll('.nav-btn');
const viewSections = document.querySelectorAll('.view-section');
const filterSelects = document.querySelectorAll('.status-filter');
const toastContainer = document.getElementById('toast-container');
const issueTypeSelect = document.getElementById('issueType');

// ============================================================
//  1. NAVIGATION
// ============================================================
function switchView(viewId) {
    navButtons.forEach(btn => btn.classList.toggle('active', btn.dataset.view === viewId));
    viewSections.forEach(section => {
        const isActive = section.id === viewId;
        section.classList.toggle('active', isActive);
        if (isActive && section.id.startsWith('dashboard-')) {
            const dept = section.dataset.department;
            const { gridId, statsId } = DEPT_GRID_MAP[dept];
            const filterVal = section.querySelector('.status-filter').value;
            renderDashboard(dept, gridId, statsId, filterVal);
        }
    });
}

navButtons.forEach(btn => {
    btn.addEventListener('click', () => switchView(btn.dataset.view));
});

// ============================================================
//  2. LIVE ROUTING PREVIEW (shows dept on issue type change)
// ============================================================
issueTypeSelect.addEventListener('change', () => {
    const val = issueTypeSelect.value;
    const badge = document.getElementById('routingBadge');
    if (val && ROUTING_MAP[val]) {
        const { dept, color, icon } = ROUTING_MAP[val];
        badge.innerHTML = `<span style="background:${color}20; border:1px solid ${color}; color:${color}; padding:4px 10px; border-radius:20px; font-size:0.82rem; font-weight:600; margin-top: 6px; display: inline-block;">
            ${icon} Will be routed to: <strong>${dept}</strong>
        </span>`;
    } else {
        badge.innerHTML = '';
    }
});

// ============================================================
//  3. COMPLAINT SUBMISSION + AUTO-ROUTING
// ============================================================
document.getElementById('complaintForm').addEventListener('submit', (e) => {
    e.preventDefault();

    const issueType  = document.getElementById('issueType').value;
    const priority   = document.getElementById('priority').value;
    const location   = document.getElementById('location').value;
    const description = document.getElementById('description').value;

    // --- SMART ROUTING LOGIC ---
    const routeInfo = ROUTING_MAP[issueType] || { dept: 'General Administration', color: '#95a5a6', icon: '📋' };
    const assignedDepartment = routeInfo.dept;

    const newComplaint = {
        id: 'CMP-' + Date.now(),
        issueType,
        priority,
        location,
        description,
        department: assignedDepartment,
        status: 'Pending',
        timestamp: new Date().toISOString()
    };

    complaints.unshift(newComplaint);
    saveState();
    updateSidebarStats();

    document.getElementById('complaintForm').reset();
    document.getElementById('routingBadge').innerHTML = '';

    showToast(`✅ Routed to <strong>${assignedDepartment}</strong>`, 'success');
});

// ============================================================
//  4. RENDER DASHBOARD CARDS (department-isolated)
// ============================================================
function renderDashboard(deptName, gridId, statsId, statusFilter = 'All') {
    const grid = document.getElementById(gridId);
    grid.innerHTML = '';

    // ONLY show complaints for THIS department
    let deptComplaints = complaints.filter(c => c.department === deptName);
    renderStats(statsId, deptComplaints);

    if (statusFilter !== 'All') {
        deptComplaints = deptComplaints.filter(c => c.status === statusFilter);
    }

    if (deptComplaints.length === 0) {
        grid.innerHTML = `<p class="empty-state">No complaints ${statusFilter !== 'All' ? `with status "<strong>${statusFilter}</strong>"` : ''} for this department.</p>`;
        return;
    }

    deptComplaints.forEach(complaint => {
        const card = document.createElement('div');
        card.className = 'complaint-card';
        const statusClass = complaint.status === 'In Progress' ? 'status-Progress' : `status-${complaint.status}`;
        const dateStr = new Date(complaint.timestamp).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' });
        const routeColor = (ROUTING_MAP[complaint.issueType] || {}).color || '#95a5a6';

        card.style.borderTop = `3px solid ${routeColor}`;
        card.innerHTML = `
            <div class="card-header">
                <div>
                    <span class="status-badge ${statusClass}">${complaint.status}</span>
                    <div class="card-title">${complaint.issueType}</div>
                </div>
                <span class="badge priority-${complaint.priority}">${complaint.priority}</span>
            </div>
            <div class="card-meta">📍 ${escapeHTML(complaint.location)} &nbsp;·&nbsp; 📅 ${dateStr}</div>
            <div class="card-desc">${escapeHTML(complaint.description)}</div>
            <div class="card-actions">
                <span class="complaint-id">ID: ${complaint.id}</span>
                <select class="status-updater" data-id="${complaint.id}">
                    <option value="Pending"     ${complaint.status === 'Pending'     ? 'selected' : ''}>⏳ Pending</option>
                    <option value="In Progress" ${complaint.status === 'In Progress' ? 'selected' : ''}>🔄 In Progress</option>
                    <option value="Completed"   ${complaint.status === 'Completed'   ? 'selected' : ''}>✅ Completed</option>
                </select>
            </div>
        `;
        grid.appendChild(card);
    });

    // Status update listeners
    grid.querySelectorAll('.status-updater').forEach(sel => {
        sel.addEventListener('change', (e) => {
            updateStatus(e.target.dataset.id, e.target.value, deptName, gridId, statsId);
        });
    });
}

function renderStats(statsId, deptComplaints) {
    const el = document.getElementById(statsId);
    if (!el) return;
    const pending    = deptComplaints.filter(c => c.status === 'Pending').length;
    const inProgress = deptComplaints.filter(c => c.status === 'In Progress').length;
    const completed  = deptComplaints.filter(c => c.status === 'Completed').length;
    el.innerHTML = `
        <span class="stat-pill pending">⏳ ${pending} Pending</span>
        <span class="stat-pill progress">🔄 ${inProgress} In Progress</span>
        <span class="stat-pill done">✅ ${completed} Completed</span>
    `;
}

// ============================================================
//  5. SIDEBAR STATS (total per dept)
// ============================================================
function updateSidebarStats() {
    const el = document.getElementById('sidebarStats');
    if (!el) return;
    const total = complaints.length;
    const pending = complaints.filter(c => c.status === 'Pending').length;
    el.innerHTML = `
        <div class="sidebar-stat-row"><span>Total Complaints</span><strong>${total}</strong></div>
        <div class="sidebar-stat-row"><span>Pending</span><strong style="color:#fbbf24">${pending}</strong></div>
        <div class="sidebar-stat-row"><span>Resolved</span><strong style="color:#34d399">${total - pending}</strong></div>
    `;
}

// ============================================================
//  6. FILTER CHANGE LISTENERS
// ============================================================
filterSelects.forEach(select => {
    select.addEventListener('change', (e) => {
        const section = e.target.closest('.view-section');
        const dept = section.dataset.department;
        const { gridId, statsId } = DEPT_GRID_MAP[dept];
        renderDashboard(dept, gridId, statsId, e.target.value);
    });
});

// ============================================================
//  7. STATUS UPDATE
// ============================================================
function updateStatus(id, newStatus, currentDept, currentGridId, currentStatsId) {
    const idx = complaints.findIndex(c => c.id === id);
    if (idx > -1) {
        complaints[idx].status = newStatus;
        saveState();
        updateSidebarStats();
        showToast(`Status updated to <strong>${newStatus}</strong>`, 'info');
        const section = document.getElementById(currentGridId).closest('.view-section');
        const filterVal = section.querySelector('.status-filter').value;
        renderDashboard(currentDept, currentGridId, currentStatsId, filterVal);
    }
}

// ============================================================
//  8. UTILITIES
// ============================================================
function saveState() {
    localStorage.setItem(STATE_KEY, JSON.stringify(complaints));
}

function escapeHTML(str) {
    return String(str).replace(/[&<>'"]/g, tag => ({
        '&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'
    }[tag]));
}

function showToast(html, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = html;
    toastContainer.appendChild(toast);
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-in forwards';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ============================================================
//  9. SEED DATA (one sample per department if fresh)
// ============================================================
if (complaints.length === 0) {
    const seeds = [
        { issueType: 'Road Damage',      priority: 'High',   location: 'Main St near City Hall',    description: 'Large pothole causing vehicle damage.' },
        { issueType: 'Water Leakage',    priority: 'High',   location: 'Gandhi Nagar Block 3',       description: 'Pipe burst — water flooding the road.' },
        { issueType: 'Garbage Overflow', priority: 'Medium', location: 'Market Area, East Side',     description: 'Bins overflowing, stray dogs accessing waste.' },
        { issueType: 'Street Light',     priority: 'Medium', location: 'School Road Sector 5',       description: 'Three lights not working since last week.' },
        { issueType: 'Sanitation',       priority: 'High',   location: 'Bus Stand Toilet Block',     description: 'Sewage overflow, extremely unhygienic.' },
        { issueType: 'Traffic',          priority: 'Low',    location: 'Junction near Mall Road',    description: 'Signal stuck on red, causing traffic jam.' },
        { issueType: 'Public Property',  priority: 'Low',    location: 'Central Park, Entry Gate',  description: 'Benches broken, gate lock damaged.' },
        { issueType: 'Other',            priority: 'Low',    location: 'Ward 7 Office',              description: 'Stray wire hanging dangerously from pole.' }
    ];
    seeds.forEach(s => {
        const routeInfo = ROUTING_MAP[s.issueType] || { dept: 'General Administration' };
        complaints.push({ id: 'CMP-' + Date.now() + Math.random(), ...s, department: routeInfo.dept, status: 'Pending', timestamp: new Date().toISOString() });
    });
    saveState();
}

// Init sidebar stats
updateSidebarStats();
