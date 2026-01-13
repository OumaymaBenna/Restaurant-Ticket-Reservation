@echo off
echo ========================================
echo   Demarrage du serveur Node.js
echo ========================================
echo.

REM Vérifier si Node.js est installé
where node >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Node.js n'est pas installe ou n'est pas dans le PATH
    echo Veuillez installer Node.js depuis https://nodejs.org/
    pause
    exit /b 1
)

echo [OK] Node.js detecte
node --version
echo.

REM Vérifier si les dépendances sont installées
if not exist "node_modules" (
    echo [INFO] Installation des dependances...
    call npm install
    echo.
)

REM Vérifier si MongoDB est nécessaire
echo [INFO] Verification de MongoDB...
echo Si MongoDB n'est pas demarre, certaines fonctionnalites ne fonctionneront pas.
echo.

REM Démarrer le serveur
echo [INFO] Demarrage du serveur sur http://localhost:3000
echo [INFO] Accessible depuis l'emulateur Android via http://10.0.2.2:3000
echo.
echo Appuyez sur Ctrl+C pour arreter le serveur
echo.

node server.js
