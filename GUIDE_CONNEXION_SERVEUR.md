# Guide de résolution - Erreur de connexion au serveur

## Problème
L'application Android affiche : "Erreur de connexion: failed to connect to /10.0.2.2 (port 300...)"

## Solutions

### 1. Démarrer le serveur Node.js

Ouvrez un terminal dans le dossier `server` et exécutez :

```bash
cd server
npm install  # Si ce n'est pas déjà fait
node server.js
```

Vous devriez voir :
```
🚀 Serveur démarré sur http://localhost:3000
📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
```

### 2. Vérifier que le serveur écoute bien

Le serveur doit être configuré pour écouter sur `0.0.0.0` (toutes les interfaces) pour permettre la connexion depuis l'émulateur.

Dans `server/server.js`, ligne 1160 :
```javascript
const HOST = '0.0.0.0'; // ✅ Correct
```

### 3. Vérifier le port

Le serveur doit écouter sur le port **3000**. Vérifiez dans `server/server.js` ligne 1159 :
```javascript
const PORT = 3000;
```

### 4. Tester la connexion depuis votre navigateur

Ouvrez votre navigateur et allez sur :
- http://localhost:3000/test-connection

Si cela fonctionne, le serveur est bien démarré.

### 5. Vérifier l'émulateur Android

L'adresse `10.0.2.2` est l'adresse spéciale utilisée par l'émulateur Android pour accéder à `localhost` de votre machine.

**Si vous utilisez un appareil physique** au lieu d'un émulateur :
- Remplacez `10.0.2.2` par l'adresse IP locale de votre ordinateur
- Trouvez votre IP : `ipconfig` (Windows) ou `ifconfig` (Mac/Linux)
- Exemple : `http://192.168.1.100:3000`

### 6. Vérifier le firewall Windows

Le firewall peut bloquer les connexions. Ajoutez une exception pour Node.js ou désactivez temporairement le firewall pour tester.

### 7. Vérifier MongoDB

Le serveur nécessite MongoDB. Assurez-vous que MongoDB est démarré :
```bash
# Windows (si installé comme service)
# MongoDB devrait démarrer automatiquement

# Ou démarrez manuellement
mongod
```

### 8. Configuration pour appareil physique

Si vous testez sur un appareil physique Android :

1. Trouvez l'adresse IP de votre ordinateur :
   ```bash
   ipconfig  # Windows
   # Cherchez "IPv4 Address" sous votre connexion réseau
   ```

2. Modifiez les fichiers suivants pour remplacer `10.0.2.2` par votre IP :
   - `app/src/main/java/com/example/projet_tp/api/RetrofitClient.java`
   - `app/src/main/java/com/example/projet_tp/api/MealReservationAPI.java`
   - `app/src/main/java/com/example/projet_tp/network/RetrofitClient.java`
   - `app/src/main/java/com/example/projet_tp/utils/Constants.java`

   Remplacez :
   ```java
   private static final String BASE_URL = "http://10.0.2.2:3000/";
   ```
   Par :
   ```java
   private static final String BASE_URL = "http://192.168.1.XXX:3000/"; // Votre IP
   ```

3. Assurez-vous que votre téléphone et votre ordinateur sont sur le même réseau Wi-Fi.

### 9. Vérifier les permissions Internet

Vérifiez que `AndroidManifest.xml` contient :
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 10. Redémarrer l'application

Après avoir démarré le serveur :
1. Fermez complètement l'application Android
2. Redémarrez-la
3. Réessayez de créer un compte

## Checklist rapide

- [ ] Serveur Node.js démarré (`node server.js` dans le dossier `server`)
- [ ] MongoDB démarré
- [ ] Port 3000 disponible (pas utilisé par un autre programme)
- [ ] Firewall autorise les connexions sur le port 3000
- [ ] Émulateur Android ou appareil physique connecté
- [ ] Application Android redémarrée après démarrage du serveur

## Test de connexion

Pour tester si le serveur répond, créez un fichier `test-connection.js` dans le dossier `server` :

```javascript
const http = require('http');

const options = {
  hostname: 'localhost',
  port: 3000,
  path: '/test-connection',
  method: 'GET'
};

const req = http.request(options, (res) => {
  console.log(`Status: ${res.statusCode}`);
  res.on('data', (d) => {
    process.stdout.write(d);
  });
});

req.on('error', (e) => {
  console.error(`Erreur: ${e.message}`);
});

req.end();
```

Exécutez : `node test-connection.js`

Si vous voyez "Status: 200", le serveur fonctionne correctement.



