# 🚨 URGENT : Redémarrer le Serveur Node.js

## ❌ Problème Actuel

Les routes `/create-payment-session` et `/payment-page` **existent dans le code** mais le serveur ne les trouve pas.

**Cela signifie que le serveur Node.js n'a PAS été redémarré après l'ajout de ces routes.**

## ✅ Solution IMMÉDIATE

### Étape 1 : Arrêter TOUS les processus Node.js

**Ouvrez PowerShell** (en tant qu'administrateur si possible) et exécutez :

```powershell
taskkill /F /IM node.exe
```

Cela va arrêter **TOUS** les serveurs Node.js en cours d'exécution.

### Étape 2 : Vérifier qu'aucun processus Node.js ne tourne

```powershell
Get-Process node -ErrorAction SilentlyContinue
```

Si vous voyez des processus, répétez l'étape 1.

### Étape 3 : Redémarrer le serveur

```bash
cd server
npm start
```

### Étape 4 : Vérifier que les routes sont chargées

Vous devriez voir dans le terminal :

```
✅ Connecté à MongoDB
🚀 Serveur démarré sur http://localhost:3000
📡 Routes disponibles:
   ...
   - GET /payment-page (Page de paiement simulée)
   - POST /create-payment-session (Créer une session de paiement)
   ...
```

## 🔍 Vérification

### Test 1 : Tester la route dans le navigateur

Ouvrez votre navigateur et allez à :
```
http://localhost:3000/payment-page?amount=15.0&userId=test&email=test@example.com
```

**Vous devriez voir la page HTML de paiement** (pas une erreur JSON).

### Test 2 : Tester avec curl (optionnel)

```bash
curl -X POST http://localhost:3000/create-payment-session -H "Content-Type: application/json" -d "{\"amount\":15,\"userId\":\"test\",\"userEmail\":\"test@example.com\",\"description\":\"Test\"}"
```

**Vous devriez recevoir une réponse JSON avec `success: true`** (pas une erreur 404).

## ⚠️ Important

**Chaque fois que vous modifiez `server.js`, vous DEVEZ redémarrer le serveur.**

Node.js charge le code au démarrage. Si vous modifiez le fichier sans redémarrer, les changements ne sont **PAS** pris en compte.

## 📝 Logs Attendus

Après le redémarrage, quand vous cliquez sur "Payer" dans l'app, vous devriez voir :

```
📥 POST /create-payment-session
   Body: {"amount":15,...}
💳 Création de session de paiement: { amount: 15, ... }
✅ Session de paiement créée
```

**PAS** :
```
❌ Route non trouvée: POST /create-payment-session
```

---

**ACTION REQUISE** : Redémarrez le serveur MAINTENANT pour que les routes fonctionnent.



