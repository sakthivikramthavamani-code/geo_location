# MySQL Setup Guide for GeoReport Application

## Quick Setup Steps

### 1. Verify MySQL Connection in MySQL Workbench

Since you have MySQL Workbench open, test the connection:

**Option A: Run the test script**
- Open `test-connection.sql` in MySQL Workbench
- Execute it (Ctrl+Shift+Enter or click the lightning bolt)
- If it runs successfully, your MySQL is ready!

**Option B: Manual verification**
```sql
-- Create database (if not exists)
CREATE DATABASE IF NOT EXISTS georeport_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify root user has access
USE georeport_db;
SELECT DATABASE(); -- Should return 'georeport_db'
```

### 2. Verify Root Password

The application is configured with:
- **Username**: `root`
- **Password**: `root`
- **Database**: `georeport_db` (will be auto-created)

If your MySQL root password is different, update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    password: your_actual_password
```

### 3. Start the Application

**Using the batch file:**
```bash
run.bat
```

**Or using Maven wrapper:**
```bash
.\mvnw.cmd spring-boot:run
```

### 4. What Happens on First Run

The application will automatically:
- ✅ Connect to MySQL
- ✅ Create `georeport_db` database (if not exists)
- ✅ Create all required tables (via Hibernate)
- ✅ Initialize roles (ROLE_CITIZEN, ROLE_ADMIN)
- ✅ Create default issue categories
- ✅ Start on http://localhost:8080

### 5. Verify Application Started

Look for this message in the console:
```
==============================================
  Geo-Based Issue Reporting System Started!
  Access at: http://localhost:8080
==============================================
  Database: MySQL (georeport_db)
  Mode: Production
==============================================
```

### 6. Access the Application

- **Homepage**: http://localhost:8080
- **Login**: http://localhost:8080/login.html
- **Register**: http://localhost:8080/register.html
- **Admin Dashboard**: http://localhost:8080/admin-dashboard.html (after admin login)
- **Citizen Dashboard**: http://localhost:8080/citizen-dashboard.html (after citizen login)

## Troubleshooting

### "Access denied for user 'root'@'localhost'"

**Solution 1**: Verify root password
```sql
-- In MySQL Workbench, try connecting with password 'root'
-- If it fails, your password is different
```

**Solution 2**: Update application.yml with correct password

**Solution 3**: Create a dedicated user (recommended for production)
```sql
CREATE USER 'georeport_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON georeport_db.* TO 'georeport_user'@'localhost';
FLUSH PRIVILEGES;
```

Then update `application.yml`:
```yaml
spring:
  datasource:
    username: georeport_user
    password: your_secure_password
```

### "Unknown database 'georeport_db'"

The application should auto-create it, but if it doesn't:
```sql
CREATE DATABASE georeport_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### MySQL Service Not Running

**Windows:**
```bash
# Check if MySQL is running
sc query MySQL80

# Start MySQL service
net start MySQL80
```

**Or use Services:**
- Press `Win + R`, type `services.msc`
- Find "MySQL80" service
- Right-click → Start

## Database Schema

The application uses **Hibernate auto-DDL** (`ddl-auto: update`), which means:
- Tables are created automatically from JPA entities
- Schema changes are applied automatically
- **No manual SQL scripts needed!**

## First User Registration

After the application starts:
1. Go to http://localhost:8080/register.html
2. Register a new account (will be assigned ROLE_CITIZEN by default)
3. Login and start reporting issues!

**Note:** To create an admin user, register normally then update the user's role in the database:
```sql
USE georeport_db;
-- Get the user ID from the users table
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'your_admin_email@example.com' AND r.name = 'ROLE_ADMIN';
```

## Notes

- **Production Ready**: No test data or demo users are created
- The `schema.sql` file in `src/main/resources/db/` is for reference only (PostgreSQL format)
- For MySQL, Hibernate handles all table creation automatically via `ddl-auto: update`
- All data initialization (roles, categories) happens via `DataInitializer.java` on startup
- Users must register through the application - no default accounts are created

---

**Ready to go?** Just run `run.bat` and the application will handle everything!

