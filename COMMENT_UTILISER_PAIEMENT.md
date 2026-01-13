# 💳 Comment Utiliser le Système de Paiement

## 🎯 Pour l'Utilisateur Final

### 1️⃣ S'abonner (15 DT/mois)

1. Ouvrir l'application
2. Aller dans l'onglet **"Profil"** (icône personne en bas)
3. Faire défiler jusqu'à la section **"Abonnement Mensuel"**
4. Voir le statut :
   - **"Aucun abonnement actif"** si pas d'abonnement
   - **"Abonnement actif"** si déjà abonné
5. Cliquer sur **"S'abonner maintenant"** (ou **"Renouveler l'abonnement"**)
6. Une page de paiement s'ouvre
7. Cliquer sur **"Payer 15.000 TND"**
8. ✅ Abonnement activé ! Solde crédité à 15.000 TND

### 2️⃣ Réserver un Repas

#### Avec Abonnement Actif :
1. Aller dans **"Réservation"**
2. Ajouter un repas (Déjeuner, Dîner, ou Repas Froid)
3. Cliquer sur **"Réserver ce repas"**
4. ✅ **Paiement automatique** : 0.200 TND déduit du solde
5. Réservation confirmée immédiatement
6. Voir le nouveau solde dans le message de confirmation

#### Sans Abonnement (ou Solde Insuffisant) :
1. Aller dans **"Réservation"**
2. Ajouter un repas
3. Cliquer sur **"Réserver ce repas"**
4. Une page de paiement s'ouvre
5. Cliquer sur **"Payer 0.200 TND"**
6. ✅ Paiement effectué, réservation confirmée

## 🔧 Pour le Développeur

### Utilisation Actuelle (Simulation)

Le système utilise actuellement une **simulation de paiement** pour les tests :

1. **Page HTML simulée** : Créée dans `PaymentActivity.buildPaymentUrl()`
2. **Boutons de test** : "Payer" et "Annuler"
3. **Callbacks** : Détection automatique des URLs `payment_success` et `payment_cancel`

### Passer à un Vrai Paiement

#### Option A : Utiliser le Serveur Node.js (Recommandé pour commencer)

1. **Le serveur est déjà configuré** avec une route `/payment-page`
2. **Modifier PaymentActivity.java** :

```java
private void loadPaymentPage() {
    String userId = sessionManager.getUserId();
    String userEmail = sessionManager.getEmail();
    
    // Utiliser le serveur Node.js
    String serverUrl = "http://10.0.2.2:3000/payment-page?amount=" + amount + 
                       "&userId=" + userId + 
                       "&email=" + java.net.URLEncoder.encode(userEmail, "UTF-8") +
                       "&description=" + (isSubscriptionPayment ? "Abonnement mensuel" : "Réservation repas");
    
    webView.loadUrl(serverUrl);
}
```

3. **Démarrer le serveur** :
```bash
cd server
npm start
```

#### Option B : Intégrer une Vraie Passerelle (Production)

Voir le fichier **GUIDE_PAIEMENT.md** pour :
- Intégration Stripe
- Intégration Flouci (Tunisie)
- Intégration CMI (Maroc)
- Création d'une passerelle personnalisée

### Tester le Paiement

1. **Lancer l'application**
2. **Se connecter** avec un compte
3. **Aller dans Profil** → Voir la section Abonnement
4. **Cliquer sur "S'abonner maintenant"**
5. **Vérifier** que la page de paiement s'affiche
6. **Cliquer sur "Payer"** → Vérifier le message de succès
7. **Retourner au Profil** → Vérifier que le solde est à 15.000 TND

### Flux de Paiement

```
Utilisateur clique "Réserver"
         ↓
Vérifier abonnement actif ?
         ↓
    ┌────┴────┐
    │         │
   OUI       NON
    │         │
    ↓         ↓
Solde >= 0.2?  Ouvrir PaymentActivity
    │         │
    ↓         ↓
Déduire 0.2  Page WebView
    │         │
    ↓         ↓
Confirmer   Paiement réussi?
    │         │
    └────┬────┘
         ↓
   Confirmer réservation
```

## 📝 Notes Importantes

1. **Mode Simulation** : Le système actuel simule le paiement pour les tests
2. **Production** : Remplacez par une vraie passerelle (voir GUIDE_PAIEMENT.md)
3. **Sécurité** : Ne stockez jamais les clés API dans l'application Android
4. **Serveur** : Utilisez votre serveur Node.js comme intermédiaire avec la passerelle

## 🐛 Dépannage

### La page de paiement ne s'affiche pas
- Vérifier que le serveur Node.js est démarré
- Vérifier l'URL : `http://10.0.2.2:3000` (émulateur) ou `http://VOTRE_IP:3000` (appareil réel)

### Le paiement ne se confirme pas
- Vérifier les callbacks dans `shouldOverrideUrlLoading()`
- Vérifier que l'URL contient `payment_success` ou `payment_cancel`

### L'abonnement ne se met pas à jour
- Vérifier `SessionManager.setSubscriptionBalance()`
- Vérifier que `handlePaymentSuccess()` est appelé

## 📞 Support

Pour plus de détails, consultez :
- `GUIDE_PAIEMENT.md` : Guide technique complet
- `PaymentActivity.java` : Code source de l'activité de paiement
- `server/server.js` : Routes serveur pour le paiement



