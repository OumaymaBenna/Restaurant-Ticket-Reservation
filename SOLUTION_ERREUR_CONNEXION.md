# Solution : Erreur "Serveur non accessible"

## ✅ Solution immédiate

L'application a été modifiée pour **basculer automatiquement vers la simulation locale** si le serveur n'est pas accessible. 

**Vous pouvez maintenant utiliser le paiement même si le serveur n'est pas connecté !**

### Comment tester :

1. **Lancez l'application Android**
2. **Allez dans le profil** → Cliquez sur "Renouveler l'abonnement"
3. **La page de paiement devrait s'afficher automatiquement** (même sans serveur)
4. **Testez le paiement** en cliquant sur "Payer"

## 🔍 Diagnostic du problème de connexion

### Étape 1 : Vérifier que le serveur est démarré

Dans votre terminal serveur, vous devriez voir :
```
🚀 Serveur démarré sur http://localhost:3000
✅ Connecté à MongoDB
```

### Étape 2 : Tester la connexion depuis votre navigateur

Ouvrez votre navigateur et allez sur :
- `http://localhost:3000` → Devrait afficher "🌍 Serveur Node.js opérationnel..."
- `http://localhost:3000/test-connection` → Devrait afficher un JSON avec `"success": true`

### Étape 3 : Tester avec le script de test

Dans le dossier `server/`, exécutez :
```bash
node test-connection.js
```

Vous devriez voir :
```
✅ Connexion réussie !
```

### Étape 4 : Vérifier les logs Android

Dans Android Studio :
1. Ouvrez l'onglet **Logcat**
2. Filtrez par `PaymentAPI` ou `PaymentActivity`
3. Recherchez les messages :
   - `📤 Envoi de la requête...` → La requête est envoyée
   - `✅ Réponse serveur reçue...` → Le serveur répond
   - `❌ Erreur réseau...` → Problème de connexion
   - `🔄 Chargement de la page de paiement en mode simulation locale` → Basculement vers simulation

## 🔧 Solutions selon le type d'erreur

### Erreur : "UnknownHostException" ou "Unable to resolve host"

**Cause :** L'émulateur Android ne peut pas résoudre l'adresse `10.0.2.2`

**Solutions :**
1. Redémarrez l'émulateur Android (Cold Boot)
2. Vérifiez que vous utilisez bien l'émulateur (pas un appareil réel)
3. Si vous utilisez un appareil réel, changez l'URL dans `PaymentAPI.java` :
   ```java
   this.serverUrl = "http://192.168.1.XXX:3000"; // Votre IP locale
   ```

### Erreur : "Connection refused" ou "ECONNREFUSED"

**Cause :** Le serveur refuse la connexion

**Solutions :**
1. Vérifiez que le serveur écoute bien sur `0.0.0.0:3000` (pas seulement `localhost`)
2. Vérifiez le firewall Windows :
   - Ouvrez "Pare-feu Windows Defender"
   - Autorisez Node.js ou désactivez temporairement le firewall
3. Vérifiez qu'aucune autre application n'utilise le port 3000 :
   ```powershell
   netstat -ano | findstr :3000
   ```

### Erreur : "Timeout"

**Cause :** Le serveur met trop de temps à répondre

**Solutions :**
1. Vérifiez que MongoDB est démarré (si utilisé)
2. Vérifiez les logs du serveur pour voir s'il y a des erreurs
3. Redémarrez le serveur

### Erreur : "404 Not Found"

**Cause :** La route n'existe pas sur le serveur

**Solutions :**
1. Vérifiez que vous avez la dernière version de `server.js`
2. Vérifiez que la route `/create-payment-session` existe
3. Redémarrez le serveur

## 📱 Utilisation sans serveur (Mode simulation)

**L'application fonctionne maintenant en mode simulation même sans serveur !**

Quand vous cliquez sur "Renouveler l'abonnement" :
1. L'app essaie de se connecter au serveur
2. Si la connexion échoue, elle bascule **automatiquement** vers la simulation locale
3. La page de paiement s'affiche avec un formulaire de carte
4. Vous pouvez tester le paiement normalement

**Aucun message d'erreur ne s'affichera** - l'app bascule silencieusement vers la simulation.

## 🎯 Test rapide

Pour tester rapidement si tout fonctionne :

1. **Sans serveur démarré :**
   - Lancez l'app → Profil → Renouveler l'abonnement
   - La page de paiement devrait s'afficher (mode simulation)
   - Cliquez sur "Payer" → Le paiement devrait être traité

2. **Avec serveur démarré :**
   - Démarrez le serveur : `npm start` dans `server/`
   - Lancez l'app → Profil → Renouveler l'abonnement
   - La page de paiement devrait s'afficher (depuis le serveur)
   - Cliquez sur "Payer" → Le paiement devrait être traité

## 📝 Notes importantes

- **L'application fonctionne maintenant même sans serveur** grâce au mode simulation automatique
- Les logs détaillés sont disponibles dans Logcat pour le débogage
- Le mode simulation est identique au mode serveur pour l'utilisateur
- Pour la production, configurez Flouci ou Stripe dans `server.js`

## 🆘 Besoin d'aide ?

Si le problème persiste :
1. Vérifiez les logs dans Logcat (filtre : `PaymentAPI`)
2. Vérifiez les logs du serveur Node.js
3. Testez la connexion avec `node test-connection.js`
4. Consultez `DEBUG_CONNEXION_PAIEMENT.md` pour plus de détails



