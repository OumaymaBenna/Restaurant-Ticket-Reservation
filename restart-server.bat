@echo off
echo ========================================
echo Redemarrage du serveur Node.js
echo ========================================
echo.

echo [1/3] Arret de tous les processus Node.js...
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Processus Node.js arretes
) else (
    echo ℹ️  Aucun processus Node.js trouve
)
echo.

echo [2/3] Attente de 2 secondes...
timeout /t 2 /nobreak >nul
echo.

echo [3/3] Demarrage du serveur...
cd server
node server.js



