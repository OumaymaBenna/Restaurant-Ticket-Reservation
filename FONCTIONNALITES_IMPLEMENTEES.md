# Fonctionnalités Implémentées - ISET Restaurant App

## ✅ 1. Persistance de la Connexion (Session Utilisateur)

### Fonctionnement
- **Stockage** : Utilise `SharedPreferences` pour sauvegarder l'état de connexion
- **Persistance** : La session reste active même après fermeture/redémarrage de l'application
- **Vérification automatique** : Au démarrage, `SplashActivity` vérifie si l'utilisateur est connecté
- **Déconnexion** : Uniquement via le bouton "Déconnexion" dans le profil

### Code concerné
- `SessionManager.java` : Gère la session avec `SharedPreferences`
- `SplashActivity.java` : Vérifie l'état de connexion au démarrage
- `LoginActivity.java` : Sauvegarde la session après connexion réussie

### Test
1. Connectez-vous une fois
2. Fermez l'application (stop run)
3. Relancez l'application (run)
4. ✅ Vous devriez être toujours connecté automatiquement

---

## ✅ 2. Persistance des Modifications du Profil

### Fonctionnement
- **Stockage** : Toutes les modifications sont sauvegardées dans `SharedPreferences`
- **Données persistantes** : Nom, email, téléphone, université, image de profil
- **Affichage automatique** : Les données modifiées s'affichent automatiquement au retour sur le profil

### Code concerné
- `SessionManager.java` : Méthodes `saveUser()` et `saveProfileImage()`
- `EditProfileActivity.java` : Sauvegarde les modifications
- `ProfileActivity.java` : Charge et affiche les données sauvegardées

### Test
1. Modifiez votre profil (nom, email, téléphone, image)
2. Sauvegardez
3. Fermez l'application
4. Relancez l'application
5. ✅ Toutes vos modifications doivent être visibles

---

## ✅ 3. Réservation de Tickets Repas (Déjeuner & Dîner)

### Fonctionnement
- **Durée de validité** : 24 heures à partir de la création
- **Expiration automatique** : Les tickets expirés sont supprimés du stockage local
- **Nettoyage** : Automatique au démarrage de l'application et à l'ouverture de HomeActivity
- **Stockage** : Les réservations sont aussi sauvegardées dans MongoDB (pour historique serveur)

### Code concerné
- `ReservationManager.java` : Gère l'expiration des réservations
- `ReservationFilter.java` : Filtre les réservations expirées
- `SplashActivity.java` : Nettoie au démarrage
- `HomeActivity.java` : Nettoie à l'ouverture

### Règle d'expiration
```
Date de création + 24 heures = Date d'expiration
Si Date d'expiration < Maintenant → Ticket expiré (supprimé)
```

### Test
1. Réservez un déjeuner ou dîner
2. Attendez 24h (ou modifiez la date système)
3. Relancez l'application
4. ✅ Le ticket expiré ne doit plus apparaître

---

## ✅ 4. Gestion Stricte du Ticket Repas Froid

### Fonctionnement
- **IMPORTANT** : Les tickets ne sont **JAMAIS supprimés de MongoDB**
- **Cachage uniquement** : Les tickets expirés sont cachés dans l'application mais restent dans MongoDB
- **Historique préservé** : Tous les tickets restent dans MongoDB pour l'historique
- **Affichage** : Seuls les tickets valides (aujourd'hui ou dans le futur) sont affichés
- **Réservation future** : L'utilisateur peut réserver pour la semaine prochaine sans problème

### Code concerné
- `ReservationFilter.java` : Filtre les repas froids expirés (sans supprimer de MongoDB)
- `ReservationManager.java` : Utilise le filtre pour l'affichage
- `MealReservationAPI.java` : Récupère depuis MongoDB (tous les tickets, y compris expirés)

### Règle d'expiration
```
Date du ticket (samedi soir) >= Aujourd'hui → Ticket valide (affiché)
Date du ticket (samedi soir) < Aujourd'hui → Ticket expiré (caché mais reste dans MongoDB)
```

### Test
1. Réservez un repas froid pour samedi prochain
2. ✅ Le ticket doit s'afficher
3. Attendez que la date passe (ou modifiez la date système)
4. Relancez l'application
5. ✅ Le ticket expiré ne s'affiche plus dans l'app
6. ✅ Mais il reste dans MongoDB (vérifiable via le serveur)

---

## 📋 Architecture du Système

### Stockage Local (SharedPreferences)
- **Session utilisateur** : `session` (SharedPreferences)
- **Réservations normales** : `reservations` (SharedPreferences) - Peuvent être supprimées si expirées
- **Réservations repas froid** : `reservations` (SharedPreferences) - Filtrées mais jamais supprimées

### Stockage Serveur (MongoDB)
- **Réservations normales** : Collection `mealreservations` - Restent pour historique
- **Réservations repas froid** : Collection `coldmealreservations` - **JAMAIS supprimées**

### Filtrage
- **ReservationFilter** : Classe utilitaire qui filtre sans supprimer de MongoDB
- **ReservationManager** : Gère le stockage local et utilise ReservationFilter

---

## 🔄 Flux de Données

### Connexion
```
LoginActivity → SessionManager.saveUserSession() → SharedPreferences
SplashActivity → SessionManager.isLoggedIn() → Vérifie SharedPreferences
```

### Modification de Profil
```
EditProfileActivity → SessionManager.saveUser() → SharedPreferences
ProfileActivity → SessionManager.getFullName() → Lit SharedPreferences
```

### Réservation Déjeuner/Dîner
```
ReservationActivity → API → MongoDB (sauvegarde)
ReservationActivity → ReservationManager.saveReservation() → SharedPreferences (local)
SplashActivity → ReservationManager.cleanExpiredReservations() → Supprime expirées du local
```

### Réservation Repas Froid
```
ReservationActivity → API → MongoDB (sauvegarde - JAMAIS supprimé)
ReservationActivity → ReservationManager.saveColdMealReservation() → SharedPreferences (local)
SplashActivity → ReservationFilter.filterValidColdMealReservations() → Filtre seulement l'affichage
```

---

## ⚠️ Points Importants

1. **Repas Froids** : Les tickets expirés restent dans MongoDB pour l'historique
2. **Déjeuner/Dîner** : Les tickets expirés sont supprimés du stockage local mais restent dans MongoDB
3. **Session** : Persiste indéfiniment jusqu'à déconnexion manuelle
4. **Profil** : Toutes les modifications persistent même après redémarrage

---

## 🧪 Tests à Effectuer

### Test Session
- [ ] Se connecter
- [ ] Fermer l'app
- [ ] Relancer l'app
- [ ] Vérifier qu'on est toujours connecté

### Test Profil
- [ ] Modifier le profil
- [ ] Fermer l'app
- [ ] Relancer l'app
- [ ] Vérifier que les modifications sont toujours là

### Test Déjeuner/Dîner
- [ ] Réserver un déjeuner
- [ ] Attendre 24h (ou changer date système)
- [ ] Relancer l'app
- [ ] Vérifier que le ticket n'apparaît plus

### Test Repas Froid
- [ ] Réserver un repas froid pour samedi prochain
- [ ] Vérifier qu'il s'affiche
- [ ] Attendre que la date passe (ou changer date système)
- [ ] Relancer l'app
- [ ] Vérifier qu'il ne s'affiche plus
- [ ] Vérifier dans MongoDB qu'il est toujours là






