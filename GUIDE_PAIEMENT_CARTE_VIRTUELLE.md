# 💳 Guide : Paiement par Carte Virtuelle avec Flouci

## 🎯 Objectif

Activer le paiement par **carte bancaire virtuelle** (e-Dinar, cartes prépayées, etc.) via Flouci dans votre application Android.

## 📋 Prérequis

1. ✅ Serveur Node.js démarré
2. ✅ Compte Flouci créé sur https://flouci.com
3. ✅ Clés API Flouci obtenues

## 🔧 Configuration Étape par Étape

### Étape 1 : Installer axios (si pas déjà fait)

```bash
cd server
npm install axios
```

### Étape 2 : Obtenir vos clés Flouci

1. **Connectez-vous** à https://flouci.com
2. **Allez dans** votre dashboard → **API** ou **Developers**
3. **Copiez** :
   - **App Token** (clé publique)
   - **App Secret** (clé secrète)

### Étape 3 : Configurer les clés dans server.js

Ouvrez `server/server.js` et trouvez les lignes ~957-958 :

```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'VOTRE_APP_TOKEN_ICI';
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'VOTRE_APP_SECRET_ICI';
```

**Remplacez par vos vraies clés** :

```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'flo_live_VOTRE_TOKEN';
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'flo_live_VOTRE_SECRET';
```

**Exemple** :
```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'flo_live_abc123xyz789';
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'flo_live_secret_xyz789abc';
```

### Étape 4 : Redémarrer le serveur

```bash
# Arrêter le serveur (Ctrl+C)
# Puis redémarrer
npm start
```

Vous devriez voir dans les logs :
```
✅ Session Flouci créée: [ID]
💳 URL de paiement Flouci: https://flouci.com/pay/...
```

### Étape 5 : Tester dans l'application

1. **Lancez l'application Android**
2. **Allez dans** Profil → **Renouveler l'abonnement**
3. **La page Flouci devrait s'afficher** (au lieu de la simulation)
4. **Entrez les informations de votre carte virtuelle** :
   - Numéro de carte
   - Date d'expiration
   - CVV
   - Nom sur la carte
5. **Effectuez le paiement**

## 🎨 Types de Cartes Virtuelles Supportées

Flouci accepte :
- ✅ **e-Dinar** (carte virtuelle tunisienne)
- ✅ **Cartes prépayées** Visa/Mastercard
- ✅ **Cartes bancaires virtuelles** de toutes les banques tunisiennes
- ✅ **Cartes de débit** avec fonctionnalité virtuelle

## 🔐 Sécurité (Recommandé)

### Utiliser des variables d'environnement

1. **Créer un fichier** `.env` dans `server/` :
```env
FLOUCI_APP_TOKEN=flo_live_votre_token
FLOUCI_APP_SECRET=flo_live_votre_secret
SERVER_URL=http://10.0.2.2:3000
```

2. **Installer dotenv** :
```bash
npm install dotenv
```

3. **Ajouter au début de server.js** :
```javascript
require('dotenv').config();
```

4. **Ne jamais commiter** le fichier `.env` dans Git !

## 🧪 Mode Test vs Production

### Mode Test (Sandbox)

Flouci fournit des clés de test pour tester sans payer réellement :

- **Clés de test** : Commencent par `flo_test_...`
- **Cartes de test** : Flouci fournit des numéros de cartes de test
- **Aucun paiement réel** : Les transactions sont simulées

### Mode Production

- **Clés live** : Commencent par `flo_live_...`
- **Paiements réels** : Les transactions sont réelles
- **Argent reçu** : Vous recevez l'argent sur votre compte Flouci

## 📱 Fonctionnement dans l'Application

### Flux de paiement :

1. **Utilisateur clique** sur "Renouveler l'abonnement"
2. **Application** envoie une requête à `/create-payment-session`
3. **Serveur** crée une session Flouci et retourne l'URL de paiement
4. **WebView** charge la page Flouci
5. **Utilisateur** entre les informations de sa carte virtuelle
6. **Flouci** traite le paiement
7. **Flouci** redirige vers notre serveur (`/payment_success` ou `/payment_cancel`)
8. **Application** détecte la redirection et traite le résultat

### Détection automatique :

L'application détecte automatiquement :
- ✅ URLs de succès Flouci
- ✅ URLs d'annulation Flouci
- ✅ Callbacks du serveur
- ✅ Redirections de paiement

## 🐛 Dépannage

### Erreur : "Clés Flouci invalides"

**Solutions** :
- Vérifiez que vous avez copié les bonnes clés
- Vérifiez qu'il n'y a pas d'espaces avant/après
- Vérifiez que vous utilisez les clés du bon environnement (test vs production)

### La page Flouci ne s'affiche pas

**Solutions** :
- Vérifiez que le serveur est démarré
- Vérifiez les logs du serveur pour voir les erreurs
- Vérifiez que les clés sont bien configurées
- Vérifiez la connexion internet

### Le paiement ne se confirme pas

**Solutions** :
- Vérifiez que les URLs de callback sont correctes dans `server.js`
- Vérifiez que `checkPaymentCallback` détecte bien les URLs Flouci
- Vérifiez les logs Android (Logcat) pour voir les URLs détectées

### Erreur : "Axios non installé"

```bash
cd server
npm install axios
```

## 📊 Vérification

### Dans les logs du serveur :

```
💳 Création de session de paiement: { amount: 15, userId: '...', description: '...' }
🔗 URLs de callback Flouci:
   Succès: http://10.0.2.2:3000/payment_success?amount=15&user_id=...&gateway=flouci
   Échec: http://10.0.2.2:3000/payment_cancel?gateway=flouci
✅ Session Flouci créée: flo_xxxxx
💳 URL de paiement Flouci: https://flouci.com/pay/xxxxx
```

### Dans l'application Android :

- Page Flouci s'affiche (avec logo Flouci)
- Formulaire pour entrer les informations de carte
- Boutons de paiement Flouci
- Après paiement, redirection automatique et confirmation

## 💡 Conseils

1. **Testez d'abord en mode sandbox** avant de passer en production
2. **Vérifiez les logs** régulièrement pour détecter les problèmes
3. **Utilisez des variables d'environnement** pour la sécurité
4. **Testez avec différentes cartes** (virtuelles, prépayées, etc.)
5. **Vérifiez les callbacks** pour s'assurer que les paiements sont bien confirmés

## 📞 Support

- **Flouci** : https://flouci.com
- **Documentation Flouci** : https://developer.flouci.com
- **Support Flouci** : support@flouci.com

## ✅ C'est prêt !

Une fois configuré, vos utilisateurs pourront payer avec leur **carte bancaire virtuelle** (e-Dinar, cartes prépayées, etc.) directement dans l'application Android via Flouci !

---

**Note** : Pour un appareil réel (pas l'émulateur), modifiez `SERVER_URL` dans `.env` ou `server.js` pour utiliser l'IP locale de votre machine au lieu de `10.0.2.2`.



