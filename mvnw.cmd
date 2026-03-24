@REM ----------------------------------------------------------------------------
@REM Maven Wrapper - Windows Batch Script (Fixed)
@REM Downloads Maven binary and runs it directly
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\maven\apache-maven-%MAVEN_VERSION%

@REM Find Java
if defined JAVA_HOME goto :findJavaFromJavaHome
set JAVA=java
goto :checkJava

:findJavaFromJavaHome
set JAVA="%JAVA_HOME%\bin\java.exe"

:checkJava
%JAVA% -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo Error: JAVA_HOME is not set or java is not in PATH
    echo Please set JAVA_HOME to your JDK installation directory
    echo.
    exit /b 1
)

@REM Check if Maven is already downloaded
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto :runMvn

echo Maven not found. Downloading Maven %MAVEN_VERSION%...
mkdir "%MAVEN_PROJECTBASEDIR%.mvn\maven" 2>nul

set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set MAVEN_ZIP=%MAVEN_PROJECTBASEDIR%.mvn\maven\maven.zip

echo Downloading from %MAVEN_URL%...
powershell -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%' -UseBasicParsing } catch { exit 1 }"

if not exist "%MAVEN_ZIP%" (
    echo Download failed. Trying alternate source...
    curl -L -o "%MAVEN_ZIP%" "https://dlcdn.apache.org/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"
)

if not exist "%MAVEN_ZIP%" (
    echo Failed to download Maven
    echo Please download manually from: https://maven.apache.org/download.cgi
    exit /b 1
)

echo Extracting Maven...
powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_PROJECTBASEDIR%.mvn\maven' -Force"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Failed to extract Maven
    exit /b 1
)

del "%MAVEN_ZIP%" 2>nul
echo Maven %MAVEN_VERSION% installed successfully!

:runMvn
@REM Run Maven with provided arguments
call "%MAVEN_HOME%\bin\mvn.cmd" %*
