# 🚀 Guide de démarrage du serveur

## ⚠️ PROBLÈME ACTUEL
L'erreur "failed to connect to /10.0.2.2 (port 300...)" signifie que **le serveur Node.js n'est pas démarré**.

## ✅ SOLUTION RAPIDE

### Option 1 : Utiliser le script de démarrage (Recommandé)

1. **Double-cliquez** sur le fichier `server/start-server.bat`
   - Ce script vérifie Node.js
   - Installe les dépendances si nécessaire
   - Démarre le serveur

2. **Attendez** de voir ce message :
   ```
   🚀 Serveur démarré sur http://localhost:3000
   📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
   ```

3. **Laissez cette fenêtre ouverte** (ne la fermez pas !)

4. **Retournez dans Android Studio** et réessayez de créer un compte

### Option 2 : Démarrage manuel

1. **Ouvrez un terminal** (PowerShell ou CMD)

2. **Allez dans le dossier server** :
   ```bash
   cd C:\Users\ASUS\projet_tp\server
   ```

3. **Installez les dépendances** (si pas déjà fait) :
   ```bash
   npm install
   ```

4. **Démarrez le serveur** :
   ```bash
   node server.js
   ```

5. **Vous devriez voir** :
   ```
   🚀 Serveur démarré sur http://localhost:3000
   📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
   ```

6. **Laissez cette fenêtre ouverte** et retournez dans Android Studio

## 🔍 Vérifier que le serveur fonctionne

### Test 1 : Depuis votre navigateur
Ouvrez votre navigateur et allez sur :
```
http://localhost:3000/test-connection
```

Vous devriez voir : `{"message":"Connexion réussie!","timestamp":"..."}`

### Test 2 : Script de test
Dans un nouveau terminal, dans le dossier `server` :
```bash
node test-connection.js
```

Vous devriez voir : `✅ Le serveur fonctionne correctement!`

## ⚠️ IMPORTANT

1. **Le serveur doit rester ouvert** pendant que vous utilisez l'application Android
2. **Ne fermez pas la fenêtre** où le serveur tourne
3. **Si vous fermez la fenêtre**, le serveur s'arrête et l'application ne pourra plus se connecter

## 🐛 Si ça ne fonctionne toujours pas

### Vérifier que le port 3000 n'est pas utilisé
```bash
netstat -ano | findstr :3000
```
Si vous voyez quelque chose, un autre programme utilise le port 3000.

### Vérifier que Node.js est installé
```bash
node --version
```
Si ça ne fonctionne pas, installez Node.js depuis https://nodejs.org/

### Vérifier MongoDB (optionnel mais recommandé)
Le serveur fonctionne sans MongoDB, mais certaines fonctionnalités ne marcheront pas.
Pour démarrer MongoDB (si installé) :
- Windows : Il devrait démarrer automatiquement comme service
- Sinon : `mongod` dans un terminal

## 📱 Pour appareil physique Android

Si vous testez sur un **vrai téléphone** (pas un émulateur) :

1. Trouvez l'IP de votre ordinateur :
   ```bash
   ipconfig
   ```
   Cherchez "IPv4 Address" (exemple : 192.168.1.100)

2. Modifiez les fichiers suivants pour remplacer `10.0.2.2` par votre IP :
   - `app/src/main/java/com/example/projet_tp/api/RetrofitClient.java`
   - `app/src/main/java/com/example/projet_tp/api/MealReservationAPI.java`
   - `app/src/main/java/com/example/projet_tp/network/RetrofitClient.java`
   - `app/src/main/java/com/example/projet_tp/utils/Constants.java`

3. Assurez-vous que votre téléphone et votre ordinateur sont sur le **même réseau Wi-Fi**

## ✅ Checklist

- [ ] Node.js est installé (`node --version` fonctionne)
- [ ] Les dépendances sont installées (`node_modules` existe dans `server/`)
- [ ] Le serveur est démarré (`node server.js` dans le dossier `server`)
- [ ] Le serveur affiche "🚀 Serveur démarré sur http://localhost:3000"
- [ ] Le test de connexion fonctionne (http://localhost:3000/test-connection)
- [ ] La fenêtre du serveur reste ouverte
- [ ] L'application Android est redémarrée
