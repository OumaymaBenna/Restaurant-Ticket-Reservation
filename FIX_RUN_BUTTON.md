# Solution : Le bouton Run ne fonctionne pas

## 🔧 Corrections appliquées

1. **compileSdk corrigé** : Changé de 36 à 34 (SDK 36 peut ne pas être disponible)

## 📋 Étapes à suivre dans Android Studio

### 1. Synchroniser Gradle
- Menu **File** → **Sync Project with Gradle Files**
- Attendez que la synchronisation se termine complètement
- Vérifiez qu'il n'y a pas d'erreurs dans l'onglet "Build"

### 2. Nettoyer le projet
- Menu **Build** → **Clean Project**
- Attendez la fin du nettoyage

### 3. Reconstruire le projet
- Menu **Build** → **Rebuild Project**
- Attendez la fin de la reconstruction

### 4. Vérifier l'émulateur/appareil
- Ouvrez **Tools** → **Device Manager**
- Assurez-vous qu'un appareil/émulateur est démarré
- Si aucun appareil n'est visible, créez ou démarrez un émulateur

### 5. Vérifier la configuration de Run
- Cliquez sur la flèche à côté du bouton Run
- Sélectionnez **Edit Configurations...**
- Vérifiez que :
  - **Module** : `app` est sélectionné
  - **Launch** : `Default Activity` ou `SplashActivity` est sélectionné
  - **Target** : Un appareil/émulateur est sélectionné

### 6. Invalider les caches
Si le problème persiste :
- Menu **File** → **Invalidate Caches / Restart...**
- Cochez toutes les options
- Cliquez sur **Invalidate and Restart**
- Attendez qu'Android Studio redémarre

## 🚨 Erreurs courantes

### Erreur : "No target device found"
**Solution :**
- Démarrez un émulateur dans Device Manager
- Ou connectez un appareil physique via USB avec le débogage USB activé

### Erreur : "SDK not found" ou "compileSdk not found"
**Solution :**
- Menu **Tools** → **SDK Manager**
- Installez le SDK 34 (Android 14)
- Menu **File** → **Sync Project with Gradle Files**

### Erreur : "Gradle sync failed"
**Solution :**
- Vérifiez votre connexion internet
- Menu **File** → **Sync Project with Gradle Files**
- Si ça échoue, supprimez le dossier `.gradle` dans le projet et resynchronisez

### Erreur : "Installation failed"
**Solution :**
- Désinstallez l'ancienne version de l'app sur l'appareil
- Ou changez le `applicationId` dans `build.gradle.kts`

## ✅ Vérifications finales

1. ✅ compileSdk = 34 (corrigé)
2. ✅ Émulateur/appareil connecté
3. ✅ Gradle synchronisé
4. ✅ Projet nettoyé et reconstruit
5. ✅ Configuration Run correcte

## 🎯 Test rapide

Après avoir suivi les étapes :
1. Cliquez sur le bouton **Run** (▶️) dans Android Studio
2. Sélectionnez votre appareil/émulateur
3. L'app devrait se lancer

Si le problème persiste, regardez l'onglet **Build** en bas pour voir l'erreur exacte.



