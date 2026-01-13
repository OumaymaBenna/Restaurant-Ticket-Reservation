# 🚀 Guide Rapide : Paiement par Carte Bancaire Virtuelle avec Flouci

## ⚡ Configuration en 5 minutes

### 1️⃣ Créer un compte Flouci
- Aller sur https://flouci.com
- S'inscrire et vérifier l'email
- Se connecter au dashboard

### 2️⃣ Obtenir les clés API
- Dans le dashboard Flouci → **API** ou **Developers**
- Copier **App Token** et **App Secret**

### 3️⃣ Installer axios
Ouvrir un terminal dans le dossier `server` :
```bash
npm install
```
(Cela installera axios automatiquement car il est maintenant dans package.json)

### 4️⃣ Configurer les clés

**Ouvrir** `server/server.js` et **trouver** les lignes ~767-768 :

```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'VOTRE_APP_TOKEN_ICI';
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'VOTRE_APP_SECRET_ICI';
```

**Remplacer** `VOTRE_APP_TOKEN_ICI` et `VOTRE_APP_SECRET_ICI` par vos vraies clés Flouci.

**Exemple** :
```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'flo_live_abc123xyz456';
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'flo_live_secret_xyz789abc';
```

### 5️⃣ Redémarrer le serveur

**Arrêter** le serveur (Ctrl+C) puis **redémarrer** :
```bash
npm start
```

### 6️⃣ Tester

1. **Lancer l'application Android**
2. **Profil** → **S'abonner maintenant**
3. **Vous devriez voir** la page Flouci pour entrer votre carte bancaire virtuelle
4. **Tester avec une carte** (mode test ou réelle selon vos clés)

## ✅ Vérification

### Dans les logs du serveur, vous devriez voir :
```
✅ Session Flouci créée: [ID]
💳 URL de paiement Flouci: https://flouci.com/pay/...
```

### Dans l'application :
- Page de paiement Flouci (avec logo)
- Formulaire pour carte bancaire virtuelle
- Boutons de paiement Flouci

## 🎯 Types de cartes acceptées

Flouci accepte :
- ✅ Cartes bancaires tunisiennes (Visa, Mastercard)
- ✅ **Cartes bancaires virtuelles** (e-Dinar, etc.)
- ✅ Cartes prépayées
- ✅ Cartes internationales

## 📝 Note importante

- **Mode Test** : Utilisez les clés de test Flouci (commencent par `flo_test_`)
- **Mode Production** : Utilisez les clés live (commencent par `flo_live_`)

## 🐛 Problème ?

Consultez `CONFIGURER_FLOUCI.md` pour un guide détaillé avec dépannage.

## 🎉 C'est tout !

Vos utilisateurs peuvent maintenant payer avec leur **carte bancaire virtuelle** via Flouci !



