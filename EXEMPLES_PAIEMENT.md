# 💳 Exemples de Remplissage pour le Paiement

## 📝 Données de Test pour le Formulaire de Paiement

### Exemple 1 : Carte Visa (Test)

**Numéro de carte :**
```
4111 1111 1111 1111
```
ou
```
4111111111111111
```

**Nom sur la carte :**
```
Oumayma Ben Ali
```
ou
```
Oumayma B. A.
```

**Date d'expiration (MM/AA) :**
```
12/25
```
ou
```
03/26
```

**CVV :**
```
123
```
ou
```
456
```

---

### Exemple 2 : Carte Mastercard (Test)

**Numéro de carte :**
```
5555 5555 5555 4444
```
ou
```
5555555555554444
```

**Nom sur la carte :**
```
Ahmed Mohamed
```

**Date d'expiration :**
```
06/27
```

**CVV :**
```
789
```

---

### Exemple 3 : Carte Virtuelle e-Dinar (Simulation)

**Numéro de carte :**
```
1234 5678 9012 3456
```

**Nom sur la carte :**
```
Oumayma
```

**Date d'expiration :**
```
12/24
```

**CVV :**
```
123
```

---

## 🎯 Exemple Complet de Remplissage

### Scénario : Paiement d'Abonnement de 15.000 TND

1. **Ouvrez l'application** → **Profil** → **Renouveler l'abonnement**

2. **Remplissez le formulaire :**

   ```
   Numéro de carte : 4111 1111 1111 1111
   Nom sur la carte : Oumayma Ben Ali
   Date d'expiration : 12/25
   CVV : 123
   ```

3. **Cliquez sur** "✅ Payer 15.000 TND"

4. **Résultat attendu :**
   - ✅ Message "Paiement Réussi!"
   - ✅ Abonnement activé avec solde de 15.000 TND
   - ✅ Retour automatique au profil

---

## 📋 Formatage Automatique

Le formulaire formate automatiquement :

### Numéro de carte
- **Vous tapez :** `4111111111111111`
- **S'affiche :** `4111 1111 1111 1111` (espaces automatiques)

### Date d'expiration
- **Vous tapez :** `1225`
- **S'affiche :** `12/25` (slash automatique)

### CVV
- **Vous tapez :** `123` ou `abc123`
- **S'affiche :** `123` (uniquement chiffres)

---

## ✅ Validation du Formulaire

Le formulaire vérifie que :

- ✅ **Numéro de carte** : Minimum 13 chiffres
- ✅ **Nom sur la carte** : Minimum 2 caractères
- ✅ **Date d'expiration** : Format MM/AA (5 caractères)
- ✅ **CVV** : 3 chiffres

Si un champ est invalide, une alerte s'affiche.

---

## 🧪 Données de Test Recommandées

### Pour les Tests (Mode Simulation)

| Champ | Exemple 1 | Exemple 2 | Exemple 3 |
|-------|-----------|-----------|-----------|
| **Numéro** | `4111 1111 1111 1111` | `5555 5555 5555 4444` | `1234 5678 9012 3456` |
| **Nom** | `Oumayma Ben Ali` | `Ahmed Mohamed` | `Test User` |
| **Expiration** | `12/25` | `06/27` | `03/24` |
| **CVV** | `123` | `456` | `789` |

---

## 💡 Conseils

1. **Pour tester rapidement :**
   - Utilisez n'importe quel numéro de 13+ chiffres
   - Le nom peut être court (ex: "Test")
   - La date peut être dans le futur (ex: 12/25)
   - Le CVV peut être n'importe quel 3 chiffres

2. **Formatage automatique :**
   - Tapez directement les chiffres, les espaces et slashes sont ajoutés automatiquement
   - Pas besoin de formater manuellement

3. **Validation :**
   - Si un champ est vide ou invalide, une alerte vous le dira
   - Corrigez le champ et réessayez

---

## 🎬 Exemple Visuel de Remplissage

```
┌─────────────────────────────────────┐
│  💳 Paiement par Carte [Mode Test]  │
├─────────────────────────────────────┤
│                                     │
│  Nom: Oumayma                       │
│  Email: s@gmail.com                 │
│                                     │
│  15.000 TND                         │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Numéro de carte                │ │
│  │ 4111 1111 1111 1111           │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Nom sur la carte               │ │
│  │ Oumayma Ben Ali                │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌──────────┐  ┌──────────┐        │
│  │ MM/AA    │  │ CVV      │        │
│  │ 12/25    │  │ 123      │        │
│  └──────────┘  └──────────┘        │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ✅ Payer 15.000 TND            │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ❌ Annuler                     │ │
│  └───────────────────────────────┘ │
│                                     │
│  ℹ️ Mode test - Paiement simulé     │
│     pour les tests                  │
└─────────────────────────────────────┘
```

---

## 🚀 Test Rapide

**Pour tester sans remplir le formulaire :**

1. Cliquez directement sur "✅ Payer 15.000 TND"
2. Le paiement sera validé même sans remplir les champs (en mode test)
3. Vous verrez le message de succès

**Pour tester avec validation :**

1. Remplissez au moins :
   - Numéro de carte : `1234 5678 9012 3456`
   - Nom : `Test`
   - Date : `12/25`
   - CVV : `123`
2. Cliquez sur "✅ Payer 15.000 TND"
3. Le paiement sera validé

---

## 📞 Note

En **mode simulation/test**, n'importe quelle donnée fonctionne. Les données ne sont pas envoyées à une vraie banque.

Pour un **vrai paiement** (avec Flouci configuré), vous devrez utiliser de vraies informations de carte bancaire.



