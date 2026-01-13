# 💳 Configurer Flouci pour Paiement par Carte Bancaire Virtuelle

## 🎯 Objectif

Activer le paiement par **carte bancaire virtuelle** via Flouci dans votre application.

## 📋 Étapes de Configuration

### Étape 1 : Créer un compte Flouci

1. **Aller sur** https://flouci.com
2. **Cliquer sur "S'inscrire"** ou "Sign Up"
3. **Remplir le formulaire** :
   - Email
   - Mot de passe
   - Informations de votre entreprise/projet
4. **Vérifier votre email**
5. **Se connecter** à votre compte

### Étape 2 : Obtenir les clés API

1. **Se connecter** à votre dashboard Flouci
2. **Aller dans** "API" ou "Developers" ou "Settings"
3. **Trouver** :
   - **App Token** (clé publique)
   - **App Secret** (clé secrète)
4. **Copier ces deux clés** (vous en aurez besoin)

### Étape 3 : Installer axios (si nécessaire)

Ouvrir un terminal dans le dossier `server` et taper :

```bash
npm install axios
```

**OU** si vous êtes déjà dans le dossier server :

```bash
cd server
npm install axios
```

### Étape 4 : Configurer les clés dans server.js

1. **Ouvrir** `server/server.js`
2. **Trouver** la ligne ~767 (dans la section Flouci)
3. **Remplacer** :
   ```javascript
   const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'VOTRE_APP_TOKEN_ICI';
   const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'VOTRE_APP_SECRET_ICI';
   ```
   
   **Par** :
   ```javascript
   const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'VOTRE_VRAIE_APP_TOKEN';
   const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'VOTRE_VRAIE_APP_SECRET';
   ```

   **Exemple** :
   ```javascript
   const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'flo_live_abc123xyz';
   const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'flo_live_secret_xyz789';
   ```

### Étape 5 : Redémarrer le serveur

1. **Arrêter le serveur** (Ctrl+C dans le terminal)
2. **Redémarrer** :
   ```bash
   npm start
   ```

### Étape 6 : Tester

1. **Lancer l'application Android**
2. **Aller dans Profil** → **S'abonner maintenant**
3. **Vous devriez voir** la page de paiement Flouci (au lieu de la simulation)
4. **Entrer les informations de votre carte bancaire virtuelle**
5. **Effectuer le paiement**

## 🔐 Sécurité (Optionnel mais Recommandé)

Pour plus de sécurité, utilisez des variables d'environnement :

### Créer un fichier .env

1. **Créer un fichier** `.env` dans le dossier `server/`
2. **Ajouter** :
   ```
   FLOUCI_APP_TOKEN=votre_app_token_ici
   FLOUCI_APP_SECRET=votre_app_secret_ici
   ```

### Installer dotenv

```bash
npm install dotenv
```

### Modifier server.js

**Au début du fichier** (après les require), ajouter :
```javascript
require('dotenv').config();
```

Les clés seront automatiquement chargées depuis le fichier `.env`

## ✅ Vérification

### Le serveur affiche :
```
✅ Session Flouci créée: [ID]
💳 URL de paiement Flouci: https://flouci.com/pay/...
```

### L'application affiche :
- Page de paiement Flouci (avec logo Flouci)
- Formulaire pour entrer les informations de carte
- Boutons de paiement Flouci

## 🧪 Mode Test vs Production

### Mode Test (Sandbox)
- Utilisez les clés de **test** de Flouci
- Les paiements ne sont pas réels
- Parfait pour tester

### Mode Production
- Utilisez les clés **live** de Flouci
- Les paiements sont réels
- Vous recevez l'argent sur votre compte Flouci

## 💡 Types de Cartes Acceptées

Flouci accepte :
- ✅ **Cartes bancaires tunisiennes** (Visa, Mastercard)
- ✅ **Cartes bancaires virtuelles** (e-Dinar, etc.)
- ✅ **Cartes prépayées**
- ✅ **Cartes internationales** (selon configuration)

## 🐛 Dépannage

### Erreur : "Axios non installé"
```bash
cd server
npm install axios
```

### Erreur : "Clés Flouci invalides"
- Vérifier que vous avez copié les bonnes clés
- Vérifier qu'il n'y a pas d'espaces avant/après
- Vérifier que vous utilisez les clés du bon environnement (test vs production)

### La page Flouci ne s'affiche pas
- Vérifier que le serveur est démarré
- Vérifier les logs du serveur pour voir les erreurs
- Vérifier que les clés sont bien configurées

### Le paiement ne se confirme pas
- Vérifier les URLs de callback dans `server.js`
- Vérifier que `shouldOverrideUrlLoading` détecte bien les URLs Flouci

## 📞 Support Flouci

- **Site web** : https://flouci.com
- **Documentation** : https://developer.flouci.com
- **Support** : support@flouci.com

## 🎉 C'est prêt !

Une fois configuré, vos utilisateurs pourront payer avec leur **carte bancaire virtuelle** directement dans l'application !



