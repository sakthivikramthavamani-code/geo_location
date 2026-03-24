@echo off
setlocal EnableDelayedExpansion

title GeoReport Application - Auto Launcher

echo.
echo ============================================
echo   GeoReport Application - Auto Launcher
echo ============================================
echo.

REM Change to script directory
cd /d "%~dp0"

REM ============================================
REM 1. Check and Setup Java
REM ============================================
echo [1/4] Checking Java installation...

if not defined JAVA_HOME (
    REM Try to find Java automatically
    for /d %%i in ("C:\Program Files\Java\jdk*") do (
        if exist "%%i\bin\java.exe" (
            set "JAVA_HOME=%%i"
            goto :java_found
        )
    )
    for /d %%i in ("C:\Program Files (x86)\Java\jdk*") do (
        if exist "%%i\bin\java.exe" (
            set "JAVA_HOME=%%i"
            goto :java_found
        )
    )
    REM Try common JDK locations
    if exist "C:\Program Files\Java\jdk-20\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-20"
    ) else if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    ) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    )
)

:java_found
if not defined JAVA_HOME (
    echo [ERROR] Java not found! Please install Java 17+ and set JAVA_HOME.
    echo.
    pause
    exit /b 1
)

REM Verify Java version
"%JAVA_HOME%\bin\java.exe" -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java executable not found at %JAVA_HOME%\bin\java.exe
    pause
    exit /b 1
)

echo [OK] Java found at: %JAVA_HOME%

REM ============================================
REM 2. Check MySQL Service
REM ============================================
echo.
echo [2/4] Checking MySQL service...

sc query MySQL80 | find "RUNNING" >nul 2>&1
if errorlevel 1 (
    echo [WARNING] MySQL service may not be running.
    echo           Attempting to start MySQL service...
    net start MySQL80 >nul 2>&1
    if errorlevel 1 (
        echo [WARNING] Could not start MySQL automatically.
        echo           Please ensure MySQL is running on localhost:3306
        echo           You can start it manually from Services (services.msc)
        timeout /t 3 >nul
    ) else (
        echo [OK] MySQL service started.
        timeout /t 2 >nul
    )
) else (
    echo [OK] MySQL service is running.
)

REM ============================================
REM 3. Use Maven Wrapper (mvnw.cmd)
REM ============================================
echo.
echo [3/4] Checking Maven wrapper...

if not exist "mvnw.cmd" (
    echo [ERROR] Maven wrapper (mvnw.cmd) not found!
    echo         Please ensure you're in the project root directory.
    pause
    exit /b 1
)

echo [OK] Maven wrapper found.

REM ============================================
REM 4. Start Application
REM ============================================
echo.
echo [4/4] Starting GeoReport Application...
echo.
echo ============================================
echo   Configuration:
echo   - Java: %JAVA_HOME%
echo   - Database: MySQL (localhost:3306/georeport_db)
echo   - Port: 8080
echo ============================================
echo.
echo Starting application... (This may take a moment)
echo.
echo Once started, access at: http://localhost:8080
echo Press Ctrl+C to stop the application.
echo.

REM Set PATH to include Java
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Run the application using Maven wrapper
call mvnw.cmd spring-boot:run

REM If we get here, the application has stopped
echo.
echo ============================================
echo   Application stopped.
echo ============================================
pause
