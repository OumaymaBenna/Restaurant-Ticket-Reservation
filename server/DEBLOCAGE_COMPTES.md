# Déblocage de tous les comptes utilisateurs

## 📋 Description

Cette fonctionnalité permet de débloquer tous les comptes utilisateurs qui sont actuellement bloqués dans la base de données.

## 🚀 Méthodes d'utilisation

### 1. Via l'API REST (recommandé)

**Endpoint:** `PUT /admin/users/unblock-all`

**Requête:**
```bash
curl -X PUT http://localhost:3000/admin/users/unblock-all
```

**Réponse:**
```json
{
  "success": true,
  "message": "X compte(s) débloqué(s) avec succès",
  "unblockedCount": X
}
```

### 2. Via le script Node.js

**Exécuter le script:**
```bash
cd server
node unblock-all-users.js
```

Le script va:
- Se connecter à MongoDB
- Trouver tous les utilisateurs bloqués (`isBlocked: true`)
- Les débloquer (mettre `isBlocked: false` et `blockedUntil: null`)
- Afficher le nombre de comptes débloqués

## ⚙️ Fonctionnement

L'opération utilise `updateMany` de MongoDB pour mettre à jour tous les utilisateurs qui ont:
- `isBlocked: true`

Et les met à jour avec:
- `isBlocked: false`
- `blockedUntil: null`

## 🔒 Sécurité

⚠️ **Note:** Cette route devrait normalement être protégée par une authentification admin dans un environnement de production.

## 📊 Exemple de sortie

```
🔓 Déblocage de tous les comptes utilisateurs...
✅ 5 compte(s) débloqué(s)

📋 Utilisateurs débloqués:
   - Total d'utilisateurs dans la base: 50
   - Comptes débloqués: 5

✅ Opération terminée
```

## 🔄 Route API

La route est disponible dans `server.js`:
- **Méthode:** PUT
- **URL:** `/admin/users/unblock-all`
- **Description:** Débloque tous les comptes utilisateurs actuellement bloqués

