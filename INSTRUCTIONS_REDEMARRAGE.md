# Instructions pour résoudre l'erreur "Route non trouvée"

## Problème
L'erreur "Erreur lors de la récupération du solde" indique que le serveur n'a pas chargé les nouvelles routes d'abonnement.

## Solution étape par étape

### Étape 1 : Arrêter TOUS les processus Node.js
1. Ouvrez le **Gestionnaire des tâches** (Ctrl + Shift + Esc)
2. Cherchez tous les processus "Node.js" ou "node.exe"
3. Cliquez droit sur chacun → **Terminer la tâche**
4. Fermez tous les terminaux où le serveur pourrait tourner

### Étape 2 : Vérifier que le serveur est bien arrêté
- Ouvrez un nouveau terminal
- Tapez : `netstat -ano | findstr :3000`
- Si vous voyez des résultats, cela signifie qu'un processus utilise encore le port 3000
- Dans ce cas, notez le PID et tuez-le : `taskkill /PID [PID] /F`

### Étape 3 : Redémarrer le serveur
1. Ouvrez un **nouveau terminal** (important : nouveau terminal)
2. Naviguez vers le dossier du projet :
   ```bash
   cd C:\Users\ASUS\projet_tp\server
   ```
3. Démarrez le serveur :
   ```bash
   node server.js
   ```

### Étape 4 : Vérifier que les routes sont chargées
Dans la console du serveur, vous devriez voir :
```
🚀 Serveur démarré sur http://localhost:3000
...
💳 Routes d'abonnement:
   - POST /subscribe (Payer abonnement 15 DNT)
   - GET /user/:studentId/balance (Récupérer solde)
✅ Routes d'abonnement enregistrées avec succès!
```

**Si vous ne voyez PAS ces messages**, le serveur n'a pas chargé le nouveau code.

### Étape 5 : Tester dans l'application
1. Ouvrez l'application Android
2. Allez dans "Abonnement"
3. Regardez la console du serveur

**Vous devriez voir** :
- `📥 GET /user/45646545341/balance`
- `✅ Route GET /user/:studentId/balance appelée`

**Si vous voyez** :
- `❌ Route non trouvée: GET /user/45646545341/balance`

Cela signifie que le serveur n'a pas été redémarré avec le nouveau code.

## Solution alternative : Vérifier le fichier server.js

Si le problème persiste, vérifiez que le fichier `server/server.js` contient bien les routes aux lignes :
- Ligne ~1015 : `app.post('/subscribe', ...)`
- Ligne ~1067 : `app.get('/user/:studentId/balance', ...)`

Si ces lignes n'existent pas, le fichier n'a pas été sauvegardé correctement.
