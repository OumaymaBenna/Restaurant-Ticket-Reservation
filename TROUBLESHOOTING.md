# Guide de résolution des problèmes - ISET Restaurant App

## Problème : Impossible de lancer l'app dans l'émulateur

### Solutions à essayer :

#### 1. Synchroniser le projet Gradle
Dans Android Studio :
- Menu **File** → **Sync Project with Gradle Files**
- Ou cliquez sur l'icône d'éléphant en haut à droite
- Attendez que la synchronisation se termine

#### 2. Vérifier le JDK configuré
- Menu **File** → **Project Structure** (ou `Ctrl+Alt+Shift+S`)
- Onglet **SDK Location**
- Vérifiez que **JDK location** pointe vers Java 11 ou supérieur
- Si nécessaire, cliquez sur **Download JDK** pour télécharger Java 11

#### 3. Nettoyer et reconstruire le projet
- Menu **Build** → **Clean Project**
- Puis **Build** → **Rebuild Project**

#### 4. Invalider les caches
- Menu **File** → **Invalidate Caches / Restart...**
- Cochez toutes les options
- Cliquez sur **Invalidate and Restart**

#### 5. Vérifier l'émulateur
- Dans Android Studio, ouvrez **Device Manager**
- Vérifiez que votre émulateur est démarré
- Si l'émulateur n'apparaît pas, cliquez sur ▶️ pour le démarrer

#### 6. Vérifier les erreurs dans le log
- Ouvrez l'onglet **Build** en bas de l'écran
- Regardez les erreurs affichées
- Corrigez-les une par une

#### 7. Configuration manuelle du JDK pour Gradle
Si JAVA_HOME pointe vers JDK 1.8, dans Android Studio :
- Menu **File** → **Settings** (ou `Ctrl+Alt+S`)
- **Build, Execution, Deployment** → **Build Tools** → **Gradle**
- **Gradle JDK** : Sélectionnez un JDK 11+ (ou **Download JDK**)

---

## Problème : Erreur 404 lors de la création de compte ou "Compte non trouvé" lors de la connexion

### Solutions :

#### 1. Vérifier que le serveur Node.js est démarré
1. Ouvrez un terminal dans le dossier `server/`
2. Exécutez `node server.js` ou double-cliquez sur `start-server.bat`
3. Vous devriez voir :
   ```
   ✅ Connecté à MongoDB
   🚀 Serveur démarré sur http://localhost:3000
   ```

#### 2. Vérifier que MongoDB est démarré
- Le serveur doit être connecté à MongoDB sur `mongodb://127.0.0.1:27017`
- Si MongoDB n'est pas démarré, vous verrez un avertissement mais le serveur continuera
- Pour démarrer MongoDB :
  - Windows : Ouvrez les Services et démarrez "MongoDB"
  - Ou exécutez `mongod` dans un terminal

#### 3. Vérifier l'URL du serveur dans l'application
- L'application Android utilise `http://10.0.2.2:3000` pour se connecter au serveur
- Cette URL fonctionne uniquement depuis l'émulateur Android
- Si vous testez sur un appareil physique, vous devez utiliser l'IP de votre ordinateur (ex: `http://192.168.1.100:3000`)

#### 4. Vérifier les routes du serveur
Le serveur doit avoir ces routes actives :
- `POST /register` - Pour créer un compte
- `POST /login` - Pour se connecter

#### 5. Tester le serveur manuellement
Ouvrez un navigateur et allez sur `http://localhost:3000`
- Vous devriez voir : "🌍 Serveur Node.js opérationnel et connecté à MongoDB !"

#### 6. Vérifier les logs du serveur
Quand vous essayez de créer un compte ou de vous connecter, regardez la console du serveur :
- Vous devriez voir des logs comme : `📥 POST /register` ou `📥 POST /login`
- Si vous ne voyez rien, l'application n'arrive pas à se connecter au serveur

#### 7. Vérifier le firewall
- Assurez-vous que le port 3000 n'est pas bloqué par le firewall Windows
- Si nécessaire, autorisez Node.js dans le firewall

#### 8. Redémarrer le serveur après les modifications
- Après avoir modifié `server.js`, arrêtez le serveur (Ctrl+C) et redémarrez-le
- Les modifications ne sont prises en compte qu'après un redémarrage


















