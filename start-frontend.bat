@echo off
setlocal EnableExtensions
cd /d "%~dp0frontend"
title PoultryShare Frontend

echo [Frontend] Killing existing process on port 5173...
call :KillPort 5173
timeout /t 1 /nobreak >nul

echo [Frontend] Vite dev server starting...
echo Working dir: %CD%
echo.
if not exist "node_modules\" (
  echo node_modules missing - running npm install...
  call npm install
  if errorlevel 1 (
    echo npm install failed
    pause
    exit /b 1
  )
)
call npm run dev
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
