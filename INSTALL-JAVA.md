# Install Java 17+ for GeoReport Application

## Quick Installation Guide

### Option 1: Download and Install (Recommended)

1. **Download Java 17 or 20:**
   - Visit: https://adoptium.net/
   - Click "Latest LTS Release" (Java 17 or 21)
   - Select:
     - **Operating System**: Windows
     - **Architecture**: x64
     - **Package Type**: JDK
   - Click "Download"

2. **Install:**
   - Run the downloaded installer
   - Follow the installation wizard
   - **Important**: Note the installation path (usually `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`)

3. **Set JAVA_HOME (if not auto-set):**
   - Press `Win + R`, type `sysdm.cpl`, press Enter
   - Go to "Advanced" tab → "Environment Variables"
   - Under "System variables", click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot` (your actual path)
   - Click OK, then OK again

4. **Verify Installation:**
   ```powershell
   java -version
   ```
   Should show version 17 or higher.

### Option 2: Use Chocolatey (If Installed)

```powershell
choco install temurin17
```

### Option 3: Manual JAVA_HOME Setup

If you already have Java 17+ installed but it's not detected:

1. Find your Java installation:
   ```powershell
   Get-ChildItem "C:\Program Files\Java" -Recurse -Filter "java.exe" | Select-Object FullName
   ```

2. Set JAVA_HOME to the parent directory (not the bin folder):
   - If Java is at: `C:\Program Files\Java\jdk-17\bin\java.exe`
   - Set JAVA_HOME to: `C:\Program Files\Java\jdk-17`

3. Add to PATH:
   - Add `%JAVA_HOME%\bin` to your PATH environment variable

## After Installation

Run the application again:
```powershell
.\run.ps1
```

The script will now detect Java 17+ automatically!

