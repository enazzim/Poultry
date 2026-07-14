@echo off
setlocal EnableExtensions
cd /d "%~dp0backend"
title PoultryShare Backend

echo [Backend] Killing existing process on port 8080...
call :KillPort 8080
timeout /t 1 /nobreak >nul

echo [Backend] Spring Boot starting...
echo Working dir: %CD%
echo.
call gradlew.bat bootRun
pause
exit /b 0

:KillPort
set "PORT=%~1"
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":%PORT% " ^| findstr /I "LISTENING"') do (
  if not "%%P"=="0" (
    echo   taskkill PID %%P (port %PORT%)
    taskkill /F /PID %%P >nul 2>&1
  )
)
exit /b 0
