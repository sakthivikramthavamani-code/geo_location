@echo off
REM Create desktop shortcut for GeoReport Application

echo Creating desktop shortcut...

set SCRIPT_DIR=%~dp0
set SHORTCUT_PATH=%USERPROFILE%\Desktop\GeoReport.lnk

powershell -Command "$WshShell = New-Object -ComObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%SHORTCUT_PATH%'); $Shortcut.TargetPath = '%SCRIPT_DIR%run.bat'; $Shortcut.WorkingDirectory = '%SCRIPT_DIR%'; $Shortcut.Description = 'GeoReport Application Launcher'; $Shortcut.IconLocation = 'shell32.dll,137'; $Shortcut.Save()"

if exist "%SHORTCUT_PATH%" (
    echo [OK] Desktop shortcut created successfully!
    echo       You can now double-click "GeoReport" on your desktop to start the application.
) else (
    echo [WARNING] Could not create shortcut. You can still run "run.bat" directly.
)

echo.
pause

