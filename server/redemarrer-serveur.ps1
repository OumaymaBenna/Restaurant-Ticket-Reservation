# Script PowerShell pour redémarrer le serveur Node.js
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Redémarrage du Serveur Node.js" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Étape 1 : Arrêter tous les processus Node.js
Write-Host "Étape 1 : Arrêt des processus Node.js..." -ForegroundColor Yellow
$processes = Get-Process node -ErrorAction SilentlyContinue
if ($processes) {
    Write-Host "   Processus Node.js trouvés : $($processes.Count)" -ForegroundColor Yellow
    Stop-Process -Name node -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "   ✅ Tous les processus Node.js ont été arrêtés" -ForegroundColor Green
} else {
    Write-Host "   ℹ️  Aucun processus Node.js en cours d'exécution" -ForegroundColor Gray
}

# Étape 2 : Vérifier qu'aucun processus ne tourne
Write-Host ""
Write-Host "Étape 2 : Vérification..." -ForegroundColor Yellow
$remaining = Get-Process node -ErrorAction SilentlyContinue
if ($remaining) {
    Write-Host "   ⚠️  Des processus Node.js sont encore actifs" -ForegroundColor Red
    Write-Host "   Tentative d'arrêt forcé..." -ForegroundColor Yellow
    taskkill /F /IM node.exe 2>$null
    Start-Sleep -Seconds 2
} else {
    Write-Host "   ✅ Aucun processus Node.js actif" -ForegroundColor Green
}

# Étape 3 : Changer de répertoire
Write-Host ""
Write-Host "Étape 3 : Changement vers le répertoire server..." -ForegroundColor Yellow
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath
Write-Host "   Répertoire actuel : $(Get-Location)" -ForegroundColor Gray

# Étape 4 : Démarrer le serveur
Write-Host ""
Write-Host "Étape 4 : Démarrage du serveur..." -ForegroundColor Yellow
Write-Host "   Commande : npm start" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Le serveur va démarrer maintenant..." -ForegroundColor Cyan
Write-Host "  Appuyez sur Ctrl+C pour l'arrêter" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Démarrer le serveur
npm start



