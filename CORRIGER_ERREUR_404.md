# 🔧 Corriger l'Erreur 404 - Guide Rapide

## ⚠️ Problème

Vous voyez l'erreur : **"Erreur: Erreur 404 Utilisation de la simulation..."**

## ✅ Solution Rapide (3 étapes)

### Étape 1 : Démarrer le serveur

**Option A : Double-clic sur le fichier**
```
server/start-server.bat
```

**Option B : Via terminal**
```bash
cd server
npm start
```

### Étape 2 : Vérifier que le serveur est démarré

Vous devriez voir dans le terminal :
```
🚀 Serveur démarré sur http://localhost:3000
📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
```

### Étape 3 : Relancer l'application

1. **Fermer complètement l'application Android** (swipe depuis les apps récentes)
2. **Relancer l'application**
3. **Retester le paiement**

## 🎯 Résultat Attendu

- ✅ Plus d'erreur 404
- ✅ La page de paiement s'affiche correctement
- ✅ Le paiement fonctionne (simulation ou réel)

## 📝 Note Importante

**Le message "Utilisation de la simulation..." n'est PAS une erreur !**

C'est le **mode de test** qui fonctionne parfaitement pour :
- ✅ Tester l'application
- ✅ Vérifier que tout fonctionne
- ✅ Développer sans passerelle de paiement réelle

Pour activer un **paiement réel** (Flouci, Stripe), consultez `INTEGRATION_PAIEMENT_VIRTUEL.md`

## 🐛 Si ça ne fonctionne toujours pas

1. **Vérifier que Node.js est installé** :
   ```bash
   node --version
   ```

2. **Vérifier que les dépendances sont installées** :
   ```bash
   cd server
   npm install
   ```

3. **Vérifier le port 3000** :
   - Le port 3000 doit être libre
   - Si occupé, changer le port dans `server.js`

4. **Vérifier le firewall Windows** :
   - Autoriser Node.js dans le firewall

## 📞 Besoin d'aide ?

Consultez `DEMARRER_SERVEUR.md` pour un guide détaillé.



