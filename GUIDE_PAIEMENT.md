# Guide de Paiement - ISET Restaurant

## 📋 Vue d'ensemble

Le système de paiement fonctionne de deux manières :
1. **Paiement via abonnement** : Si l'utilisateur a un abonnement actif (15 DT/mois), chaque réservation déduit automatiquement 0.200 TND du solde
2. **Paiement en ligne** : Si pas d'abonnement ou solde insuffisant, l'utilisateur est redirigé vers une page de paiement WebView

## 🚀 Comment utiliser le paiement

### 1. Pour l'utilisateur

#### S'abonner (15 DT/mois)
1. Aller dans **Profil**
2. Voir la section **Abonnement Mensuel**
3. Cliquer sur **"S'abonner maintenant"**
4. Effectuer le paiement de 15 DT
5. Le solde est automatiquement crédité à 15.000 TND

#### Réserver un repas
1. Aller dans **Réservation**
2. Ajouter un repas au panier (Déjeuner, Dîner, ou Repas Froid)
3. Cliquer sur **"Réserver ce repas"**
4. **Si abonnement actif avec solde suffisant** :
   - 0.200 TND est automatiquement déduit du solde
   - La réservation est confirmée immédiatement
5. **Si pas d'abonnement ou solde insuffisant** :
   - Une page de paiement s'ouvre
   - Effectuer le paiement de 0.200 TND
   - La réservation est confirmée après paiement réussi

### 2. Pour le développeur

## 🔧 Intégration d'une vraie passerelle de paiement

### Option 1 : Stripe (Recommandé pour tests)

#### Étape 1 : Installer Stripe SDK
```bash
cd server
npm install stripe
```

#### Étape 2 : Ajouter la route de paiement dans server.js
```javascript
const stripe = require('stripe')('sk_test_VOTRE_CLE_SECRETE');

// Route pour créer une session de paiement
app.post('/create-payment-session', async (req, res) => {
  try {
    const { amount, userId, userEmail, description } = req.body;
    
    const session = await stripe.checkout.sessions.create({
      payment_method_types: ['card'],
      line_items: [{
        price_data: {
          currency: 'usd', // ou 'tnd' si disponible
          product_data: {
            name: description || 'Réservation repas',
          },
          unit_amount: Math.round(amount * 100), // Convertir en centimes
        },
        quantity: 1,
      }],
      mode: 'payment',
      success_url: `votre-app://payment_success?session_id={CHECKOUT_SESSION_ID}`,
      cancel_url: `votre-app://payment_cancel`,
      customer_email: userEmail,
      metadata: {
        userId: userId,
        amount: amount.toString()
      }
    });

    res.json({ sessionId: session.id, url: session.url });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

#### Étape 3 : Modifier PaymentActivity.java
Remplacer la méthode `buildPaymentUrl()` par :

```java
private String buildPaymentUrl(double amount, String userId, String userEmail, String userName) {
    // Appeler votre serveur pour créer une session Stripe
    // Puis charger l'URL de checkout Stripe dans la WebView
    String serverUrl = "http://10.0.2.2:3000/create-payment-session";
    // Faire une requête POST pour obtenir l'URL de paiement
    // Retourner l'URL Stripe
    return "https://checkout.stripe.com/pay/..."; // URL retournée par le serveur
}
```

### Option 2 : Passerelle de paiement tunisienne (ex: CMI, Flouci)

#### Exemple avec Flouci (Tunisie)

1. **S'inscrire sur Flouci** : https://flouci.com
2. **Obtenir les clés API** : App ID et App Secret
3. **Créer une route serveur** :

```javascript
const axios = require('axios');

app.post('/create-flouci-payment', async (req, res) => {
  try {
    const { amount, userId, userEmail } = req.body;
    
    const response = await axios.post('https://api.flouci.com/api/generatePayment', {
      app_token: 'VOTRE_APP_TOKEN',
      app_secret: 'VOTRE_APP_SECRET',
      amount: amount,
      success_link: 'votre-app://payment_success',
      fail_link: 'votre-app://payment_cancel',
      developer_tracking_id: userId
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    res.json({ 
      paymentUrl: response.data.result.link,
      paymentId: response.data.result.id 
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

4. **Modifier PaymentActivity** pour charger l'URL Flouci

### Option 3 : Paiement local (Simulation pour développement)

Le système actuel utilise une page HTML simulée. Pour un vrai paiement local :

1. **Créer une page de paiement sur votre serveur** :
```javascript
app.get('/payment-page', (req, res) => {
  const { amount, userId } = req.query;
  // Retourner une page HTML avec formulaire de paiement
  res.send(`
    <html>
      <body>
        <h2>Paiement</h2>
        <p>Montant: ${amount} TND</p>
        <form action="/process-payment" method="POST">
          <input type="hidden" name="amount" value="${amount}">
          <input type="hidden" name="userId" value="${userId}">
          <button type="submit">Payer</button>
        </form>
      </body>
    </html>
  `);
});

app.post('/process-payment', (req, res) => {
  // Traiter le paiement
  // Rediriger vers success ou cancel
  res.redirect('votre-app://payment_success');
});
```

## 📱 Configuration dans l'application Android

### Modifier PaymentActivity pour utiliser votre serveur

```java
private void loadPaymentPage() {
    String userId = sessionManager.getUserId();
    String userEmail = sessionManager.getEmail();
    
    // Option 1 : Charger depuis votre serveur
    String serverUrl = "http://10.0.2.2:3000/payment-page?amount=" + amount + "&userId=" + userId;
    webView.loadUrl(serverUrl);
    
    // Option 2 : Pour Stripe/Flouci, obtenir l'URL via API
    // Faire une requête POST à votre serveur pour obtenir l'URL de paiement
    // Puis charger cette URL dans la WebView
}
```

### Gérer les callbacks de paiement

Les URLs de callback sont détectées automatiquement dans `shouldOverrideUrlLoading()` :
- `payment_success` → Paiement réussi
- `payment_cancel` → Paiement annulé
- `payment_error` → Erreur de paiement

## 🔐 Sécurité

1. **Ne jamais stocker les clés API dans l'application Android**
   - Utiliser votre serveur comme intermédiaire
   - Le serveur communique avec la passerelle de paiement

2. **Valider les paiements côté serveur**
   - Vérifier le statut du paiement avec la passerelle
   - Ne pas faire confiance uniquement aux callbacks client

3. **Utiliser HTTPS en production**
   - Toutes les communications doivent être chiffrées

## 📝 Exemple complet : Intégration Stripe

### 1. Serveur (server.js)
```javascript
const stripe = require('stripe')('sk_test_...');

app.post('/create-payment-intent', async (req, res) => {
  const { amount, userId } = req.body;
  
  const paymentIntent = await stripe.paymentIntents.create({
    amount: Math.round(amount * 100),
    currency: 'usd',
    metadata: { userId }
  });
  
  res.json({ 
    clientSecret: paymentIntent.client_secret,
    publishableKey: 'pk_test_...'
  });
});
```

### 2. Android (PaymentActivity.java)
```java
// Dans loadPaymentPage(), faire une requête HTTP POST
// pour obtenir clientSecret, puis charger Stripe Checkout
```

## 🎯 Résumé

1. **Pour les tests** : Utiliser la simulation actuelle (page HTML)
2. **Pour la production** : 
   - Choisir une passerelle (Stripe, Flouci, etc.)
   - Créer une route serveur pour générer les sessions de paiement
   - Modifier `PaymentActivity.buildPaymentUrl()` pour charger l'URL réelle
   - Tester avec des cartes de test

## 📞 Support

Pour toute question sur l'intégration, consultez :
- Documentation Stripe : https://stripe.com/docs
- Documentation Flouci : https://flouci.com/docs
- Documentation CMI : https://www.cmi.co.ma



