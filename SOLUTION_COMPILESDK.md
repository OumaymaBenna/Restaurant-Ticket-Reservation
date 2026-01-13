# Solution : Erreur compileSdk

## ✅ Corrections appliquées

1. **compileSdk mis à jour** : Changé de 34 à 36
   - La dépendance `androidx.activity:activity:1.11.0` nécessite compileSdk 36

## 📋 Si le SDK 36 n'est pas installé

Si vous obtenez une erreur indiquant que le SDK 36 n'est pas trouvé :

### Option 1 : Installer le SDK 36 (Recommandé)
1. Dans Android Studio : **Tools** → **SDK Manager**
2. Onglet **SDK Platforms**
3. Cochez **Android 14.0 (API 36)** ou **Android 15.0 (API 36)**
4. Cliquez sur **Apply** pour installer
5. **File** → **Sync Project with Gradle Files**

### Option 2 : Rétrograder androidx.activity
Si vous ne pouvez pas installer le SDK 36, modifiez `gradle/libs.versions.toml` :

```toml
activity = "1.8.2"  # Au lieu de 1.11.0
```

Et remettez `compileSdk = 34` dans `app/build.gradle.kts`.

## ✅ État actuel

- ✅ compileSdk = 36
- ✅ targetSdk = 34 (peut rester à 34)
- ✅ minSdk = 24 (peut rester à 24)
- ✅ androidx.activity = 1.11.0

## 🚀 Prochaines étapes

1. **Synchroniser Gradle** : File → Sync Project with Gradle Files
2. **Nettoyer le projet** : Build → Clean Project
3. **Reconstruire** : Build → Rebuild Project
4. **Lancer l'app** : Run (▶️)

Le build devrait maintenant fonctionner !



