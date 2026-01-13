# 💳 Intégration Paiement Virtuel - Guide Complet

## 🎯 Vue d'ensemble

Ce guide vous montre comment intégrer un **paiement virtuel réel** dans votre application. Le système supporte plusieurs passerelles de paiement.

## 🚀 Options de Paiement Virtuel

### 1. Flouci (Tunisie) - RECOMMANDÉ
- ✅ Supporte le Dinar tunisien (TND)
- ✅ Cartes bancaires tunisiennes
- ✅ Facile à intégrer
- 🌐 Site : https://flouci.com

### 2. Stripe (International)
- ✅ Supporte plusieurs devises
- ✅ Très sécurisé
- 🌐 Site : https://stripe.com

### 3. Autres passerelles
- Paymee (Tunisie)
- CMI (Maroc)
- PayPal

## 📋 Intégration Flouci (Étape par étape)

### Étape 1 : Créer un compte Flouci

1. Aller sur https://flouci.com
2. Créer un compte
3. Vérifier votre email
4. Aller dans **Dashboard** → **API Keys**
5. Copier :
   - **App Token** (clé publique)
   - **App Secret** (clé secrète)

### Étape 2 : Configurer le serveur

1. **Installer axios** (si pas déjà installé) :
```bash
cd server
npm install axios
```

2. **Modifier `server/server.js`** :

Trouvez la section `/create-payment-session` et décommentez le code Flouci :

```javascript
const axios = require('axios');

// Remplacez par vos vraies clés Flouci
const FLOUCI_APP_TOKEN = 'VOTRE_APP_TOKEN_ICI';
const FLOUCI_APP_SECRET = 'VOTRE_APP_SECRET_ICI';

// Dans la fonction create-payment-session, décommentez :
const response = await axios.post('https://api.flouci.com/api/generatePayment', {
  app_token: FLOUCI_APP_TOKEN,
  app_secret: FLOUCI_APP_SECRET,
  amount: Math.round(amount * 1000), // Flouci utilise millimes
  success_link: 'votre-app://payment_success',
  fail_link: 'votre-app://payment_cancel',
  developer_tracking_id: userId,
  customer_email: userEmail
}, {
  headers: {
    'Content-Type': 'application/json'
  }
});

if (response.data && response.data.result && response.data.result.link) {
  return res.json({ 
    success: true,
    url: response.data.result.link,
    paymentId: response.data.result.id,
    gateway: 'flouci'
  });
}
```

### Étape 3 : Variables d'environnement (Recommandé)

Pour plus de sécurité, utilisez des variables d'environnement :

1. **Créer un fichier `.env` dans le dossier `server/`** :
```
FLOUCI_APP_TOKEN=votre_app_token
FLOUCI_APP_SECRET=votre_app_secret
```

2. **Installer dotenv** :
```bash
cd server
npm install dotenv
```

3. **Ajouter au début de `server.js`** :
```javascript
require('dotenv').config();
```

4. **Modifier le code** :
```javascript
const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN;
const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET;
```

### Étape 4 : Tester

1. **Démarrer le serveur** :
```bash
cd server
npm start
```

2. **Lancer l'application Android**
3. **Aller dans Profil** → **S'abonner maintenant**
4. **Vérifier** que la page Flouci s'affiche (au lieu de la simulation)

## 📋 Intégration Stripe

### Étape 1 : Créer un compte Stripe

1. Aller sur https://stripe.com
2. Créer un compte
3. Aller dans **Developers** → **API keys**
4. Copier la **Secret key** (commence par `sk_test_`)

### Étape 2 : Installer Stripe

```bash
cd server
npm install stripe
```

### Étape 3 : Configurer le serveur

Dans `server/server.js`, décommentez le code Stripe :

```javascript
const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY || 'sk_test_VOTRE_CLE');

const session = await stripe.checkout.sessions.create({
  payment_method_types: ['card'],
  line_items: [{
    price_data: {
      currency: 'usd', // ou 'tnd' si disponible
      product_data: { 
        name: description || 'Réservation repas',
      },
      unit_amount: Math.round(amount * 100), // Centimes
    },
    quantity: 1,
  }],
  mode: 'payment',
  success_url: 'votre-app://payment_success?session_id={CHECKOUT_SESSION_ID}',
  cancel_url: 'votre-app://payment_cancel',
  customer_email: userEmail,
});

return res.json({ 
  success: true,
  url: session.url, 
  sessionId: session.id,
  gateway: 'stripe'
});
```

## 🔧 Configuration Android

L'application Android est **déjà configurée** pour utiliser le paiement virtuel !

### Comment ça fonctionne :

1. **PaymentActivity** appelle automatiquement le serveur
2. Le serveur crée une session avec la passerelle (Flouci/Stripe)
3. L'URL de paiement est retournée
4. La WebView charge cette URL
5. L'utilisateur paie sur la page de la passerelle
6. Après paiement, redirection vers `payment_success` ou `payment_cancel`
7. L'application détecte le callback et confirme le paiement

### Aucune modification Android nécessaire !

Le code Android gère automatiquement :
- ✅ Création de session via `PaymentAPI`
- ✅ Chargement de l'URL dans WebView
- ✅ Détection des callbacks
- ✅ Confirmation du paiement

## 🧪 Tester avec des cartes de test

### Flouci
- Utilisez une vraie carte bancaire tunisienne
- En mode test, Flouci peut fournir des cartes de test

### Stripe
Cartes de test :
- **Succès** : `4242 4242 4242 4242`
- **Échec** : `4000 0000 0000 0002`
- Date : n'importe quelle date future
- CVC : n'importe quel 3 chiffres

## 🔐 Sécurité

### ⚠️ IMPORTANT :

1. **Ne JAMAIS** stocker les clés API dans le code Android
2. **Toujours** utiliser le serveur comme intermédiaire
3. **Utiliser HTTPS** en production
4. **Valider les paiements** côté serveur après callback

### Exemple de validation côté serveur :

```javascript
// Route pour vérifier un paiement Flouci
app.post('/verify-payment', async (req, res) => {
  const { paymentId } = req.body;
  
  const response = await axios.get(
    `https://api.flouci.com/api/verifyPayment/${paymentId}`,
    {
      headers: {
        'apppublic': FLOUCI_APP_SECRET
      }
    }
  );
  
  if (response.data.result.status === 'SUCCESS') {
    // Paiement confirmé - mettre à jour la base de données
    // ...
    res.json({ success: true });
  } else {
    res.json({ success: false });
  }
});
```

## 📱 URLs de callback

L'application détecte automatiquement ces URLs dans la WebView :

- `payment_success` → Paiement réussi
- `payment_cancel` → Paiement annulé
- `payment_error` → Erreur

Ces URLs sont configurées dans :
- Flouci : `success_link` et `fail_link`
- Stripe : `success_url` et `cancel_url`

## 🐛 Dépannage

### Le paiement ne fonctionne pas

1. **Vérifier que le serveur est démarré** :
```bash
cd server
npm start
```

2. **Vérifier les clés API** :
   - Flouci : Vérifier App Token et App Secret
   - Stripe : Vérifier Secret Key

3. **Vérifier les logs serveur** :
   - Regarder la console Node.js pour les erreurs

4. **Tester avec Postman** :
```bash
POST http://localhost:3000/create-payment-session
{
  "amount": 15,
  "userId": "test123",
  "userEmail": "test@example.com",
  "description": "Test",
  "isSubscription": true
}
```

### La page de paiement ne s'affiche pas

1. Vérifier la connexion réseau
2. Vérifier l'URL du serveur dans `PaymentAPI.java`
3. Vérifier les permissions Internet dans `AndroidManifest.xml`

## 📞 Support

- **Flouci** : https://flouci.com/support
- **Stripe** : https://stripe.com/docs/support
- **Documentation API Flouci** : https://developer.flouci.com

## ✅ Checklist d'intégration

- [ ] Compte créé sur la passerelle (Flouci/Stripe)
- [ ] Clés API obtenues
- [ ] Code serveur modifié avec les vraies clés
- [ ] Variables d'environnement configurées (optionnel mais recommandé)
- [ ] Serveur redémarré
- [ ] Test effectué avec une carte de test
- [ ] Callbacks vérifiés
- [ ] Validation côté serveur implémentée

## 🎉 C'est prêt !

Une fois configuré, vos utilisateurs pourront payer avec leur carte bancaire directement dans l'application !



