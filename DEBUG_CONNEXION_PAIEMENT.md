# Guide de débogage - Connexion au serveur de paiement

## Problème : "Serveur non accessible" dans l'application Android

### ✅ Vérifications à faire

#### 1. Vérifier que le serveur Node.js est démarré

Dans le terminal où vous avez lancé le serveur, vous devriez voir :
```
🚀 Serveur démarré sur http://localhost:3000
📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
✅ Connecté à MongoDB
```

Si vous ne voyez pas ces messages, le serveur n'est pas démarré correctement.

#### 2. Tester la connexion depuis un navigateur

Ouvrez votre navigateur et allez sur :
- `http://localhost:3000` → Devrait afficher "🌍 Serveur Node.js opérationnel..."
- `http://localhost:3000/payment-page?amount=15&userId=test&email=test@test.com` → Devrait afficher la page de paiement

Si ces URLs ne fonctionnent pas, le serveur n'est pas accessible.

#### 3. Vérifier l'URL dans l'application Android

L'application Android utilise l'URL : `http://10.0.2.2:3000`

**Important :**
- `10.0.2.2` est l'adresse spéciale de l'émulateur Android pour accéder à `localhost` de votre machine
- Cette URL fonctionne **uniquement** depuis l'émulateur Android
- Si vous testez sur un appareil réel, vous devez utiliser l'IP locale de votre machine (ex: `http://192.168.1.100:3000`)

#### 4. Vérifier les logs Android

Dans Android Studio :
1. Ouvrez l'onglet **Logcat** (en bas de l'écran)
2. Filtrez par `PaymentAPI` ou `PaymentActivity`
3. Recherchez les messages commençant par :
   - `📤 Envoi de la requête...`
   - `✅ Réponse serveur reçue...`
   - `❌ Erreur réseau...`

Les logs vous indiqueront exactement quelle erreur se produit.

#### 5. Tester la route de paiement manuellement

Depuis votre terminal (ou Postman), testez :

```bash
curl -X POST http://localhost:3000/create-payment-session \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15.0,
    "userId": "test123",
    "userEmail": "test@test.com",
    "description": "Test paiement",
    "isSubscription": true
  }'
```

Vous devriez recevoir une réponse JSON avec une `url` de paiement.

### 🔧 Solutions courantes

#### Solution 1 : Redémarrer le serveur

1. Arrêtez le serveur (Ctrl+C dans le terminal)
2. Relancez-le : `npm start` ou `node server.js`
3. Vérifiez qu'il démarre sans erreur
4. Relancez l'application Android

#### Solution 2 : Vérifier le firewall Windows

Le firewall Windows peut bloquer les connexions entrantes :

1. Ouvrez **Pare-feu Windows Defender**
2. Vérifiez que Node.js est autorisé
3. Ou désactivez temporairement le firewall pour tester

#### Solution 3 : Vérifier que le port 3000 n'est pas utilisé

Dans PowerShell (Windows) :
```powershell
netstat -ano | findstr :3000
```

Si une autre application utilise le port 3000, arrêtez-la ou changez le port dans `server.js`.

#### Solution 4 : Utiliser l'IP locale pour un appareil réel

Si vous testez sur un appareil réel (pas l'émulateur) :

1. Trouvez votre IP locale :
   - Windows : `ipconfig` dans PowerShell
   - Cherchez "Adresse IPv4" (ex: 192.168.1.100)

2. Modifiez `PaymentAPI.java` ligne 28 :
   ```java
   this.serverUrl = "http://192.168.1.100:3000"; // Remplacez par votre IP
   ```

3. Assurez-vous que votre téléphone et votre ordinateur sont sur le même réseau Wi-Fi

#### Solution 5 : Vérifier la configuration réseau de l'émulateur

1. Dans Android Studio, ouvrez **Device Manager**
2. Cliquez sur les trois points (⋮) à côté de votre émulateur
3. Sélectionnez **Cold Boot Now** pour redémarrer l'émulateur

### 📊 Comprendre les erreurs dans les logs

#### Erreur : "UnknownHostException" ou "Unable to resolve host"
→ Le serveur n'est pas accessible. Vérifiez qu'il est démarré.

#### Erreur : "Connection refused" ou "ECONNREFUSED"
→ Le serveur refuse la connexion. Vérifiez qu'il écoute sur `0.0.0.0:3000`.

#### Erreur : "Timeout"
→ Le serveur met trop de temps à répondre. Vérifiez qu'il n'est pas bloqué.

#### Erreur : "404 Not Found"
→ La route n'existe pas. Vérifiez que le serveur a bien la route `/create-payment-session`.

### ✅ Test de fonctionnement

Une fois que tout fonctionne, vous devriez voir dans les logs Android :

```
PaymentAPI: 📤 Envoi de la requête de création de session de paiement
PaymentAPI:    URL: http://10.0.2.2:3000/create-payment-session
PaymentAPI: ✅ Réponse serveur reçue: {"success":true,"url":"http://10.0.2.2:3000/payment-page?...","gateway":"simulation"}
PaymentActivity: URL de paiement chargée: http://10.0.2.2:3000/payment-page?...
```

Et la WebView devrait charger la page de paiement avec le formulaire de carte.

### 🆘 Si rien ne fonctionne

1. Vérifiez que MongoDB est démarré (si vous utilisez MongoDB)
2. Vérifiez les logs du serveur Node.js pour voir les erreurs
3. Testez avec la simulation locale (l'application devrait basculer automatiquement)
4. Vérifiez que `usesCleartextTraffic="true"` est présent dans `AndroidManifest.xml`

---

**Note :** Même si la connexion au serveur échoue, l'application basculera automatiquement vers une simulation locale, donc vous pouvez toujours tester le paiement !



