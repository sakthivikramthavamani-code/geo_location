# Auto-Run Guide - GeoReport Application

## Quick Start

### Option 1: Double-Click to Run (Easiest)
Simply double-click **`run.bat`** in the project folder. The script will:
- ✅ Automatically detect Java installation
- ✅ Check and start MySQL service if needed
- ✅ Launch the application automatically
- ✅ Open on http://localhost:8080

### Option 2: PowerShell (Recommended for PowerShell users)
```powershell
.\run.ps1
```

or

```powershell
.\start.ps1
```

### Option 3: Create Desktop Shortcut
1. Run `create-shortcut.bat` once
2. A shortcut "GeoReport" will appear on your desktop
3. Double-click the shortcut anytime to start the application

### Option 4: Command Prompt
```bash
run.bat
```

or

```bash
start.bat
```

## What the Auto-Run Script Does

The `run.bat` script automatically:

1. **Detects Java Installation**
   - Searches common Java installation paths
   - Supports JDK 17, 20, 21
   - Sets JAVA_HOME automatically if not set

2. **Checks MySQL Service**
   - Verifies MySQL is running
   - Attempts to start MySQL service if stopped
   - Shows warning if MySQL cannot be started

3. **Uses Maven Wrapper**
   - Uses the included `mvnw.cmd` (no Maven installation needed)
   - Downloads dependencies automatically on first run

4. **Starts Application**
   - Compiles and runs the Spring Boot application
   - Shows startup progress
   - Displays access URL when ready

## Application Startup

Once started, you'll see:
```
==============================================
  Geo-Based Issue Reporting System Started!
  Access at: http://localhost:8080
==============================================
  Database: MySQL (georeport_db)
  Mode: Production
==============================================
```

## Access Points

- **Homepage**: http://localhost:8080
- **Login**: http://localhost:8080/login.html
- **Register**: http://localhost:8080/register.html
- **Admin Dashboard**: http://localhost:8080/admin-dashboard.html
- **Citizen Dashboard**: http://localhost:8080/citizen-dashboard.html

## Stopping the Application

Press **Ctrl+C** in the console window to stop the application.

## Troubleshooting

### Java Not Found
- Install Java 17+ from [Adoptium](https://adoptium.net/)
- Or set JAVA_HOME environment variable manually

### MySQL Not Running
- Open Services (Win+R → `services.msc`)
- Find "MySQL80" service
- Right-click → Start

### Port 8080 Already in Use
- Stop other applications using port 8080
- Or change port in `src/main/resources/application.yml`

### Database Connection Error
- Verify MySQL root password is "root" (or update in application.yml)
- Ensure MySQL service is running
- Check MySQL is listening on port 3306

## Files

- **`run.bat`** - Main auto-launcher script (Windows CMD)
- **`run.ps1`** - Main auto-launcher script (PowerShell)
- **`start.bat`** - Quick start alias (CMD)
- **`start.ps1`** - Quick start alias (PowerShell)
- **`create-shortcut.bat`** - Creates desktop shortcut
- **`mvnw.cmd`** - Maven wrapper (included, no installation needed)

---

**That's it!** Just double-click `run.bat` and the application will start automatically.

