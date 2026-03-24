# GeoReport Application - PowerShell Auto Launcher
# Run with: .\run.ps1

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  GeoReport Application - Auto Launcher" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Change to script directory
Set-Location $PSScriptRoot

# ============================================
# 1. Check and Setup Java
# ============================================
Write-Host "[1/4] Checking Java installation..." -ForegroundColor Yellow

$javaHome = $env:JAVA_HOME
$javaExe = $null
$javaVersion = $null

# Function to check Java version
function Test-JavaVersion {
    param([string]$javaPath)
    try {
        $versionOutput = & "$javaPath" -version 2>&1 | Out-String
        if ($versionOutput -match '"(\d+)\.(\d+)') {
            $majorVersion = [int]$matches[1]
            $minorVersion = [int]$matches[2]
            if ($majorVersion -eq 1) {
                # Java 8 format: "1.8.0"
                return $minorVersion
            } else {
                # Java 9+ format: "17.0.1" or "20.0.1"
                return $majorVersion
            }
        }
    } catch {
        return $null
    }
    return $null
}

if ($javaHome) {
    # Normalize path (remove trailing backslash, handle double bin)
    $javaHome = $javaHome.TrimEnd('\')
    if ($javaHome.EndsWith('\bin')) {
        $javaHome = $javaHome.Substring(0, $javaHome.Length - 4)
    }
    $javaExe = Join-Path $javaHome "bin\java.exe"
    if (Test-Path $javaExe) {
        $javaVersion = Test-JavaVersion -javaPath $javaExe
        if ($javaVersion -and $javaVersion -ge 17) {
            Write-Host "[OK] Java $javaVersion found at: $javaHome" -ForegroundColor Green
            $env:JAVA_HOME = $javaHome
            $env:PATH = "$javaHome\bin;$env:PATH"
        } else {
            Write-Host "[WARNING] JAVA_HOME points to Java $javaVersion (need 17+), searching for newer version..." -ForegroundColor Yellow
            $javaHome = $null
        }
    }
}

if (-not $javaHome) {
    # Try to find Java automatically - check multiple locations
    $searchPaths = @(
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Java",
        "C:\Program Files (x86)\Java",
        "C:\Program Files\Microsoft",
        "C:\Program Files\Amazon Corretto"
    )
    
    $foundJava = $null
    $foundVersion = 0
    
    foreach ($basePath in $searchPaths) {
        if (Test-Path $basePath) {
            # Look for JDK first (preferred) - search recursively
            $jdkDirs = Get-ChildItem -Path $basePath -Directory -Recurse -Filter "jdk*" -ErrorAction SilentlyContinue | 
                       Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
                       Sort-Object Name -Descending
            
            foreach ($jdkDir in $jdkDirs) {
                $testJavaExe = Join-Path $jdkDir.FullName "bin\java.exe"
                if (Test-Path $testJavaExe) {
                    $version = Test-JavaVersion -javaPath $testJavaExe
                    if ($version -and $version -ge 17 -and $version -gt $foundVersion) {
                        $foundJava = $jdkDir.FullName
                        $foundVersion = $version
                    }
                }
            }
        }
    }
    
    if ($foundJava) {
        $javaHome = $foundJava
        $javaExe = Join-Path $javaHome "bin\java.exe"
        Write-Host "[OK] Java $foundVersion found at: $javaHome" -ForegroundColor Green
        $env:JAVA_HOME = $javaHome
        $env:PATH = "$javaHome\bin;$env:PATH"
    } else {
        Write-Host "[ERROR] Java 17+ not found!" -ForegroundColor Red
        Write-Host "        Please install Java 17 or higher from: https://adoptium.net/" -ForegroundColor Yellow
        Write-Host "        Or set JAVA_HOME environment variable to your JDK installation." -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
}

# Final verification
if (-not (Test-Path $javaExe)) {
    Write-Host "[ERROR] Java executable not found at $javaExe" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# ============================================
# 2. Check MySQL Service
# ============================================
Write-Host ""
Write-Host "[2/4] Checking MySQL service..." -ForegroundColor Yellow

$mysqlService = Get-Service -Name "MySQL80" -ErrorAction SilentlyContinue

if (-not $mysqlService) {
    Write-Host "[WARNING] MySQL80 service not found. Checking for other MySQL services..." -ForegroundColor Yellow
    $mysqlServices = Get-Service | Where-Object { $_.Name -like "*MySQL*" }
    if ($mysqlServices) {
        Write-Host "Found MySQL services: $($mysqlServices.Name -join ', ')" -ForegroundColor Yellow
        $mysqlService = $mysqlServices[0]
    }
}

if ($mysqlService) {
    if ($mysqlService.Status -ne "Running") {
        Write-Host "[WARNING] MySQL service is not running. Attempting to start..." -ForegroundColor Yellow
        try {
            Start-Service -Name $mysqlService.Name
            Start-Sleep -Seconds 3
            Write-Host "[OK] MySQL service started." -ForegroundColor Green
        } catch {
            Write-Host "[WARNING] Could not start MySQL automatically: $_" -ForegroundColor Yellow
            Write-Host "          Please ensure MySQL is running on localhost:3306" -ForegroundColor Yellow
            Write-Host "          You can start it manually from Services (services.msc)" -ForegroundColor Yellow
            Start-Sleep -Seconds 3
        }
    } else {
        Write-Host "[OK] MySQL service is running." -ForegroundColor Green
    }
} else {
    Write-Host "[WARNING] MySQL service not found. Please ensure MySQL is installed and running." -ForegroundColor Yellow
    Start-Sleep -Seconds 2
}

# ============================================
# 3. Check Maven Wrapper
# ============================================
Write-Host ""
Write-Host "[3/4] Checking Maven wrapper..." -ForegroundColor Yellow


$mvnwPath = Join-Path $PSScriptRoot "mvnw.cmd"
$localMaven = Join-Path $PSScriptRoot "tools\apache-maven-3.9.6\bin\mvn.cmd"

if (Test-Path $localMaven) {
    Write-Host "[OK] Local Maven found in tools." -ForegroundColor Green
    $mvnwPath = $localMaven
} elseif (-not (Test-Path $mvnwPath)) {
    Write-Host "[ERROR] Maven wrapper (mvnw.cmd) not found!" -ForegroundColor Red
    Write-Host "        Please ensure you're in the project root directory." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
} elseif (Test-Path $mvnwPath) {
    Write-Host "[OK] Maven wrapper found." -ForegroundColor Green
}

# ============================================
# 4. Start Application
# ============================================
Write-Host ""
Write-Host "[4/4] Starting GeoReport Application..." -ForegroundColor Yellow
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Configuration:" -ForegroundColor Cyan
Write-Host "  - Java: $javaHome" -ForegroundColor Cyan
Write-Host "  - Database: MySQL (localhost:3306/georeport_db)" -ForegroundColor Cyan
Write-Host "  - Port: 8080" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Starting application... (This may take a moment)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Once started, access at: http://localhost:8080" -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the application." -ForegroundColor Yellow
Write-Host ""

try {
    # Run the application using Maven wrapper
    & $mvnwPath spring-boot:run
} catch {
    Write-Host ""
    Write-Host "[ERROR] Failed to start application: $_" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Application stopped." -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Read-Host "Press Enter to exit"

