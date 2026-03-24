-- =====================================================
-- Geo-Based Public Issue Reporting System
-- Database Schema for PostgreSQL with PostGIS
-- =====================================================

-- Enable PostGIS extension (run as superuser if needed)
CREATE EXTENSION IF NOT EXISTS postgis;

-- =====================================================
-- ROLES TABLE
-- Stores application roles (CITIZEN, ADMIN)
-- =====================================================
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ROLE_CITIZEN', 'Regular citizen who can report and track issues'),
    ('ROLE_ADMIN', 'Municipal administrator who manages and resolves issues')
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- USERS TABLE
-- Stores all user accounts (citizens and admins)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    ward VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP WITH TIME ZONE
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- =====================================================
-- USER_ROLES TABLE
-- Many-to-many relationship between users and roles
-- =====================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- =====================================================
-- ISSUE_CATEGORIES TABLE
-- Predefined categories for public issues
-- =====================================================
CREATE TABLE IF NOT EXISTS issue_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(50),
    color VARCHAR(20),
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default categories
INSERT INTO issue_categories (name, description, icon, color, priority) VALUES 
    ('Road Damage', 'Potholes, cracks, broken roads, and pavement issues', 'road', '#e74c3c', 1),
    ('Water Leakage', 'Water pipe leaks, water logging, and drainage issues', 'water', '#3498db', 2),
    ('Garbage Overflow', 'Overflowing dustbins, garbage dumping, and waste issues', 'trash', '#27ae60', 3),
    ('Street Light', 'Broken or non-functional street lights', 'lightbulb', '#f39c12', 4),
    ('Sanitation', 'Open drains, sewage overflow, and sanitation issues', 'warning', '#9b59b6', 5),
    ('Traffic', 'Traffic signal issues, signboard damage, road markings', 'traffic-light', '#e67e22', 6),
    ('Public Property', 'Damaged public benches, bus stops, public toilets', 'building', '#1abc9c', 7),
    ('Other', 'Any other civic issues not listed above', 'question', '#95a5a6', 10)
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- ISSUES TABLE
-- Main table storing all reported issues with geo-location
-- =====================================================
CREATE TABLE IF NOT EXISTS issues (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    address TEXT,
    ward VARCHAR(50),
    landmark VARCHAR(200),
    
    -- Status: SUBMITTED, IN_PROGRESS, RESOLVED, REJECTED, CLOSED
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    
    -- Foreign keys
    category_id INTEGER NOT NULL REFERENCES issue_categories(id),
    reporter_id INTEGER NOT NULL REFERENCES users(id),
    assigned_to INTEGER REFERENCES users(id),
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    
    -- Resolution details
    resolution_notes TEXT,
    rejection_reason TEXT
);

-- Create spatial index for fast geo-queries
CREATE INDEX IF NOT EXISTS idx_issues_location ON issues USING GIST(location);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_issues_status ON issues(status);
CREATE INDEX IF NOT EXISTS idx_issues_category ON issues(category_id);
CREATE INDEX IF NOT EXISTS idx_issues_reporter ON issues(reporter_id);
CREATE INDEX IF NOT EXISTS idx_issues_ward ON issues(ward);
CREATE INDEX IF NOT EXISTS idx_issues_created_at ON issues(created_at DESC);

-- =====================================================
-- ISSUE_IMAGES TABLE
-- Stores image metadata for issue proof
-- =====================================================
CREATE TABLE IF NOT EXISTS issue_images (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    file_path TEXT NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_issue_images_issue ON issue_images(issue_id);

-- =====================================================
-- ISSUE_STATUS_HISTORY TABLE
-- Audit trail for status changes
-- =====================================================
CREATE TABLE IF NOT EXISTS issue_status_history (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by INTEGER NOT NULL REFERENCES users(id),
    change_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_status_history_issue ON issue_status_history(issue_id);
CREATE INDEX IF NOT EXISTS idx_status_history_created ON issue_status_history(created_at DESC);

-- =====================================================
-- COMMENTS TABLE
-- Comments on issues by citizens and admins
-- =====================================================
CREATE TABLE IF NOT EXISTS issue_comments (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    comment TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comments_issue ON issue_comments(issue_id);

-- =====================================================
-- NOTIFICATIONS TABLE
-- Stores notifications for real-time updates
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    issue_id INTEGER REFERENCES issues(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- =====================================================
-- UTILITY FUNCTIONS
-- =====================================================

-- Function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to auto-update updated_at on users table
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to auto-update updated_at on issues table
DROP TRIGGER IF EXISTS update_issues_updated_at ON issues;
CREATE TRIGGER update_issues_updated_at
    BEFORE UPDATE ON issues
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- VIEWS FOR COMMON QUERIES
-- =====================================================

-- View for issues with category and reporter info
CREATE OR REPLACE VIEW v_issues_detail AS
SELECT 
    i.id,
    i.title,
    i.description,
    ST_X(i.location::geometry) as longitude,
    ST_Y(i.location::geometry) as latitude,
    i.address,
    i.ward,
    i.status,
    i.priority,
    i.created_at,
    i.updated_at,
    i.resolved_at,
    c.name as category_name,
    c.icon as category_icon,
    c.color as category_color,
    u.full_name as reporter_name,
    u.email as reporter_email,
    a.full_name as assigned_to_name
FROM issues i
JOIN issue_categories c ON i.category_id = c.id
JOIN users u ON i.reporter_id = u.id
LEFT JOIN users a ON i.assigned_to = a.id;

-- =====================================================
-- Database schema complete
-- Users and data are created through the application
-- =====================================================

COMMIT;
