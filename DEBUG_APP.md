# Guide de débogage - Application ne démarre pas

## Vérifications à faire

### 1. Vérifier les logs Android
Dans Android Studio :
- Ouvrez l'onglet **Logcat** en bas
- Filtrez par "Error" ou "FATAL"
- Lancez l'app et regardez les erreurs

### 2. Vérifier la synchronisation Gradle
- Menu **File** → **Sync Project with Gradle Files**
- Attendez que la synchronisation se termine

### 3. Nettoyer et reconstruire
```bash
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

### 4. Vérifier l'émulateur/appareil
- Assurez-vous qu'un appareil/émulateur est connecté
- Dans Android Studio : **Tools** → **Device Manager**
- Vérifiez que l'appareil est démarré

### 5. Vérifier les permissions
- L'app nécessite INTERNET
- Vérifiez dans **AndroidManifest.xml** que les permissions sont présentes

### 6. Erreurs courantes

#### Erreur : "Unable to resolve host"
- Le serveur n'est pas démarré
- Démarrez le serveur : `cd server && node server.js`

#### Erreur : "ClassNotFoundException"
- Problème de compilation
- Faites **Build** → **Clean Project** puis **Rebuild Project**

#### Erreur : "View not found" ou "findViewById returned null"
- Vérifiez que les IDs dans le layout correspondent au code Java
- Vérifiez que `setContentView()` est appelé avant `findViewById()`

### 7. Vérifier les ressources
- Tous les drawables référencés existent
- Tous les strings référencés existent dans `strings.xml`
- Tous les layouts référencés existent

### 8. Logs spécifiques à vérifier
Dans Logcat, cherchez :
- `AndroidRuntime` : Erreurs fatales
- `FATAL EXCEPTION` : Crash de l'app
- `ActivityManager` : Problèmes de démarrage d'activité

## Commandes utiles

```bash
# Nettoyer le projet
.\gradlew.bat clean

# Compiler en mode debug
.\gradlew.bat assembleDebug

# Installer sur l'appareil
.\gradlew.bat installDebug

# Voir les logs
adb logcat | findstr "projet_tp"
```

## Problèmes connus et solutions

### Problème : L'app crash au démarrage
**Solution :** Vérifiez les logs Logcat pour voir l'erreur exacte

### Problème : Écran blanc
**Solution :** Vérifiez que `SplashActivity` est bien définie comme activité principale dans `AndroidManifest.xml`

### Problème : Boutons ne fonctionnent pas
**Solution :** Vérifiez que les listeners sont bien définis et que les IDs correspondent



