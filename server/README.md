# Serveur ISET Restaurant

## Démarrage du serveur

### Prérequis
1. **Node.js** doit être installé (version 14 ou supérieure)
2. **MongoDB** doit être installé et démarré sur `mongodb://127.0.0.1:27017`

### Installation des dépendances
```bash
npm install
```

### Démarrage du serveur

**Option 1: Utiliser le script batch (Windows)**
```bash
start-server.bat
```

**Option 2: Utiliser Node.js directement**
```bash
node server.js
```

**Option 3: Utiliser npm**
```bash
npm start
```

### Vérification que le serveur fonctionne

Le serveur devrait afficher:
```
✅ Connecté à MongoDB
🚀 Serveur démarré sur http://localhost:3000
📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:3000
```

### Routes disponibles

- `POST /register` - Inscription
- `POST /login` - Connexion
- `GET /menus` - Liste des menus
- `POST /meal-reservations` - Réservation déjeuner/dîner
- `POST /cold-meal-reservations` - Réservation repas froid
- `GET /cold-meal-reservations/user/:studentId` - Réservations repas froid d'un utilisateur

### Problèmes courants

#### Erreur: "Route non trouvée (404)"
- Vérifiez que le serveur est démarré
- Vérifiez que MongoDB est démarré
- Vérifiez que vous utilisez la bonne URL dans l'application Android

#### Erreur: "Impossible de se connecter au serveur"
- Si vous testez sur un **émulateur Android**: utilisez `http://10.0.2.2:3000`
- Si vous testez sur un **appareil physique**: utilisez l'adresse IP locale de votre PC (ex: `http://192.168.1.100:3000`)

#### MongoDB n'est pas connecté
- Démarrez MongoDB avec: `mongod` (ou via le service Windows)
- Vérifiez que MongoDB écoute sur le port 27017

### Configuration de l'application Android

Dans `MealReservationAPI.java`, l'URL est définie comme:
```java
private static final String BASE_URL = "http://10.0.2.2:3000"; // Pour émulateur Android
```

Pour un appareil physique, changez cette ligne avec l'adresse IP de votre PC.












