# 🔍 Debug : ERR_CONNECTION_TIMED_OUT

## ❌ Problème

L'application Android affiche :
```
ERR_CONNECTION_TIMED_OUT
http://10.0.2.2:3000/payment-page?...
```

Cela signifie que l'application **essaie de se connecter** au serveur, mais la connexion **échoue**.

## ✅ Causes Possibles

### 1. Le serveur n'est pas démarré
Le serveur Node.js n'est pas en cours d'exécution.

### 2. Le serveur n'écoute pas sur la bonne interface
Le serveur doit écouter sur `0.0.0.0` (toutes les interfaces) pour être accessible depuis l'émulateur Android.

### 3. Problème de port
Le port 3000 est peut-être déjà utilisé par un autre processus.

### 4. Firewall bloque la connexion
Le firewall Windows bloque peut-être les connexions entrantes.

## 🔧 Solutions

### Solution 1 : Vérifier que le serveur est démarré

**Ouvrez un terminal** et exécutez :

```bash
cd server
npm start
```

Vous devriez voir :
```
✅ Connecté à MongoDB
🚀 Serveur démarré sur http://localhost:3000
💳 Routes de paiement:
   - GET /payment-page (Page de paiement simulée)
   ...
```

**Si vous ne voyez pas ces messages, le serveur n'est pas démarré.**

### Solution 2 : Vérifier que le serveur écoute sur toutes les interfaces

Dans `server.js`, ligne 1152, vérifiez :

```javascript
const HOST = '0.0.0.0'; // Écouter sur toutes les interfaces
```

**Si c'est `localhost` ou `127.0.0.1`, changez-le en `0.0.0.0`.**

### Solution 3 : Tester la connexion depuis le navigateur

Ouvrez votre navigateur et allez à :
```
http://localhost:3000/payment-page?amount=15.0&userId=test&email=test@example.com
```

**Si ça fonctionne dans le navigateur mais pas dans l'émulateur**, c'est un problème de réseau entre l'émulateur et votre machine.

### Solution 4 : Tester depuis l'émulateur

Dans l'émulateur Android, ouvrez le navigateur et allez à :
```
http://10.0.2.2:3000/payment-page?amount=15.0&userId=test&email=test@example.com
```

**Si ça ne fonctionne pas**, vérifiez :
- Le serveur est démarré
- Le serveur écoute sur `0.0.0.0`
- Aucun firewall ne bloque le port 3000

### Solution 5 : Vérifier le port

Vérifiez qu'aucun autre processus n'utilise le port 3000 :

**Windows PowerShell :**
```powershell
netstat -ano | findstr :3000
```

Si vous voyez des processus, notez le PID et arrêtez-le :
```powershell
taskkill /PID <PID> /F
```

## 🧪 Tests de Diagnostic

### Test 1 : Vérifier que le serveur répond

Dans un terminal, exécutez :

```bash
curl http://localhost:3000/test-connection
```

**Résultat attendu** : `{"success": true, "message": "Connexion réussie"}`

### Test 2 : Vérifier depuis l'émulateur

Dans l'émulateur Android, ouvrez le navigateur et allez à :
```
http://10.0.2.2:3000/test-connection
```

**Résultat attendu** : Page JSON avec `"success": true`

### Test 3 : Vérifier les logs du serveur

Quand vous essayez de vous connecter depuis l'app, vous devriez voir dans le terminal du serveur :

```
📥 GET /payment-page?amount=15.0&...
✅ Route /payment-page appelée
✅ HTML de paiement envoyé avec succès
```

**Si vous ne voyez RIEN**, le serveur ne reçoit pas la requête.

## 📝 Checklist

- [ ] Le serveur Node.js est démarré (`npm start`)
- [ ] Le serveur affiche "🚀 Serveur démarré sur http://localhost:3000"
- [ ] Le serveur écoute sur `0.0.0.0` (pas `localhost`)
- [ ] Le port 3000 n'est pas utilisé par un autre processus
- [ ] Le serveur répond dans le navigateur (`http://localhost:3000/test-connection`)
- [ ] Le serveur répond depuis l'émulateur (`http://10.0.2.2:3000/test-connection`)
- [ ] Aucun firewall ne bloque le port 3000

## 🆘 Solution d'Urgence

Si **RIEN** ne fonctionne, utilisez le fallback HTML local dans l'application Android :

L'application devrait automatiquement basculer vers un HTML local si le serveur n'est pas accessible. Vérifiez les logs Android (Logcat) pour voir si le fallback est activé.

---

**Note** : `10.0.2.2` est l'adresse IP spéciale de l'émulateur Android qui pointe vers `localhost` de votre machine hôte.



