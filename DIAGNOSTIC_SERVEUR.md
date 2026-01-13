# Guide de Diagnostic - Erreur "Route non trouvée"

## 🔍 Vérifications à faire étape par étape

### 1. Vérifier que le serveur Node.js est démarré

**Windows :**
```bash
cd server
node server.js
```

**Ou utilisez le script :**
```bash
cd server
start-server.bat
```

**Vous devriez voir :**
```
✅ Connecté à MongoDB
🚀 Serveur démarré sur http://localhost:3000
📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
📡 Routes disponibles:
   - POST /register
   - POST /login
   ...
```

### 2. Tester le serveur dans le navigateur

Ouvrez votre navigateur et allez sur : **http://localhost:3000**

**Vous devriez voir :**
```
🌍 Serveur Node.js opérationnel et connecté à MongoDB !
```

Si vous ne voyez pas ce message, le serveur n'est pas démarré correctement.

### 3. Vérifier que MongoDB est démarré

Le serveur doit être connecté à MongoDB. Si MongoDB n'est pas démarré, vous verrez :
```
❌ Erreur de connexion MongoDB
⚠️  Assurez-vous que MongoDB est démarré sur mongodb://127.0.0.1:27017
```

**Pour démarrer MongoDB sur Windows :**
- Ouvrez les **Services** Windows (Win+R, tapez `services.msc`)
- Cherchez **MongoDB**
- Cliquez sur **Démarrer**

### 4. Vérifier l'URL dans l'application Android

L'application utilise : `http://10.0.2.2:3000`

**Important :**
- ✅ Cette URL fonctionne **uniquement depuis l'émulateur Android**
- ❌ Si vous testez sur un **appareil physique**, vous devez utiliser l'IP de votre ordinateur

**Pour trouver l'IP de votre ordinateur :**
```bash
ipconfig
```
Cherchez l'adresse IPv4 (ex: 192.168.1.100) et utilisez : `http://192.168.1.100:3000`

### 5. Vérifier les logs du serveur

Quand vous essayez de créer un compte, regardez la console du serveur.

**Vous devriez voir :**
```
📥 POST /register
   Body: {"fullName":"...","email":"...","studentId":"...","password":"..."}
```

**Si vous ne voyez rien :**
- L'application n'arrive pas à se connecter au serveur
- Vérifiez le firewall Windows
- Vérifiez que le port 3000 n'est pas bloqué

### 6. Vérifier le firewall Windows

Le port 3000 doit être autorisé dans le firewall.

**Pour autoriser Node.js dans le firewall :**
1. Ouvrez **Panneau de configuration** → **Pare-feu Windows**
2. Cliquez sur **Autoriser une application**
3. Cherchez **Node.js** et cochez les cases **Privé** et **Public**
4. Si Node.js n'apparaît pas, cliquez sur **Autoriser une autre application** et ajoutez Node.js

### 7. Tester avec curl (optionnel)

Si vous avez curl installé, testez la route directement :

```bash
curl -X POST http://localhost:3000/register \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Test User\",\"email\":\"test@test.com\",\"studentId\":\"12345\",\"password\":\"test123\"}"
```

**Réponse attendue :**
- Si succès : Code 201 avec les données de l'utilisateur
- Si erreur : Code 400/409/500 avec un message d'erreur

### 8. Vérifier les erreurs dans Android Studio

Dans Android Studio, ouvrez l'onglet **Logcat** et filtrez par "RegisterActivity".

**Cherchez :**
- Erreurs réseau
- Messages de connexion
- Codes d'erreur HTTP

## 🛠️ Solutions courantes

### Problème : "Unable to resolve host"
**Solution :** Vérifiez que vous utilisez l'émulateur Android (10.0.2.2) et non un appareil physique.

### Problème : "Connection refused"
**Solution :** Le serveur n'est pas démarré. Démarrez-le avec `node server.js`.

### Problème : "Route non trouvée (404)"
**Solutions possibles :**
1. Le serveur n'est pas démarré
2. L'URL de base est incorrecte
3. La route n'existe pas dans le serveur (vérifiez server.js)

### Problème : Le serveur démarre mais MongoDB n'est pas connecté
**Solution :** Démarrez MongoDB. Le serveur continuera à fonctionner mais les opérations de base de données échoueront.

## 📝 Checklist rapide

- [ ] Serveur Node.js démarré (`node server.js`)
- [ ] MongoDB démarré
- [ ] Test navigateur : http://localhost:3000 fonctionne
- [ ] Utilisation de l'émulateur Android (10.0.2.2)
- [ ] Firewall autorise Node.js
- [ ] Logs du serveur montrent les requêtes POST /register

## 🆘 Si rien ne fonctionne

1. **Redémarrez le serveur** (Ctrl+C puis `node server.js`)
2. **Redémarrez l'émulateur Android**
3. **Vérifiez les logs** dans Android Studio (Logcat)
4. **Vérifiez les logs** du serveur Node.js







