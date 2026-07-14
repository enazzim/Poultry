@echo off
setlocal
cd /d "%~dp0"
title PoultryShare Start All
echo PoultryShare - starting Backend + Frontend
echo.
echo Opening Backend and Frontend in new windows...
echo.

start "PoultryShare Backend" "%~dp0start-backend.bat"
timeout /t 2 /nobreak >nul
start "PoultryShare Frontend" "%~dp0start-frontend.bat"

echo.
echo Backend and Frontend windows launched.
echo Frontend: http://localhost:5173
echo.
pause
