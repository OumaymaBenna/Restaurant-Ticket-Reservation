# Instructions pour redémarrer le serveur

## Problème
Les routes `/subscribe` et `/user/:studentId/balance` ne sont pas reconnues car le serveur n'a pas été redémarré avec le nouveau code.

## Solution

### Étape 1 : Arrêter le serveur actuel
1. Trouvez le terminal/console où le serveur Node.js est en cours d'exécution
2. Appuyez sur `Ctrl + C` pour arrêter le serveur
3. Attendez que le processus se termine complètement

### Étape 2 : Redémarrer le serveur
1. Ouvrez un terminal dans le dossier du projet
2. Naviguez vers le dossier `server` :
   ```bash
   cd server
   ```
3. Démarrez le serveur :
   ```bash
   node server.js
   ```

### Étape 3 : Vérifier que le serveur a bien démarré
Vous devriez voir dans la console :
```
🚀 Serveur démarré sur http://localhost:3000
📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
...
💳 Routes d'abonnement:
   - POST /subscribe (Payer abonnement 15 DNT)
   - GET /user/:studentId/balance (Récupérer solde)
```

Si vous voyez ces messages, le serveur a bien chargé les nouvelles routes.

### Étape 4 : Tester dans l'application Android
1. Ouvrez l'application Android
2. Allez dans l'interface "Abonnement"
3. Le solde devrait maintenant se charger sans erreur
4. Testez le bouton "Payer 15 DNT"

## Note importante
Si le serveur ne redémarre pas correctement, vérifiez qu'aucun autre processus Node.js n'utilise le port 3000.
