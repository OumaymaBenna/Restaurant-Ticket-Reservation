@echo off
echo ========================================
echo   Redemarrage du Serveur Node.js
echo ========================================
echo.
echo Arret du serveur en cours (si actif)...
taskkill /F /IM node.exe 2>nul
timeout /t 2 /nobreak >nul
echo.
echo Demarrage du serveur...
cd /d %~dp0
npm start
pause



