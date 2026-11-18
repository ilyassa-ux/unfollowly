@echo off
setlocal
set GRADLE_VERSION=8.10.2
set BOOT_DIR=%~dp0.gradle-bootstrap
set GRADLE_BIN=%BOOT_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if not exist "%GRADLE_BIN%" (
  if not exist "%BOOT_DIR%" mkdir "%BOOT_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%BOOT_DIR%\gradle.zip'; Expand-Archive -Force '%BOOT_DIR%\gradle.zip' '%BOOT_DIR%'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_BIN%" %*
