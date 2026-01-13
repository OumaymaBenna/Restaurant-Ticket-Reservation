# 💳 Configurer Flouci pour Paiement par Carte Bancaire Virtuelle

## 🎯 Votre serveur se trouve ici : `C:\Users\ASUS\restaurant-server`

## ⚡ Configuration Rapide

### 1️⃣ Installer axios

Ouvrir un terminal dans le dossier `C:\Users\ASUS\restaurant-server` :

```bash
cd C:\Users\ASUS\restaurant-server
npm install
```

### 2️⃣ Obtenir vos clés Flouci

1. Aller sur https://flouci.com
2. S'inscrire et créer un compte
3. Dans le dashboard → **API** ou **Developers**
4. Copier **App Token** et **App Secret**

### 3️⃣ Configurer les clés

**Ouvrir** `C:\Users\ASUS\restaurant-server\server.js`

**Trouver** les lignes ~767-768 :

```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'VOTRE_APP_TOKEN_ICI';
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'VOTRE_APP_SECRET_ICI';
```

**Remplacer** par vos vraies clés :

```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'flo_live_votre_token_ici';
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'flo_live_votre_secret_ici';
```

### 4️⃣ Redémarrer le serveur

```bash
cd C:\Users\ASUS\restaurant-server
npm start
```

### 5️⃣ Tester

1. Lancer l'application Android
2. Profil → S'abonner maintenant
3. La page Flouci devrait s'afficher pour entrer votre carte bancaire virtuelle

## ✅ Vérification

Dans les logs du serveur, vous devriez voir :
```
✅ Session Flouci créée: [ID]
💳 URL de paiement Flouci: https://flouci.com/pay/...
```

## 🎉 C'est prêt !

Vos utilisateurs peuvent maintenant payer avec leur **carte bancaire virtuelle** via Flouci !



