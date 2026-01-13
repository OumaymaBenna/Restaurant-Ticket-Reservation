# Solution : Application ne démarre pas

## ✅ Corrections appliquées

1. **Attributs shadow invalides supprimés** : Tous les attributs `android:shadow*` ont été retirés des layouts XML
2. **ProgressBar optionnel** : Le code gère maintenant le cas où `progressBar` est null
3. **IDs vérifiés** : Tous les IDs dans les layouts correspondent au code Java

## 🔍 Vérifications à faire dans Android Studio

### 1. Synchroniser Gradle
- Menu **File** → **Sync Project with Gradle Files**
- Attendez la fin de la synchronisation

### 2. Nettoyer et reconstruire
- Menu **Build** → **Clean Project**
- Menu **Build** → **Rebuild Project**

### 3. Vérifier les logs
- Ouvrez l'onglet **Logcat** en bas de l'écran
- Filtrez par "Error" ou "FATAL"
- Lancez l'app et regardez les erreurs exactes

### 4. Vérifier l'émulateur
- L'émulateur est connecté (emulator-5554)
- Assurez-vous qu'il est démarré et fonctionnel

## 🚀 Commandes pour tester

```bash
# Nettoyer le projet
.\gradlew.bat clean

# Compiler
.\gradlew.bat assembleDebug

# Installer sur l'émulateur
.\gradlew.bat installDebug

# Voir les logs en temps réel
adb logcat | findstr "projet_tp"
```

## ⚠️ Erreurs courantes et solutions

### Erreur : "ClassNotFoundException"
**Solution :** 
- Build → Clean Project
- Build → Rebuild Project
- File → Invalidate Caches / Restart

### Erreur : "View not found" ou findViewById retourne null
**Solution :** 
- Vérifiez que `setContentView()` est appelé avant `findViewById()`
- Vérifiez que les IDs dans le layout correspondent au code Java

### Erreur : "Unable to resolve host"
**Solution :** 
- Le serveur Node.js n'est pas démarré
- Démarrez le serveur : `cd server && node server.js`

### L'app crash au démarrage
**Solution :** 
1. Ouvrez Logcat dans Android Studio
2. Filtrez par "FATAL" ou "AndroidRuntime"
3. Regardez l'erreur exacte et la ligne de code concernée
4. Partagez l'erreur pour obtenir une solution spécifique

## 📝 Prochaines étapes

1. **Lancez l'app dans Android Studio**
2. **Regardez les logs Logcat** pour voir l'erreur exacte
3. **Partagez l'erreur** si le problème persiste

## ✅ État actuel

- ✅ Build réussit (gradlew assembleDebug)
- ✅ Émulateur connecté
- ✅ Tous les layouts XML sont valides
- ✅ Tous les IDs correspondent
- ✅ Code Java gère les vues optionnelles

L'application devrait maintenant fonctionner. Si le problème persiste, vérifiez les logs Logcat pour l'erreur exacte.



