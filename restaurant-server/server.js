const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

// Initialisation
const app = express();

// ⚡ Middleware
app.use(cors());
app.use(express.json()); // Important : doit être avant les routes

// Middleware pour logger toutes les requêtes (avant les routes)
app.use((req, res, next) => {
  console.log(`📥 ${req.method} ${req.url}`);
  if (req.body && Object.keys(req.body).length > 0) {
    console.log(`   Body:`, JSON.stringify(req.body));
  }
  next();
});

// Connexion MongoDB
// Note: useNewUrlParser et useUnifiedTopology sont dépréciés depuis MongoDB Driver v4.0.0
mongoose.connect('mongodb://127.0.0.1:27017/restaurant')
  .then(() => {
    console.log('✅ Connecté à MongoDB');
    console.log('📊 Base de données: restaurant');
  })
  .catch(err => {
    console.error('❌ Erreur de connexion MongoDB :', err.message);
    console.error('⚠️  Assurez-vous que MongoDB est démarré sur mongodb://127.0.0.1:27017');
    console.error('⚠️  Le serveur continuera à fonctionner, mais les opérations de base de données échoueront.');
  });

/* -------------------------------------------------------------------
   SCHÉMAS ET MODÈLES
------------------------------------------------------------------- */

// Schéma Utilisateur
const userSchema = new mongoose.Schema({
  fullName: { type: String, required: true },
  email: { type: String, required: true, unique: true },
  studentId: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  phone: { type: String, default: '' },
  university: { type: String, default: 'ISET Tataouine' },
  role: { 
    type: String, 
    enum: ['etudiant', 'admin'], 
    default: 'etudiant' 
  },
  subscriptionBalance: { type: Number, default: 0.0 }, // Solde d'abonnement en DNT
  isBlocked: { type: Boolean, default: false }, // Statut de blocage
  blockedUntil: { type: Date, default: null }, // Date de fin de blocage
  createdAt: { type: Date, default: Date.now }
});

// Schéma Menu
const menuSchema = new mongoose.Schema({
  name: { type: String, required: true },
  appetizer: { type: String, required: true },
  mainCourse: { type: String, required: true },
  dessert: { type: String, required: true },
  drink: { type: String, default: '' }, // Optionnel
  price: { type: Number, default: 0.0 }, // Optionnel
  comment: { type: String, default: '' }, // Commentaire de l'administrateur
  available: { type: Boolean, default: true },
  date: { type: Date, default: Date.now }
});

// Schéma Commentaire d'étudiant sur un menu
const studentCommentSchema = new mongoose.Schema({
  menuId: { type: String, required: true }, // ID du menu
  studentId: { type: String, required: true }, // ID de l'étudiant
  userName: { type: String, required: true }, // Nom de l'étudiant
  comment: { type: String, required: true }, // Texte du commentaire
  createdAt: { type: Date, default: Date.now } // Date de publication
});

// Schéma Commande avec commentaire (pour les étudiants)
const orderCommentSchema = new mongoose.Schema({
  studentId: { type: String, required: true }, // ID de l'étudiant
  userName: { type: String, required: true }, // Nom de l'étudiant
  mealType: { 
    type: String, 
    required: true,
    enum: ['Déjeuner', 'Dîner', 'Repas froid']
  }, // Type de repas
  comment: { type: String, required: true }, // Commentaire de l'étudiant
  createdAt: { type: Date, default: Date.now } // Date de création
});

// Schéma Réservation de Repas (Déjeuner et Dîner uniquement)
const mealReservationSchema = new mongoose.Schema({
  userId: { type: String, required: true },
  userEmail: { type: String, required: true },
  userName: { type: String, required: true },
  studentId: { type: String, required: true },
  mealType: { 
    type: String, 
    required: true,
    validate: {
      validator: function(v) {
        return ['Déjeuner', 'Dîner'].includes(v);
      },
      message: 'mealType doit être: Déjeuner ou Dîner'
    }
  },
  price: { type: Number, required: true },
  reservationDate: { type: String, required: true },
  qrCode: { type: String },
  status: { type: String, enum: ['RESERVED', 'USED', 'CANCELLED'], default: 'RESERVED' },
  createdAt: { type: Date, default: Date.now }
});

// Schéma Réservation de Repas Froid (Collection séparée)
const coldMealReservationSchema = new mongoose.Schema({
  userId: { type: String, required: true },
  userEmail: { type: String, required: true },
  userName: { type: String, required: true },
  studentId: { type: String, required: true },
  mealType: { type: String, default: 'Repas Froid', required: true },
  price: { type: Number, required: true },
  reservationDate: { type: String, required: true },
  reservationDay: { type: String, default: 'Samedi' },
  reservationTime: { type: String, default: 'Soir' },
  qrCode: { type: String },
  status: { type: String, enum: ['RESERVED', 'USED', 'CANCELLED'], default: 'RESERVED' },
  createdAt: { type: Date, default: Date.now }
});

// Schéma Paiement (Historique des paiements)
const paymentSchema = new mongoose.Schema({
  studentId: { type: String, required: true },
  userEmail: { type: String, required: true },
  userName: { type: String, required: true },
  paymentType: { 
    type: String, 
    enum: ['subscription', 'reservation'], 
    required: true 
  },
  amount: { type: Number, required: true },
  description: { type: String, default: '' },
  balanceBefore: { type: Number, required: true }, // Solde avant le paiement
  balanceAfter: { type: Number, required: true }, // Solde après le paiement
  paymentMethod: { type: String, default: 'carte_virtuelle' },
  status: { 
    type: String, 
    enum: ['SUCCESS', 'FAILED', 'PENDING', 'CANCELLED'], 
    default: 'SUCCESS' 
  },
  paymentGateway: { type: String, default: 'simulation' }, // simulation, flouci, stripe
  transactionId: { type: String, default: '' },
  createdAt: { type: Date, default: Date.now }
});

// Modèles - Supprimer les modèles existants pour forcer la mise à jour du schéma
// Cela évite les problèmes de cache avec Mongoose
if (mongoose.models.User) {
  delete mongoose.models.User;
  delete mongoose.connection.models.User;
}
if (mongoose.models.Menu) {
  delete mongoose.models.Menu;
  delete mongoose.connection.models.Menu;
}
if (mongoose.models.MealReservation) {
  delete mongoose.models.MealReservation;
  delete mongoose.connection.models.MealReservation;
}
if (mongoose.models.ColdMealReservation) {
  delete mongoose.models.ColdMealReservation;
  delete mongoose.connection.models.ColdMealReservation;
}
if (mongoose.models.Payment) {
  delete mongoose.models.Payment;
  delete mongoose.connection.models.Payment;
}
if (mongoose.models.StudentComment) {
  delete mongoose.models.StudentComment;
  delete mongoose.connection.models.StudentComment;
}
if (mongoose.models.OrderComment) {
  delete mongoose.models.OrderComment;
  delete mongoose.connection.models.OrderComment;
}

// Recréer les modèles avec les nouveaux schémas
const User = mongoose.model('User', userSchema);
const Menu = mongoose.model('Menu', menuSchema);
const MealReservation = mongoose.model('MealReservation', mealReservationSchema);
// Modèle pour les repas froids - collection séparée
const ColdMealReservation = mongoose.model('ColdMealReservation', coldMealReservationSchema, 'coldmealreservations');
// Modèle pour l'historique des paiements
const Payment = mongoose.model('Payment', paymentSchema, 'payments');
// Modèle pour les commentaires des étudiants
const StudentComment = mongoose.model('StudentComment', studentCommentSchema, 'studentcomments');
const OrderComment = mongoose.model('OrderComment', orderCommentSchema, 'ordercomments');

console.log('📋 Modèles Mongoose initialisés avec les schémas mis à jour');
console.log('   - MealReservation: pour Déjeuner et Dîner');
console.log('   - ColdMealReservation: pour Repas Froid (collection séparée)');
console.log('   - Payment: pour l\'historique des paiements (collection: payments)');

/* -------------------------------------------------------------------
   ROUTES
------------------------------------------------------------------- */

// Test serveur
app.get('/', (req, res) => {
  res.send('🌍 Serveur Node.js opérationnel et connecté à MongoDB !');
});

// Route de test simple pour vérifier la connexion depuis l'app Android
app.get('/test-connection', (req, res) => {
  res.json({ 
    success: true, 
    message: 'Connexion réussie !',
    timestamp: new Date().toISOString(),
    server: 'Node.js',
    port: 3000
  });
});

// Route de test pour POST
app.post('/test-connection', (req, res) => {
  console.log('📥 Test de connexion POST reçu:', req.body);
  res.json({ 
    success: true, 
    message: 'Connexion POST réussie !',
    receivedData: req.body,
    timestamp: new Date().toISOString()
  });
});

/* -------------------
   AUTHENTIFICATION
------------------- */

app.post('/register', async (req, res) => {
  try {
    const { fullName, email, studentId, password, role, adminCode } = req.body;
    if (!fullName || !email || !password)
      return res.status(400).json({ success: false, message: 'Tous les champs sont requis' });

    // Valider le rôle
    const validRole = role && ['etudiant', 'admin'].includes(role) ? role : 'etudiant';
    
    // Si c'est un admin, valider le code admin
    if (validRole === 'admin') {
      const ADMIN_SECRET_CODE = 'ADMIN2024'; // Code secret pour créer un compte admin
      if (!adminCode || adminCode !== ADMIN_SECRET_CODE) {
        return res.status(403).json({ 
          success: false, 
          message: 'Code administrateur invalide. Accès refusé.' 
        });
      }
      // Pour les admins, générer un studentId unique basé sur l'email
      const generatedStudentId = 'ADMIN_' + email.replace('@', '_').replace(/\./g, '_');
      
      const existingEmail = await User.findOne({ email });
      if (existingEmail) return res.status(409).json({ success: false, message: 'Email déjà utilisé' });

      const newUser = new User({ fullName, email, studentId: generatedStudentId, password, role: validRole });
      await newUser.save();

      const userResponse = {
        fullName: newUser.fullName,
        email: newUser.email,
        studentId: newUser.studentId,
        phone: newUser.phone,
        university: newUser.university,
        role: newUser.role
      };

      return res.status(201).json(userResponse);
    } else {
      // Pour les étudiants, valider l'ID étudiant
      if (!studentId) {
        return res.status(400).json({ success: false, message: 'ID étudiant requis' });
      }

      const existingEmail = await User.findOne({ email });
      if (existingEmail) return res.status(409).json({ success: false, message: 'Email déjà utilisé' });

      const existingStudentId = await User.findOne({ studentId });
      if (existingStudentId) return res.status(409).json({ success: false, message: 'ID étudiant déjà utilisé' });

      const newUser = new User({ fullName, email, studentId, password, role: validRole });
      await newUser.save();

      const userResponse = {
        fullName: newUser.fullName,
        email: newUser.email,
        studentId: newUser.studentId,
        phone: newUser.phone,
        university: newUser.university,
        role: newUser.role
      };

      return res.status(201).json(userResponse);
    }
  } catch (error) {
    console.error('❌ Erreur lors de l\'inscription:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

app.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Email et mot de passe requis' });
    }

    const user = await User.findOne({ email, password });
    if (!user) {
      return res.status(401).json({ success: false, message: 'Email ou mot de passe incorrect' });
    }

    // Vérifier si l'utilisateur est bloqué
    if (user.isBlocked && user.blockedUntil) {
      const now = new Date();
      const blockedUntil = new Date(user.blockedUntil);
      
      // Si la date de blocage est dans le futur, l'utilisateur est toujours bloqué
      if (blockedUntil > now) {
        const daysRemaining = Math.ceil((blockedUntil - now) / (1000 * 60 * 60 * 24));
        console.log('🔒 Tentative de connexion d\'un utilisateur bloqué:', {
          email: user.email,
          blockedUntil: blockedUntil,
          daysRemaining: daysRemaining
        });
        
        return res.status(403).json({ 
          success: false, 
          message: `Votre compte est bloqué jusqu'au ${blockedUntil.toLocaleDateString('fr-FR')}. Jours restants: ${daysRemaining}`,
          isBlocked: true,
          blockedUntil: blockedUntil.toISOString(),
          daysRemaining: daysRemaining
        });
      } else {
        // Le blocage a expiré, débloquer automatiquement
        console.log('🔓 Blocage expiré, déblocage automatique pour:', user.email);
        user.isBlocked = false;
        user.blockedUntil = null;
        await user.save();
      }
    } else if (user.isBlocked && !user.blockedUntil) {
      // Cas où isBlocked est true mais blockedUntil est null (ancien format)
      console.log('🔓 Déblocage automatique (blockedUntil manquant) pour:', user.email);
      user.isBlocked = false;
      await user.save();
    }

    // Log pour déboguer le rôle
    console.log('🔍 Utilisateur trouvé:', {
      email: user.email,
      role: user.role,
      roleType: typeof user.role,
      studentId: user.studentId,
      isBlocked: user.isBlocked
    });

    // Si le studentId commence par "ADMIN_" ou contient "ADMIN", mettre le rôle à "admin"
    if (user.studentId && (user.studentId.startsWith('ADMIN_') || user.studentId.toUpperCase().includes('ADMIN'))) {
      if (user.role !== 'admin') {
        console.log('⚠️ Mise à jour du rôle pour un admin (studentId contient ADMIN)');
        user.role = 'admin';
        await user.save();
        console.log('✅ Rôle mis à jour à "admin"');
      }
    }

    // Générer un token simple (dans un vrai projet, utilisez JWT)
    const token = Buffer.from(`${user._id}:${Date.now()}`).toString('base64');

    // S'assurer que le rôle est bien défini
    const userRole = user.role || 'etudiant';
    console.log('📤 Rôle envoyé dans la réponse:', userRole);

    // Retourner la réponse dans le format attendu par UserResponse
    const userResponse = {
      success: true,
      message: 'Connexion réussie',
      token: token,
      user: {
        fullName: user.fullName,
        email: user.email,
        studentId: user.studentId,
        phone: user.phone || '',
        university: user.university || 'ISET Tataouine',
        role: userRole
      }
    };

    console.log('📤 Réponse complète:', JSON.stringify(userResponse, null, 2));
    res.status(200).json(userResponse);
  } catch (error) {
    console.error('❌ Erreur lors de la connexion:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

/* -------------------
   MENUS
------------------- */

// Récupérer tous les menus (pour admin, inclut les menus non disponibles)
app.get('/menus', async (req, res) => {
  try {
    const { includeUnavailable } = req.query;
    
    // Obtenir la date du jour (début et fin de journée)
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Début de la journée
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1); // Début du jour suivant
    
    console.log('📅 Filtrage des menus pour aujourd\'hui:', {
      today: today.toISOString(),
      tomorrow: tomorrow.toISOString()
    });
    
    let query = {};
    
    // Filtrer par date du jour
    query.date = {
      $gte: today,
      $lt: tomorrow
    };
    
    // Filtrer par disponibilité si nécessaire
    if (includeUnavailable !== 'true') {
      query.available = true;
    }
    
    const menus = await Menu.find(query).sort({ date: -1 });
    
    console.log(`✅ ${menus.length} menu(s) trouvé(s) pour aujourd'hui`);
    
    // Convertir _id en id pour chaque menu
    const menusWithId = menus.map(menu => {
      const menuObj = menu.toObject();
      menuObj.id = menuObj._id;
      delete menuObj._id;
      return menuObj;
    });
    
    res.status(200).json({ success: true, menus: menusWithId });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des menus:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Créer un nouveau menu
app.post('/menus', async (req, res) => {
  try {
    const { name, appetizer, mainCourse, dessert, drink, price, comment } = req.body;

    console.log('📥 Requête de création de menu reçue:', { name, appetizer, mainCourse, dessert, drink, price, comment });

    if (!name || !appetizer || !mainCourse || !dessert) {
      console.error('❌ Champs manquants:', { name: !!name, appetizer: !!appetizer, mainCourse: !!mainCourse, dessert: !!dessert });
      return res.status(400).json({ 
        success: false, 
        message: 'Les champs requis sont: name, appetizer, mainCourse, dessert' 
      });
    }

    const newMenu = new Menu({
      name: name.trim(),
      appetizer: appetizer.trim(),
      mainCourse: mainCourse.trim(),
      dessert: dessert.trim(),
      drink: (drink && drink.trim()) || '', // Optionnel
      price: price !== undefined ? parseFloat(price) : 0.0, // Optionnel, défaut 0
      comment: (comment && comment.trim()) || '', // Commentaire de l'administrateur
      available: true
    });

    await newMenu.save();
    console.log('✅ Menu créé avec succès:', {
      id: newMenu._id,
      name: newMenu.name,
      appetizer: newMenu.appetizer,
      mainCourse: newMenu.mainCourse,
      dessert: newMenu.dessert
    });

    // Convertir _id en id pour la compatibilité avec l'app Android
    const menuResponse = newMenu.toObject();
    menuResponse.id = menuResponse._id.toString();
    delete menuResponse._id;

    res.status(201).json(menuResponse);
  } catch (error) {
    console.error('❌ Erreur lors de la création du menu:', error);
    console.error('   Détails:', error.message);
    console.error('   Stack:', error.stack);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur lors de la création du menu', 
      error: error.message,
      details: error.errors || null
    });
  }
});

// Mettre à jour un menu
app.put('/menus/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { name, appetizer, mainCourse, dessert, drink, price, comment, available } = req.body;

    const menu = await Menu.findById(id);
    if (!menu) {
      return res.status(404).json({ success: false, message: 'Menu non trouvé' });
    }

    if (name) menu.name = name;
    if (appetizer) menu.appetizer = appetizer;
    if (mainCourse) menu.mainCourse = mainCourse;
    if (dessert) menu.dessert = dessert;
    if (drink) menu.drink = drink;
    if (price !== undefined) menu.price = parseFloat(price);
    if (comment !== undefined) menu.comment = comment;
    if (available !== undefined) menu.available = available;

    await menu.save();
    console.log('✅ Menu mis à jour:', menu);

    // Convertir _id en id pour la compatibilité avec l'app Android
    const menuResponse = menu.toObject();
    menuResponse.id = menuResponse._id;
    delete menuResponse._id;

    res.status(200).json({ success: true, message: 'Menu mis à jour avec succès', menu: menuResponse });
  } catch (error) {
    console.error('❌ Erreur lors de la mise à jour du menu:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Supprimer un menu
app.delete('/menus/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const menu = await Menu.findByIdAndDelete(id);
    if (!menu) {
      return res.status(404).json({ success: false, message: 'Menu non trouvé' });
    }

    console.log('✅ Menu supprimé:', id);

    res.status(200).json({ success: true, message: 'Menu supprimé avec succès' });
  } catch (error) {
    console.error('❌ Erreur lors de la suppression du menu:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

/* -------------------
   COMMENTAIRES DES ÉTUDIANTS
------------------- */

// Créer un commentaire sur un menu
app.post('/menus/:menuId/comments', async (req, res) => {
  try {
    const { menuId } = req.params;
    const { studentId, userName, comment } = req.body;

    console.log('📝 Requête de création de commentaire reçue:', { menuId, studentId, userName });

    if (!studentId || !userName || !comment || comment.trim().length === 0) {
      return res.status(400).json({ 
        success: false, 
        message: 'studentId, userName et comment sont requis' 
      });
    }

    // Vérifier que l'utilisateur n'est pas un administrateur
    const user = await User.findOne({ studentId: studentId.trim() });
    if (user && user.role === 'admin') {
      console.log('🚫 Tentative de commentaire par un administrateur bloquée:', { studentId, userName });
      return res.status(403).json({ 
        success: false, 
        message: 'Les administrateurs ne peuvent pas ajouter de commentaires. Seuls les étudiants peuvent commenter.' 
      });
    }

    // Vérifier que le menu existe
    const menu = await Menu.findById(menuId);
    if (!menu) {
      return res.status(404).json({ success: false, message: 'Menu non trouvé' });
    }

    // Créer le commentaire
    const newComment = new StudentComment({
      menuId: menuId,
      studentId: studentId.trim(),
      userName: userName.trim(),
      comment: comment.trim()
    });

    await newComment.save();
    console.log('✅ Commentaire créé avec succès:', {
      id: newComment._id,
      menuId,
      studentId,
      userName
    });

    // Convertir _id en id pour la compatibilité avec l'app Android
    const commentResponse = newComment.toObject();
    commentResponse.id = commentResponse._id.toString();
    delete commentResponse._id;

    res.status(201).json({ 
      success: true, 
      message: 'Commentaire ajouté avec succès', 
      comment: commentResponse 
    });
  } catch (error) {
    console.error('❌ Erreur lors de la création du commentaire:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: error.message 
    });
  }
});

// Récupérer tous les commentaires d'un menu
app.get('/menus/:menuId/comments', async (req, res) => {
  try {
    const { menuId } = req.params;

    console.log('📋 Récupération des commentaires pour le menu:', menuId);

    const comments = await StudentComment.find({ menuId: menuId })
      .sort({ createdAt: -1 }); // Plus récents en premier

    console.log(`✅ ${comments.length} commentaire(s) trouvé(s) pour le menu ${menuId}`);

    // Convertir _id en id pour chaque commentaire
    const commentsWithId = comments.map(comment => {
      const commentObj = comment.toObject();
      commentObj.id = commentObj._id.toString();
      delete commentObj._id;
      return commentObj;
    });

    res.status(200).json({ 
      success: true, 
      comments: commentsWithId,
      count: commentsWithId.length
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des commentaires:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: error.message 
    });
  }
});

// Récupérer tous les commentaires (pour l'admin)
app.get('/comments', async (req, res) => {
  try {
    console.log('📋 Récupération de tous les commentaires');

    const comments = await StudentComment.find({})
      .sort({ createdAt: -1 }); // Plus récents en premier

    console.log(`✅ ${comments.length} commentaire(s) trouvé(s)`);

    // Convertir _id en id pour chaque commentaire
    const commentsWithId = comments.map(comment => {
      const commentObj = comment.toObject();
      commentObj.id = commentObj._id.toString();
      delete commentObj._id;
      return commentObj;
    });

    res.status(200).json({ 
      success: true, 
      comments: commentsWithId,
      count: commentsWithId.length
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération de tous les commentaires:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: error.message 
    });
  }
});

// Supprimer un commentaire (optionnel - pour l'admin ou l'auteur)
app.delete('/comments/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const comment = await StudentComment.findByIdAndDelete(id);
    if (!comment) {
      return res.status(404).json({ success: false, message: 'Commentaire non trouvé' });
    }

    console.log('✅ Commentaire supprimé:', id);

    res.status(200).json({ success: true, message: 'Commentaire supprimé avec succès' });
  } catch (error) {
    console.error('❌ Erreur lors de la suppression du commentaire:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

/* -------------------
   COMMANDES AVEC COMMENTAIRE
------------------- */

// Créer une commande avec commentaire
app.post('/orders/comment', async (req, res) => {
  try {
    const { studentId, userName, mealType, comment } = req.body;

    console.log('📝 Requête de création de commande avec commentaire:', { studentId, userName, mealType });

    // Vérifier les paramètres requis
    if (!studentId || !userName || !mealType || !comment || comment.trim().length === 0) {
      return res.status(400).json({ 
        success: false, 
        message: 'studentId, userName, mealType et comment sont requis' 
      });
    }

    // Vérifier que le type de repas est valide
    const validMealTypes = ['Déjeuner', 'Dîner', 'Repas froid'];
    if (!validMealTypes.includes(mealType)) {
      return res.status(400).json({ 
        success: false, 
        message: 'mealType doit être: Déjeuner, Dîner ou Repas froid' 
      });
    }

    // Vérifier que l'utilisateur n'est pas un administrateur
    const user = await User.findOne({ studentId: studentId.trim() });
    if (user && user.role === 'admin') {
      console.log('🚫 Tentative de commande par un administrateur bloquée:', { studentId, userName });
      return res.status(403).json({ 
        success: false, 
        message: 'Les administrateurs ne peuvent pas créer de commandes. Seuls les étudiants peuvent commander.' 
      });
    }

    // Créer la commande avec commentaire
    const newOrderComment = new OrderComment({
      studentId: studentId.trim(),
      userName: userName.trim(),
      mealType: mealType.trim(),
      comment: comment.trim()
    });

    await newOrderComment.save();
    console.log('✅ Commande avec commentaire créée avec succès:', {
      id: newOrderComment._id,
      studentId,
      userName,
      mealType
    });

    // Convertir _id en id pour la compatibilité avec l'app Android
    const orderResponse = newOrderComment.toObject();
    orderResponse.id = orderResponse._id.toString();
    delete orderResponse._id;

    res.status(201).json({ 
      success: true, 
      message: 'Commande créée avec succès', 
      order: orderResponse 
    });
  } catch (error) {
    console.error('❌ Erreur lors de la création de la commande:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: error.message 
    });
  }
});

// Récupérer toutes les commandes avec commentaires (pour l'admin)
app.get('/orders/comments', async (req, res) => {
  try {
    console.log('📋 Récupération de toutes les commandes avec commentaires');
    console.log('   URL:', req.url);
    console.log('   Method:', req.method);
    console.log('   Headers:', JSON.stringify(req.headers));

    // Vérifier que le modèle OrderComment existe
    if (!OrderComment) {
      console.error('❌ Modèle OrderComment non défini!');
      return res.status(500).json({ 
        success: false, 
        message: 'Erreur serveur: Modèle OrderComment non défini' 
      });
    }

    const orders = await OrderComment.find({})
      .sort({ createdAt: -1 }); // Plus récents en premier

    console.log(`✅ ${orders.length} commande(s) trouvée(s)`);

    // Convertir _id en id pour chaque commande
    const ordersWithId = orders.map(order => {
      const orderObj = order.toObject();
      orderObj.id = orderObj._id.toString();
      delete orderObj._id;
      return orderObj;
    });

    res.status(200).json({ 
      success: true, 
      orders: ordersWithId,
      count: ordersWithId.length
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des commandes:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: error.message 
    });
  }
});

/* -------------------
   RÉSERVATIONS DE REPAS
------------------- */

app.post('/meal-reservations', async (req, res) => {
  try {
    const { userId, userEmail, userName, studentId, mealType, price, reservationDate, qrCode } = req.body;

    console.log('📥 Requête de réservation reçue:', { mealType, studentId, reservationDate });

    // Validation du mealType
    const validMealTypes = ['Déjeuner', 'Dîner', 'Repas Froid'];
    if (!validMealTypes.includes(mealType)) {
      console.error('❌ Type de repas invalide:', mealType);
      return res.status(400).json({ 
        success: false, 
        message: `Type de repas invalide. Types acceptés: ${validMealTypes.join(', ')}` 
      });
    }

    const user = await User.findOne({ studentId });
    if (!user) return res.status(404).json({ success: false, message: 'Utilisateur non trouvé' });
    
    // Vérifier et déduire du solde d'abonnement
    const reservationPrice = parseFloat(price) || 0.2;
    const currentBalance = user.subscriptionBalance || 0;
    
    if (currentBalance < reservationPrice) {
      return res.status(400).json({ 
        success: false, 
        message: `Solde insuffisant. Solde actuel: ${currentBalance.toFixed(3)} DNT. Montant requis: ${reservationPrice.toFixed(3)} DNT`,
        currentBalance: currentBalance,
        requiredAmount: reservationPrice
      });
    }
    
    // Déduire du solde
    const newBalance = currentBalance - reservationPrice;
    user.subscriptionBalance = newBalance;
    await user.save();
    
    console.log('💰 Solde déduit:', {
      ancienSolde: currentBalance,
      montantDéduit: reservationPrice,
      nouveauSolde: newBalance
    });

    // Enregistrer le paiement dans l'historique
    console.log('💾 Création du document Payment pour la réservation...');
    const payment = new Payment({
      studentId: studentId,
      userEmail: userEmail,
      userName: userName,
      paymentType: 'reservation',
      amount: reservationPrice,
      description: `Réservation ${mealType} - ${reservationPrice.toFixed(3)} DNT`,
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      paymentMethod: 'carte_virtuelle',
      status: 'SUCCESS',
      paymentGateway: 'simulation'
    });
    
    console.log('💾 Données du paiement avant sauvegarde:', {
      studentId: payment.studentId,
      paymentType: payment.paymentType,
      amount: payment.amount,
      balanceBefore: payment.balanceBefore,
      balanceAfter: payment.balanceAfter
    });
    
    // Vérifier la connexion MongoDB avant de sauvegarder
    if (mongoose.connection.readyState !== 1) {
      console.error('❌ MongoDB n\'est pas connecté! État:', mongoose.connection.readyState);
      throw new Error('MongoDB n\'est pas connecté');
    }
    
    try {
      await payment.save();
      console.log('✅ Paiement enregistré dans la collection payments:', {
        paymentId: payment._id,
        studentId,
        mealType,
        montant: reservationPrice,
        ancienSolde: currentBalance,
        nouveauSolde: newBalance,
        collection: 'payments'
      });
    } catch (saveError) {
      console.error('❌ ERREUR lors de la sauvegarde du paiement:', saveError);
      console.error('   Détails de l\'erreur:', {
        name: saveError.name,
        message: saveError.message,
        errors: saveError.errors,
        stack: saveError.stack
      });
      // Ne pas bloquer la réservation si le paiement ne peut pas être enregistré
      // mais logger l'erreur pour le débogage
      console.warn('⚠️ La réservation sera créée mais le paiement n\'a pas pu être enregistré dans l\'historique');
    }

    // Créer la réservation avec validation explicite
    const reservationData = {
      userId,
      userEmail,
      userName,
      studentId,
      mealType,
      price,
      reservationDate,
      qrCode: qrCode || `Type: ${mealType}\nDate: ${reservationDate}`,
      status: 'RESERVED'
    };

    console.log('💾 Données de réservation:', reservationData);

    // Créer la réservation en désactivant la validation stricte pour éviter les problèmes de cache
    const newReservation = new MealReservation(reservationData);
    
    // Sauvegarder avec runValidators: true pour forcer la validation
    await newReservation.save({ validateBeforeSave: true });
    console.log('✅ Réservation créée avec succès:', {
      id: newReservation._id,
      mealType,
      studentId,
      reservationDate,
      price
    });
    res.status(201).json({ 
      success: true, 
      message: 'Réservation créée avec succès', 
      reservation: newReservation,
      subscriptionBalance: newBalance
    });
  } catch (error) {
    console.error('❌ Erreur lors de la création de la réservation:', error);
    // Retourner un message d'erreur plus détaillé
    const errorMessage = error.message || 'Erreur inconnue';
    const errorDetails = error.errors ? Object.keys(error.errors).map(key => ({
      field: key,
      message: error.errors[key].message
    })) : null;
    
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: errorMessage,
      details: errorDetails
    });
  }
});

// Récupérer les réservations d'un utilisateur (uniquement celles d'aujourd'hui)
app.get('/meal-reservations/user/:studentId', async (req, res) => {
  try {
    // Obtenir la date d'aujourd'hui au format YYYY-MM-DD
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0]; // Format: YYYY-MM-DD
    
    console.log('📅 Récupération des réservations pour aujourd\'hui:', todayStr);
    
    // Récupérer toutes les réservations de l'utilisateur
    const allReservations = await MealReservation.find({ studentId: req.params.studentId });
    
    // Filtrer uniquement les réservations d'aujourd'hui
    const todayReservations = [];
    const reservationsToDelete = [];
    
    // Créer une date de référence pour minuit aujourd'hui
    const todayMidnight = new Date(today);
    todayMidnight.setHours(0, 0, 0, 0);
    
    for (const reservation of allReservations) {
      if (!reservation.reservationDate) {
        // Si pas de date, on garde (pour éviter de supprimer par erreur)
        continue;
      }
      
      // Parser la date de réservation (peut être au format "EEEE dd/MM/yyyy" ou "yyyy-MM-dd")
      let reservationDate = null;
      const reservationDateStr = reservation.reservationDate;
      
      // Essayer de parser avec différents formats
      // Format 1: "EEEE dd/MM/yyyy" (ex: "samedi 29/11/2025")
      const dayNameMatch = reservationDateStr.match(/^\w+\s+(\d{2})\/(\d{2})\/(\d{4})/);
      if (dayNameMatch) {
        const [, day, month, year] = dayNameMatch;
        reservationDate = new Date(`${year}-${month}-${day}`);
      } else {
        // Format 2: "yyyy-MM-dd" ou "yyyy-MM-dd HH:mm:ss"
        const datePart = reservationDateStr.split(' ')[0];
        reservationDate = new Date(datePart);
      }
      
      if (isNaN(reservationDate.getTime())) {
        console.warn(`⚠️ Format de date invalide pour la réservation ${reservation._id}: ${reservation.reservationDate}`);
        continue;
      }
      
      reservationDate.setHours(0, 0, 0, 0);
      
      // Comparer avec aujourd'hui
      if (reservationDate.getTime() === todayMidnight.getTime()) {
        // Réservation d'aujourd'hui
        todayReservations.push(reservation);
      } else if (reservationDate < todayMidnight) {
        // Si la date est avant aujourd'hui, marquer pour suppression
        reservationsToDelete.push(reservation._id);
        console.log(`🗑️ Réservation passée détectée: ${reservation.reservationDate} (ID: ${reservation._id})`);
      }
    }
    
    // Supprimer les réservations passées
    if (reservationsToDelete.length > 0) {
      await MealReservation.deleteMany({ _id: { $in: reservationsToDelete } });
      console.log(`🗑️ ${reservationsToDelete.length} réservation(s) passée(s) supprimée(s)`);
    }
    
    // Trier par date de création (plus récentes en premier)
    todayReservations.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    
    console.log(`✅ ${todayReservations.length} réservation(s) trouvée(s) pour aujourd'hui`);
    
    res.status(200).json({ success: true, reservations: todayReservations });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des réservations:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Annuler une réservation
app.put('/meal-reservations/:id/cancel', async (req, res) => {
  try {
    const reservation = await MealReservation.findById(req.params.id);
    if (!reservation) return res.status(404).json({ success: false, message: 'Réservation non trouvée' });

    if (reservation.status === 'USED') 
      return res.status(400).json({ success: false, message: 'Déjà utilisée' });

    reservation.status = 'CANCELLED';
    await reservation.save();
    res.status(200).json({ success: true, message: 'Réservation annulée', reservation });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Erreur serveur', error });
  }
});

// Marquer comme utilisée (scan QR)
app.put('/meal-reservations/:id/use', async (req, res) => {
  try {
    const { id } = req.params;

    // Validation de l'ID
    if (!id || id.length !== 24) {
      return res.status(400).json({ 
        success: false, 
        message: 'ID de réservation invalide' 
      });
    }

    console.log(`📥 Requête de scan QR reçue pour la réservation: ${id}`);

    // Rechercher la réservation
    const reservation = await MealReservation.findById(id);
    
    if (!reservation) {
      console.error(`❌ Réservation non trouvée: ${id}`);
      return res.status(404).json({ 
        success: false, 
        message: 'Réservation non trouvée' 
      });
    }

    // Vérifier le statut de la réservation
    if (reservation.status === 'USED') {
      console.warn(`⚠️ Tentative d'utilisation d'une réservation déjà utilisée: ${id}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Cette réservation a déjà été utilisée',
        reservation 
      });
    }

    if (reservation.status === 'CANCELLED') {
      console.warn(`⚠️ Tentative d'utilisation d'une réservation annulée: ${id}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Cette réservation a été annulée et ne peut pas être utilisée',
        reservation 
      });
    }

    if (reservation.status !== 'RESERVED') {
      console.warn(`⚠️ Statut de réservation invalide: ${reservation.status} pour ${id}`);
      return res.status(400).json({ 
        success: false, 
        message: `Impossible d'utiliser cette réservation. Statut actuel: ${reservation.status}`,
        reservation 
      });
    }

    // Marquer comme utilisée
    reservation.status = 'USED';
    await reservation.save();

    console.log(`✅ Réservation marquée comme utilisée:`, {
      id: reservation._id,
      studentId: reservation.studentId,
      mealType: reservation.mealType,
      reservationDate: reservation.reservationDate,
      status: reservation.status
    });

    res.status(200).json({ 
      success: true, 
      message: 'Réservation utilisée avec succès', 
      reservation 
    });
  } catch (error) {
    console.error('❌ Erreur lors du marquage de la réservation comme utilisée:', error);
    
    // Gestion d'erreur plus détaillée
    if (error.name === 'CastError') {
      return res.status(400).json({ 
        success: false, 
        message: 'Format d\'ID invalide',
        error: 'L\'ID fourni n\'est pas un format MongoDB valide'
      });
    }

    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur lors du traitement de la réservation',
      error: error.message 
    });
  }
});

/* -------------------
   RÉSERVATIONS DE REPAS FROID (Collection séparée)
------------------- */

app.post('/cold-meal-reservations', async (req, res) => {
  try {
    const { userId, userEmail, userName, studentId, mealType, price, reservationDate, qrCode } = req.body;

    console.log('📥 Requête de réservation repas froid reçue:', { mealType, studentId, reservationDate });

    const user = await User.findOne({ studentId });
    if (!user) return res.status(404).json({ success: false, message: 'Utilisateur non trouvé' });
    
    // Vérifier et déduire du solde d'abonnement
    const reservationPrice = parseFloat(price) || 0.2;
    const currentBalance = user.subscriptionBalance || 0;
    
    if (currentBalance < reservationPrice) {
      return res.status(400).json({ 
        success: false, 
        message: `Solde insuffisant. Solde actuel: ${currentBalance.toFixed(3)} DNT. Montant requis: ${reservationPrice.toFixed(3)} DNT`,
        currentBalance: currentBalance,
        requiredAmount: reservationPrice
      });
    }
    
    // Déduire du solde
    const newBalance = currentBalance - reservationPrice;
    user.subscriptionBalance = newBalance;
    await user.save();
    
    console.log('💰 Solde déduit (repas froid):', {
      ancienSolde: currentBalance,
      montantDéduit: reservationPrice,
      nouveauSolde: newBalance
    });

    // Enregistrer le paiement dans l'historique
    console.log('💾 Création du document Payment pour la réservation repas froid...');
    const payment = new Payment({
      studentId: studentId,
      userEmail: userEmail,
      userName: userName,
      paymentType: 'reservation',
      amount: reservationPrice,
      description: `Réservation Repas Froid - ${reservationPrice.toFixed(3)} DNT`,
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      paymentMethod: 'carte_virtuelle',
      status: 'SUCCESS',
      paymentGateway: 'simulation'
    });
    
    console.log('💾 Données du paiement avant sauvegarde:', {
      studentId: payment.studentId,
      paymentType: payment.paymentType,
      amount: payment.amount,
      balanceBefore: payment.balanceBefore,
      balanceAfter: payment.balanceAfter
    });
    
    // Vérifier la connexion MongoDB avant de sauvegarder
    if (mongoose.connection.readyState !== 1) {
      console.error('❌ MongoDB n\'est pas connecté! État:', mongoose.connection.readyState);
      throw new Error('MongoDB n\'est pas connecté');
    }
    
    try {
      await payment.save();
      console.log('✅ Paiement enregistré dans la collection payments:', {
        paymentId: payment._id,
        studentId,
        mealType: 'Repas Froid',
        montant: reservationPrice,
        ancienSolde: currentBalance,
        nouveauSolde: newBalance,
        collection: 'payments'
      });
    } catch (saveError) {
      console.error('❌ ERREUR lors de la sauvegarde du paiement:', saveError);
      console.error('   Détails de l\'erreur:', {
        name: saveError.name,
        message: saveError.message,
        errors: saveError.errors,
        stack: saveError.stack
      });
      // Ne pas bloquer la réservation si le paiement ne peut pas être enregistré
      // mais logger l'erreur pour le débogage
      console.warn('⚠️ La réservation sera créée mais le paiement n\'a pas pu être enregistré dans l\'historique');
    }

    // Créer la réservation de repas froid
    const reservationData = {
      userId,
      userEmail,
      userName,
      studentId,
      mealType: mealType || 'Repas Froid',
      price,
      reservationDate,
      reservationDay: 'Samedi',
      reservationTime: 'Soir',
      qrCode: qrCode || `Type: Repas Froid\nDate: ${reservationDate}\nJour: Samedi Soir`,
      status: 'RESERVED'
    };

    console.log('💾 Données de réservation repas froid:', reservationData);

    const newReservation = new ColdMealReservation(reservationData);
    await newReservation.save({ validateBeforeSave: true });
    
    console.log('✅ Réservation repas froid créée avec succès dans la collection séparée:', {
      id: newReservation._id,
      mealType: newReservation.mealType,
      studentId,
      reservationDate,
      price,
      collection: 'coldmealreservations'
    });
    
    res.status(201).json({ 
      success: true, 
      message: 'Réservation repas froid créée avec succès', 
      reservation: newReservation,
      subscriptionBalance: newBalance
    });
  } catch (error) {
    console.error('❌ Erreur lors de la création de la réservation repas froid:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: error.message 
    });
  }
});

// Récupérer les réservations de repas froid d'un utilisateur (uniquement celles d'aujourd'hui)
app.get('/cold-meal-reservations/user/:studentId', async (req, res) => {
  try {
    console.log('📅 Récupération des réservations repas froid pour studentId:', req.params.studentId);
    
    // Récupérer toutes les réservations de l'utilisateur (non utilisées et non annulées)
    const allReservations = await ColdMealReservation.find({ 
      studentId: req.params.studentId,
      status: { $nin: ['USED', 'CANCELLED', 'CANCELED', 'EXPIRED'] }
    });
    
    // Filtrer les réservations valides (non utilisées et non annulées)
    const validReservations = [];
    const reservationsToDelete = [];
    
    // Créer une date de référence pour minuit aujourd'hui
    const today = new Date();
    const todayMidnight = new Date(today);
    todayMidnight.setHours(0, 0, 0, 0);
    
    for (const reservation of allReservations) {
      // Vérifier le statut
      const status = reservation.status || 'RESERVED';
      
      // Ignorer les réservations utilisées, annulées ou expirées
      if (['USED', 'CANCELLED', 'CANCELED', 'EXPIRED'].includes(status.toUpperCase())) {
        continue;
      }
      
      // Si pas de date, on garde la réservation (pour éviter de supprimer par erreur)
      if (!reservation.reservationDate) {
        validReservations.push(reservation);
        continue;
      }
      
      // Parser la date de réservation (peut être au format "EEEE dd/MM/yyyy" ou "yyyy-MM-dd")
      let reservationDate = null;
      const reservationDateStr = reservation.reservationDate;
      
      // Essayer de parser avec différents formats
      // Format 1: "EEEE dd/MM/yyyy" (ex: "samedi 29/11/2025")
      const dayNameMatch = reservationDateStr.match(/^\w+\s+(\d{2})\/(\d{2})\/(\d{4})/);
      if (dayNameMatch) {
        const [, day, month, year] = dayNameMatch;
        reservationDate = new Date(`${year}-${month}-${day}`);
      } else {
        // Format 2: "yyyy-MM-dd" ou "yyyy-MM-dd HH:mm:ss"
        const datePart = reservationDateStr.split(' ')[0];
        reservationDate = new Date(datePart);
      }
      
      if (isNaN(reservationDate.getTime())) {
        console.warn(`⚠️ Format de date invalide pour la réservation repas froid ${reservation._id}: ${reservation.reservationDate}`);
        // Garder la réservation même si la date est invalide
        validReservations.push(reservation);
        continue;
      }
      
      reservationDate.setHours(0, 0, 0, 0);
      
      // Garder toutes les réservations valides (même futures, car les repas froids sont pour le samedi)
      // Supprimer uniquement celles qui sont passées ET utilisées
      if (reservationDate < todayMidnight && status === 'USED') {
        // Si la date est passée ET utilisée, marquer pour suppression
        reservationsToDelete.push(reservation._id);
        console.log(`🗑️ Réservation repas froid passée et utilisée détectée: ${reservation.reservationDate} (ID: ${reservation._id})`);
      } else {
        // Garder toutes les autres réservations valides (futures ou non utilisées)
        validReservations.push(reservation);
      }
    }
    
    // Supprimer les réservations passées et utilisées
    if (reservationsToDelete.length > 0) {
      await ColdMealReservation.deleteMany({ _id: { $in: reservationsToDelete } });
      console.log(`🗑️ ${reservationsToDelete.length} réservation(s) repas froid passée(s) et utilisée(s) supprimée(s)`);
    }
    
    // Trier par date de création (plus récentes en premier)
    validReservations.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    
    console.log(`✅ ${validReservations.length} réservation(s) repas froid valide(s) trouvée(s)`);
    
    res.status(200).json({ 
      success: true, 
      reservations: validReservations.map(r => ({
        id: r._id.toString(),
        _id: r._id.toString(),
        userId: r.userId,
        userEmail: r.userEmail,
        userName: r.userName,
        studentId: r.studentId,
        mealType: r.mealType || 'Repas Froid',
        menuName: r.mealType || 'Repas Froid',
        date: r.reservationDate,
        reservationDate: r.reservationDate,
        time: r.time || '18:00',
        price: r.price,
        totalPrice: r.price,
        numberOfTickets: r.numberOfTickets || 1,
        status: r.status || 'RESERVED',
        qrCode: r.qrCode,
        createdAt: r.createdAt
      }))
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des réservations repas froid:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Marquer comme utilisée (scan QR) - Repas Froid
app.put('/cold-meal-reservations/:id/use', async (req, res) => {
  try {
    const { id } = req.params;

    // Validation de l'ID
    if (!id || id.length !== 24) {
      return res.status(400).json({ 
        success: false, 
        message: 'ID de réservation invalide' 
      });
    }

    console.log(`📥 Requête de scan QR repas froid reçue pour la réservation: ${id}`);

    // Rechercher la réservation
    const reservation = await ColdMealReservation.findById(id);
    
    if (!reservation) {
      console.error(`❌ Réservation repas froid non trouvée: ${id}`);
      return res.status(404).json({ 
        success: false, 
        message: 'Réservation non trouvée' 
      });
    }

    // Vérifier le statut de la réservation
    if (reservation.status === 'USED') {
      console.warn(`⚠️ Tentative d'utilisation d'une réservation repas froid déjà utilisée: ${id}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Cette réservation a déjà été utilisée',
        reservation 
      });
    }

    if (reservation.status === 'CANCELLED') {
      console.warn(`⚠️ Tentative d'utilisation d'une réservation repas froid annulée: ${id}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Cette réservation a été annulée et ne peut pas être utilisée',
        reservation 
      });
    }

    if (reservation.status !== 'RESERVED') {
      console.warn(`⚠️ Statut de réservation repas froid invalide: ${reservation.status} pour ${id}`);
      return res.status(400).json({ 
        success: false, 
        message: `Impossible d'utiliser cette réservation. Statut actuel: ${reservation.status}`,
        reservation 
      });
    }

    // Marquer comme utilisée
    reservation.status = 'USED';
    await reservation.save();

    console.log(`✅ Réservation repas froid marquée comme utilisée:`, {
      id: reservation._id,
      studentId: reservation.studentId,
      mealType: reservation.mealType,
      reservationDate: reservation.reservationDate,
      status: reservation.status
    });

    res.status(200).json({ 
      success: true, 
      message: 'Réservation repas froid utilisée avec succès', 
      reservation 
    });
  } catch (error) {
    console.error('❌ Erreur lors du marquage de la réservation repas froid comme utilisée:', error);
    
    // Gestion d'erreur plus détaillée
    if (error.name === 'CastError') {
      return res.status(400).json({ 
        success: false, 
        message: 'Format d\'ID invalide',
        error: 'L\'ID fourni n\'est pas un format MongoDB valide'
      });
    }

    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur lors du traitement de la réservation repas froid',
      error: error.message 
    });
  }
});

// Annuler une réservation de repas froid
app.put('/cold-meal-reservations/:id/cancel', async (req, res) => {
  try {
    const reservation = await ColdMealReservation.findById(req.params.id);
    if (!reservation) return res.status(404).json({ success: false, message: 'Réservation non trouvée' });

    if (reservation.status === 'USED') 
      return res.status(400).json({ success: false, message: 'Déjà utilisée' });

    reservation.status = 'CANCELLED';
    await reservation.save();
    res.status(200).json({ success: true, message: 'Réservation annulée', reservation });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Erreur serveur', error });
  }
});

/* -------------------
   GESTION DES UTILISATEURS (ADMIN)
------------------- */

// Route pour récupérer tous les utilisateurs étudiants uniquement (admin uniquement)
app.get('/admin/users', async (req, res) => {
  try {
    console.log('📊 Récupération de la liste des utilisateurs étudiants...');
    
    // Filtrer uniquement les utilisateurs avec le rôle "etudiant"
    const users = await User.find({ role: 'etudiant' }).select('-password').sort({ createdAt: -1 });
    
    console.log(`✅ ${users.length} utilisateur(s) étudiant(s) trouvé(s)`);
    
    // Convertir _id en id pour chaque utilisateur
    const usersWithId = users.map(user => {
      const userObj = user.toObject();
      userObj.id = userObj._id.toString();
      delete userObj._id;
      return userObj;
    });
    
    res.status(200).json({ 
      success: true, 
      users: usersWithId,
      count: usersWithId.length
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des utilisateurs:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route pour récupérer les statistiques des utilisateurs avec abonnement (admin uniquement)
app.get('/admin/users/stats', async (req, res) => {
  try {
    console.log('📊 Récupération des statistiques des utilisateurs...');
    
    // Récupérer tous les utilisateurs étudiants
    const allStudents = await User.find({ role: 'etudiant' });
    
    // Compter les utilisateurs avec abonnement payé (subscriptionBalance > 0)
    let usersWithSubscription = 0;
    let totalSubscriptionBalance = 0.0;
    
    for (const user of allStudents) {
      const balance = user.subscriptionBalance || 0;
      if (balance > 0) {
        usersWithSubscription++;
        totalSubscriptionBalance += balance;
      }
    }
    
    console.log('✅ Statistiques utilisateurs calculées:', {
      totalStudents: allStudents.length,
      usersWithSubscription: usersWithSubscription,
      totalSubscriptionBalance: totalSubscriptionBalance
    });
    
    res.status(200).json({
      success: true,
      stats: {
        totalStudents: allStudents.length,
        usersWithSubscription: usersWithSubscription,
        usersWithoutSubscription: allStudents.length - usersWithSubscription,
        totalSubscriptionBalance: totalSubscriptionBalance
      }
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des statistiques utilisateurs:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route pour mettre à jour un utilisateur (admin uniquement)
app.put('/admin/users/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { fullName, email, phone, university, role, subscriptionBalance } = req.body;
    
    console.log('📝 Mise à jour de l\'utilisateur:', id);
    
    const user = await User.findById(id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Utilisateur non trouvé' });
    }
    
    // Mettre à jour les champs fournis
    if (fullName) user.fullName = fullName;
    if (email) user.email = email;
    if (phone !== undefined) user.phone = phone;
    if (university !== undefined) user.university = university;
    if (role && ['etudiant', 'admin'].includes(role)) user.role = role;
    if (subscriptionBalance !== undefined) user.subscriptionBalance = parseFloat(subscriptionBalance);
    
    await user.save();
    
    console.log('✅ Utilisateur mis à jour:', {
      id: user._id,
      fullName: user.fullName,
      email: user.email,
      role: user.role
    });
    
    // Convertir _id en id
    const userResponse = user.toObject();
    userResponse.id = userResponse._id.toString();
    delete userResponse._id;
    delete userResponse.password;
    
    res.status(200).json({ 
      success: true, 
      message: 'Utilisateur mis à jour avec succès', 
      user: userResponse 
    });
  } catch (error) {
    console.error('❌ Erreur lors de la mise à jour de l\'utilisateur:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route pour bloquer/débloquer un utilisateur (admin uniquement)
app.put('/admin/users/:id/block', async (req, res) => {
  try {
    const { id } = req.params;
    const { block } = req.body; // true pour bloquer, false pour débloquer
    
    console.log('🔒 Action de blocage/déblocage pour l\'utilisateur:', id, 'block:', block);
    
    const user = await User.findById(id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Utilisateur non trouvé' });
    }
    
    if (block === true || block === 'true') {
      // Bloquer pour un mois
      const blockedUntil = new Date();
      blockedUntil.setMonth(blockedUntil.getMonth() + 1); // Ajouter un mois
      
      user.isBlocked = true;
      user.blockedUntil = blockedUntil;
      
      console.log('🔒 Utilisateur bloqué jusqu\'au:', blockedUntil);
    } else {
      // Débloquer
      user.isBlocked = false;
      user.blockedUntil = null;
      
      console.log('🔓 Utilisateur débloqué');
    }
    
    await user.save();
    
    // Convertir _id en id
    const userResponse = user.toObject();
    userResponse.id = userResponse._id.toString();
    delete userResponse._id;
    delete userResponse.password;
    
    res.status(200).json({ 
      success: true, 
      message: block ? 'Utilisateur bloqué pour un mois' : 'Utilisateur débloqué', 
      user: userResponse 
    });
  } catch (error) {
    console.error('❌ Erreur lors du blocage/déblocage de l\'utilisateur:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route pour débloquer tous les comptes utilisateurs (admin uniquement)
app.put('/admin/users/unblock-all', async (req, res) => {
  try {
    console.log('🔓 Déblocage de tous les comptes utilisateurs...');
    
    const now = new Date();
    
    // Mettre à jour tous les utilisateurs bloqués (isBlocked: true OU blockedUntil dans le futur)
    const result = await User.updateMany(
      {
        $or: [
          { isBlocked: true },
          { blockedUntil: { $exists: true, $ne: null, $gt: now } }
        ]
      },
      { 
        $set: { 
          isBlocked: false,
          blockedUntil: null
        }
      }
    );
    
    console.log(`✅ ${result.modifiedCount} compte(s) débloqué(s)`);
    
    res.status(200).json({ 
      success: true, 
      message: `${result.modifiedCount} compte(s) débloqué(s) avec succès`,
      unblockedCount: result.modifiedCount
    });
  } catch (error) {
    console.error('❌ Erreur lors du déblocage de tous les comptes:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Erreur serveur', 
      error: error.message 
    });
  }
});

// Route pour supprimer un utilisateur (admin uniquement)
app.delete('/admin/users/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    console.log('🗑️ Suppression de l\'utilisateur:', id);
    
    const user = await User.findByIdAndDelete(id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Utilisateur non trouvé' });
    }
    
    console.log('✅ Utilisateur supprimé:', id);
    
    res.status(200).json({ 
      success: true, 
      message: 'Utilisateur supprimé avec succès' 
    });
  } catch (error) {
    console.error('❌ Erreur lors de la suppression de l\'utilisateur:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

/* -------------------
   STATISTIQUES ADMIN - RÉSERVATIONS ET REVENUS
------------------- */

// Route pour récupérer les statistiques des réservations par période (pour admin)
app.get('/admin/reservations/stats', async (req, res) => {
  try {
    const { period } = req.query; // 'day', 'week', 'month'
    const periodType = period || 'day';
    
    console.log('📊 Récupération des statistiques des réservations pour la période:', periodType);
    
    // Calculer les dates selon la période
    const now = new Date();
    let startDate, endDate;
    
    if (periodType === 'day') {
      startDate = new Date(now);
      startDate.setHours(0, 0, 0, 0);
      endDate = new Date(now);
      endDate.setHours(23, 59, 59, 999);
    } else if (periodType === 'week') {
      // Début de la semaine (lundi)
      startDate = new Date(now);
      const dayOfWeek = startDate.getDay();
      const diff = startDate.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1); // Ajuster pour lundi
      startDate.setDate(diff);
      startDate.setHours(0, 0, 0, 0);
      
      endDate = new Date(startDate);
      endDate.setDate(endDate.getDate() + 6);
      endDate.setHours(23, 59, 59, 999);
    } else if (periodType === 'month') {
      // Début du mois
      startDate = new Date(now.getFullYear(), now.getMonth(), 1);
      startDate.setHours(0, 0, 0, 0);
      
      // Fin du mois
      endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0);
      endDate.setHours(23, 59, 59, 999);
    } else {
      // Par défaut, jour
      startDate = new Date(now);
      startDate.setHours(0, 0, 0, 0);
      endDate = new Date(now);
      endDate.setHours(23, 59, 59, 999);
    }
    
    console.log('📅 Période:', {
      type: periodType,
      start: startDate.toISOString(),
      end: endDate.toISOString()
    });
    
    // Récupérer toutes les réservations normales
    const allMealReservations = await MealReservation.find({});
    
    // Récupérer toutes les réservations de repas froid
    const allColdReservations = await ColdMealReservation.find({});
    
    // Compter les réservations dans la période
    let dejeunerCount = 0;
    let dinerCount = 0;
    let repasFroidCount = 0;
    let totalRevenue = 0.0; // Revenus en DNT (200 millimes = 0.2 DNT par ticket)
    
    const ticketPrice = 0.2; // 200 millimes = 0.2 DNT
    
    // Filtrer les réservations normales dans la période
    for (const reservation of allMealReservations) {
      if (!reservation.reservationDate) continue;
      
      let reservationDate = null;
      const reservationDateStr = String(reservation.reservationDate);
      
      const dayNameMatch = reservationDateStr.match(/^\w+\s+(\d{2})\/(\d{2})\/(\d{4})/);
      if (dayNameMatch) {
        const [, day, month, year] = dayNameMatch;
        reservationDate = new Date(`${year}-${month}-${day}`);
      } else {
        const datePart = reservationDateStr.split(' ')[0];
        reservationDate = new Date(datePart);
      }
      
      if (isNaN(reservationDate.getTime())) continue;
      
      reservationDate.setHours(12, 0, 0, 0); // Milieu de journée pour comparaison
      
      // Vérifier si dans la période
      if (reservationDate >= startDate && reservationDate <= endDate) {
        const mealType = reservation.mealType;
        if (mealType === 'Déjeuner') {
          dejeunerCount++;
          totalRevenue += ticketPrice;
        } else if (mealType === 'Dîner') {
          dinerCount++;
          totalRevenue += ticketPrice;
        }
      }
    }
    
    // Filtrer les réservations repas froid dans la période
    for (const reservation of allColdReservations) {
      if (!reservation.reservationDate) continue;
      
      let reservationDate = null;
      const reservationDateStr = String(reservation.reservationDate);
      
      const dayNameMatch = reservationDateStr.match(/^\w+\s+(\d{2})\/(\d{2})\/(\d{4})/);
      if (dayNameMatch) {
        const [, day, month, year] = dayNameMatch;
        reservationDate = new Date(`${year}-${month}-${day}`);
      } else {
        const datePart = reservationDateStr.split(' ')[0];
        reservationDate = new Date(datePart);
      }
      
      if (isNaN(reservationDate.getTime())) continue;
      
      reservationDate.setHours(12, 0, 0, 0);
      
      // Vérifier si dans la période
      if (reservationDate >= startDate && reservationDate <= endDate) {
        repasFroidCount++;
        totalRevenue += ticketPrice;
      }
    }
    
    const total = dejeunerCount + dinerCount + repasFroidCount;
    
    console.log('✅ Statistiques calculées:', {
      period: periodType,
      dejeuner: dejeunerCount,
      diner: dinerCount,
      repasFroid: repasFroidCount,
      total: total,
      revenue: totalRevenue
    });
    
    res.status(200).json({
      success: true,
      period: periodType,
      startDate: startDate.toISOString(),
      endDate: endDate.toISOString(),
      stats: {
        dejeuner: dejeunerCount,
        diner: dinerCount,
        repasFroid: repasFroidCount,
        total: total,
        revenue: totalRevenue
      }
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des statistiques:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route pour récupérer les statistiques des réservations du jour (pour admin) - DEPRECATED, utiliser avec ?period=day
app.get('/admin/reservations/stats/old', async (req, res) => {
  try {
    console.log('📊 Récupération des statistiques des réservations du jour...');
    
    // Obtenir la date d'aujourd'hui
    const today = new Date();
    const todayMidnight = new Date(today);
    todayMidnight.setHours(0, 0, 0, 0);
    const todayStr = todayMidnight.toISOString().split('T')[0]; // Format: YYYY-MM-DD
    
    console.log('📅 Date d\'aujourd\'hui:', todayStr);
    
    // Récupérer TOUTES les réservations normales (Déjeuner et Dîner) - sans filtre
    const allMealReservations = await MealReservation.find({});
    console.log(`📋 Total réservations normales dans la base: ${allMealReservations.length}`);
    
    // Récupérer TOUTES les réservations de repas froid - sans filtre
    const allColdReservations = await ColdMealReservation.find({});
    console.log(`📋 Total réservations repas froid dans la base: ${allColdReservations.length}`);
    
    // Compter les réservations d'aujourd'hui par type
    let dejeunerCount = 0;
    let dinerCount = 0;
    let repasFroidCount = 0;
    let skippedMeal = 0;
    let skippedCold = 0;
    
    // Filtrer les réservations normales d'aujourd'hui
    for (const reservation of allMealReservations) {
      if (!reservation.reservationDate) {
        skippedMeal++;
        continue;
      }
      
      // Parser la date avec plusieurs formats possibles
      let reservationDate = null;
      const reservationDateStr = String(reservation.reservationDate);
      
      // Format 1: "EEEE dd/MM/yyyy" (ex: "samedi 29/11/2025")
      const dayNameMatch = reservationDateStr.match(/^\w+\s+(\d{2})\/(\d{2})\/(\d{4})/);
      if (dayNameMatch) {
        const [, day, month, year] = dayNameMatch;
        reservationDate = new Date(`${year}-${month}-${day}`);
      } else {
        // Format 2: "yyyy-MM-dd" ou "yyyy-MM-dd HH:mm:ss"
        const datePart = reservationDateStr.split(' ')[0];
        reservationDate = new Date(datePart);
      }
      
      if (isNaN(reservationDate.getTime())) {
        skippedMeal++;
        console.log(`⚠️ Date invalide ignorée: ${reservationDateStr}`);
        continue;
      }
      
      reservationDate.setHours(0, 0, 0, 0);
      
      // Comparer avec aujourd'hui
      if (reservationDate.getTime() === todayMidnight.getTime()) {
        const mealType = reservation.mealType;
        if (mealType === 'Déjeuner') {
          dejeunerCount++;
        } else if (mealType === 'Dîner') {
          dinerCount++;
        }
        console.log(`✅ Réservation d'aujourd'hui trouvée: ${mealType} - ${reservationDateStr}`);
      }
    }
    
    // Filtrer les réservations repas froid d'aujourd'hui
    for (const reservation of allColdReservations) {
      if (!reservation.reservationDate) {
        skippedCold++;
        continue;
      }
      
      // Parser la date avec plusieurs formats possibles
      let reservationDate = null;
      const reservationDateStr = String(reservation.reservationDate);
      
      // Format 1: "EEEE dd/MM/yyyy" (ex: "samedi 29/11/2025")
      const dayNameMatch = reservationDateStr.match(/^\w+\s+(\d{2})\/(\d{2})\/(\d{4})/);
      if (dayNameMatch) {
        const [, day, month, year] = dayNameMatch;
        reservationDate = new Date(`${year}-${month}-${day}`);
      } else {
        // Format 2: "yyyy-MM-dd" ou "yyyy-MM-dd HH:mm:ss"
        const datePart = reservationDateStr.split(' ')[0];
        reservationDate = new Date(datePart);
      }
      
      if (isNaN(reservationDate.getTime())) {
        skippedCold++;
        console.log(`⚠️ Date invalide ignorée (repas froid): ${reservationDateStr}`);
        continue;
      }
      
      reservationDate.setHours(0, 0, 0, 0);
      
      // Comparer avec aujourd'hui
      if (reservationDate.getTime() === todayMidnight.getTime()) {
        repasFroidCount++;
        console.log(`✅ Réservation repas froid d'aujourd'hui trouvée: ${reservationDateStr}`);
      }
    }
    
    const total = dejeunerCount + dinerCount + repasFroidCount;
    
    console.log('✅ Statistiques calculées:', {
      dejeuner: dejeunerCount,
      diner: dinerCount,
      repasFroid: repasFroidCount,
      total: total,
      skippedMeal: skippedMeal,
      skippedCold: skippedCold,
      totalMealReservations: allMealReservations.length,
      totalColdReservations: allColdReservations.length
    });
    
    res.status(200).json({
      success: true,
      date: todayStr,
      stats: {
        dejeuner: dejeunerCount,
        diner: dinerCount,
        repasFroid: repasFroidCount,
        total: total
      },
      debug: {
        totalMealReservations: allMealReservations.length,
        totalColdReservations: allColdReservations.length,
        skippedMeal: skippedMeal,
        skippedCold: skippedCold
      }
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération des statistiques:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

/* -------------------
   ABONNEMENT
------------------- */

console.log('📝 Enregistrement des routes d\'abonnement...');

// Route pour payer l'abonnement de 15 DNT
app.post('/subscribe', async (req, res) => {
  console.log('✅ Route /subscribe appelée');
  try {
    const { studentId, amount } = req.body;
    
    console.log('💳 Paiement d\'abonnement reçu:', { studentId, amount });
    
    if (!studentId) {
      return res.status(400).json({ success: false, message: 'studentId requis' });
    }
    
    const subscriptionAmount = 15.0; // Montant fixe de l'abonnement
    if (amount && parseFloat(amount) !== subscriptionAmount) {
      return res.status(400).json({ 
        success: false, 
        message: `Le montant de l'abonnement doit être ${subscriptionAmount} DNT` 
      });
    }
    
    // Trouver l'utilisateur
    const user = await User.findOne({ studentId });
    if (!user) {
      return res.status(404).json({ success: false, message: 'Utilisateur non trouvé' });
    }
    
    // Créditer le solde d'abonnement
    const currentBalance = user.subscriptionBalance || 0;
    const newBalance = currentBalance + subscriptionAmount;
    
    user.subscriptionBalance = newBalance;
    await user.save();
    
    // Enregistrer le paiement dans l'historique
    console.log('💾 Création du document Payment pour l\'abonnement...');
    const payment = new Payment({
      studentId: studentId,
      userEmail: user.email,
      userName: user.fullName,
      paymentType: 'subscription',
      amount: subscriptionAmount,
      description: 'Abonnement mensuel - 15 DNT',
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      paymentMethod: 'carte_virtuelle',
      status: 'SUCCESS',
      paymentGateway: 'simulation'
    });
    
    console.log('💾 Données du paiement avant sauvegarde:', {
      studentId: payment.studentId,
      paymentType: payment.paymentType,
      amount: payment.amount,
      balanceBefore: payment.balanceBefore,
      balanceAfter: payment.balanceAfter
    });
    
    // Vérifier la connexion MongoDB avant de sauvegarder
    if (mongoose.connection.readyState !== 1) {
      console.error('❌ MongoDB n\'est pas connecté! État:', mongoose.connection.readyState);
      throw new Error('MongoDB n\'est pas connecté');
    }
    
    try {
      await payment.save();
      console.log('✅ Paiement enregistré dans la collection payments:', {
        paymentId: payment._id,
        studentId,
        montant: subscriptionAmount,
        ancienSolde: currentBalance,
        nouveauSolde: newBalance,
        collection: 'payments'
      });
    } catch (saveError) {
      console.error('❌ ERREUR lors de la sauvegarde du paiement:', saveError);
      console.error('   Détails de l\'erreur:', {
        name: saveError.name,
        message: saveError.message,
        errors: saveError.errors,
        stack: saveError.stack
      });
      // Ne pas bloquer l'abonnement si le paiement ne peut pas être enregistré
      // mais logger l'erreur pour le débogage
      console.warn('⚠️ L\'abonnement sera crédité mais le paiement n\'a pas pu être enregistré dans l\'historique');
    }
    
    res.status(200).json({ 
      success: true, 
      message: 'Abonnement payé avec succès',
      subscriptionBalance: newBalance,
      amountAdded: subscriptionAmount,
      paymentId: payment._id
    });
  } catch (error) {
    console.error('❌ Erreur lors du paiement d\'abonnement:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route pour récupérer le solde d'abonnement d'un utilisateur
// IMPORTANT: Cette route doit être définie AVANT le middleware 404
app.get('/user/:studentId/balance', async (req, res) => {
  console.log('✅ Route GET /user/:studentId/balance appelée');
  console.log('   URL complète:', req.url);
  console.log('   studentId param:', req.params.studentId);
  try {
    const { studentId } = req.params;
    console.log('📊 Récupération du solde pour studentId:', studentId);
    
    const user = await User.findOne({ studentId });
    if (!user) {
      return res.status(404).json({ success: false, message: 'Utilisateur non trouvé' });
    }
    
    const balance = user.subscriptionBalance || 0;
    
    res.status(200).json({ 
      success: true, 
      subscriptionBalance: balance,
      studentId: studentId
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération du solde:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

/* -------------------
   PAIEMENT
------------------- */

// Route pour créer une page de paiement (simulation)
// Pour un vrai paiement, intégrez Stripe, Flouci, ou une autre passerelle
app.get('/payment-page', (req, res) => {
  console.log('✅ Route /payment-page appelée');
  console.log('   Query params:', req.query);
  try {
    const { amount, userId, email, description } = req.query;
    const amountValue = parseFloat(amount) || 0;
    console.log('   Montant:', amountValue);
    console.log('   UserId:', userId);
    
    // Page HTML de paiement par carte virtuelle améliorée pour WebView Android
    const html = `
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>Paiement par Carte Virtuelle</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .container {
            background: white;
            border-radius: 24px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.5);
            max-width: 420px;
            width: 100%;
            padding: 32px;
            text-align: center;
        }
        .header {
            margin-bottom: 24px;
        }
        h2 {
            color: #212121;
            margin-bottom: 8px;
            font-size: 26px;
            font-weight: bold;
        }
        .subtitle {
            color: #757575;
            font-size: 14px;
        }
        .amount-card {
            background: linear-gradient(135deg, #FF6B35 0%, #E55A2B 100%);
            border-radius: 16px;
            padding: 24px;
            margin: 24px 0;
            color: white;
        }
        .amount-label {
            font-size: 14px;
            opacity: 0.9;
            margin-bottom: 8px;
        }
        .amount-value {
            font-size: 36px;
            font-weight: bold;
            letter-spacing: 1px;
        }
        .info-section {
            background: #f5f5f5;
            border-radius: 12px;
            padding: 16px;
            margin: 16px 0;
            text-align: left;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            margin: 8px 0;
        }
        .info-label {
            color: #757575;
            font-size: 13px;
        }
        .info-value {
            color: #212121;
            font-size: 13px;
            font-weight: 500;
        }
        .card-form {
            margin: 24px 0;
            text-align: left;
        }
        .form-group {
            margin-bottom: 16px;
        }
        .form-label {
            display: block;
            color: #212121;
            font-size: 13px;
            font-weight: 500;
            margin-bottom: 8px;
        }
        .form-input {
            width: 100%;
            padding: 14px;
            border: 2px solid #E0E0E0;
            border-radius: 12px;
            font-size: 16px;
            transition: border-color 0.3s;
            background: #FAFAFA;
        }
        .form-input:focus {
            outline: none;
            border-color: #FF6B35;
            background: white;
        }
        .form-row {
            display: flex;
            gap: 12px;
        }
        .form-row .form-group {
            flex: 1;
        }
        .button-group {
            margin-top: 24px;
        }
        button {
            width: 100%;
            padding: 16px;
            margin: 8px 0;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.3s;
            -webkit-tap-highlight-color: transparent;
        }
        button:active {
            transform: scale(0.98);
        }
        .btn-success {
            background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
            color: white;
            box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
        }
        .btn-success:hover {
            box-shadow: 0 6px 16px rgba(76, 175, 80, 0.5);
        }
        .btn-cancel {
            background: #F5F5F5;
            color: #757575;
        }
        .btn-cancel:hover {
            background: #EEEEEE;
        }
        .security-badge {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-top: 16px;
            color: #757575;
            font-size: 12px;
        }
        .lock-icon {
            width: 16px;
            height: 16px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>💳 Paiement Sécurisé</h2>
            <p class="subtitle">Carte Virtuelle</p>
        </div>
        
        <div class="amount-card">
            <div class="amount-label">Montant à payer</div>
            <div class="amount-value">${amountValue.toFixed(3)} TND</div>
        </div>
        
        <div class="info-section">
            <div class="info-row">
                <span class="info-label">Email:</span>
                <span class="info-value">${email || 'Utilisateur'}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Description:</span>
                <span class="info-value">${description || 'Réservation de repas'}</span>
            </div>
        </div>
        
        <div class="card-form">
            <div class="form-group">
                <label class="form-label">Numéro de carte</label>
                <input type="text" id="cardNumber" class="form-input" 
                       placeholder="1234 5678 9012 3456" maxlength="19" 
                       inputmode="numeric" autocomplete="cc-number">
            </div>
            
            <div class="form-group">
                <label class="form-label">Nom sur la carte</label>
                <input type="text" id="cardName" class="form-input" 
                       placeholder="JEAN DUPONT" maxlength="50" 
                       autocomplete="cc-name">
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Date d'expiration</label>
                    <input type="text" id="cardExpiry" class="form-input" 
                           placeholder="MM/AA" maxlength="5" 
                           inputmode="numeric" autocomplete="cc-exp">
                </div>
                <div class="form-group">
                    <label class="form-label">CVV</label>
                    <input type="text" id="cardCvv" class="form-input" 
                           placeholder="123" maxlength="3" 
                           inputmode="numeric" autocomplete="cc-csc">
                </div>
            </div>
        </div>
        
        <div class="button-group">
            <button class="btn-success" onclick="processPayment('success')">
                ✅ Payer ${amountValue.toFixed(3)} TND
            </button>
            <button class="btn-cancel" onclick="processPayment('cancel')">
                ❌ Annuler
            </button>
        </div>
        
        <div class="security-badge">
            <svg class="lock-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18,8A2,2 0 0,1 20,10V20A2,2 0 0,1 18,22H6A2,2 0 0,1 4,20V10C4,8.89 4.9,8 6,8H7V6A5,5 0 0,1 12,1A5,5 0 0,1 17,6V8H18M12,3A3,3 0 0,0 9,6V8H15V6A3,3 0 0,0 12,3Z"/>
            </svg>
            <span>Paiement sécurisé SSL</span>
        </div>
    </div>
    
    <script>
        // Formatage automatique du numéro de carte
        document.getElementById('cardNumber').addEventListener('input', function(e) {
            let value = e.target.value.replace(/\\s/g, '');
            let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value;
            e.target.value = formattedValue;
        });
        
        // Formatage automatique de la date d'expiration
        document.getElementById('cardExpiry').addEventListener('input', function(e) {
            let value = e.target.value.replace(/\\D/g, '');
            if (value.length >= 2) {
                value = value.substring(0, 2) + '/' + value.substring(2, 4);
            }
            e.target.value = value;
        });
        
        // Limiter le CVV aux chiffres
        document.getElementById('cardCvv').addEventListener('input', function(e) {
            e.target.value = e.target.value.replace(/\\D/g, '');
        });
        
        // Mettre en majuscules le nom sur la carte
        document.getElementById('cardName').addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase();
        });
        
        function processPayment(result) {
            console.log('processPayment appelé avec:', result);
            
            // Récupérer les valeurs des champs (sans validation stricte pour les tests)
            const cardNumber = document.getElementById('cardNumber').value.replace(/\\s/g, '');
            const cardName = document.getElementById('cardName').value;
            const cardExpiry = document.getElementById('cardExpiry').value;
            const cardCvv = document.getElementById('cardCvv').value;
            
            console.log('Données de la carte:', {
                cardNumber: cardNumber ? (cardNumber.substring(0, 4) + '****') : 'vide',
                cardName: cardName || 'vide',
                cardExpiry: cardExpiry || 'vide',
                cardCvv: cardCvv ? '***' : 'vide'
            });
            
            // MODE TEST : Validation optionnelle - permet n'importe quelles données
            // Pour activer la validation stricte, décommentez le code ci-dessous
            /*
            // MODE TEST : Validation désactivée - vous pouvez entrer n'importe quoi
            // La validation est commentée pour permettre les tests avec n'importe quelles données
            /*
            if (result === 'success') {
                // Validation minimale
                if (cardNumber.length < 13 || cardNumber.length > 19) {
                    alert('Veuillez entrer un numéro de carte valide (13-19 chiffres)');
                    console.log('Validation échouée: numéro de carte invalide');
                    return;
                }
                if (!cardName || cardName.length < 3) {
                    alert('Veuillez entrer le nom sur la carte');
                    console.log('Validation échouée: nom manquant');
                    return;
                }
                if (cardExpiry.length !== 5) {
                    alert('Veuillez entrer une date d\'expiration valide (MM/AA)');
                    console.log('Validation échouée: date d\'expiration invalide');
                    return;
                }
                if (cardCvv.length !== 3) {
                    alert('Veuillez entrer un CVV valide (3 chiffres)');
                    console.log('Validation échouée: CVV invalide');
                    return;
                }
            }
            */
            
            // MODE TEST : Accepter n'importe quelles données (même vides)
            console.log('✅ Mode test activé - Validation désactivée, redirection immédiate...');
            */
            
            // MODE TEST : Accepter n'importe quelles données (même vides)
            console.log('Mode test activé - Validation désactivée, redirection...');
            
            // Notifier l'application Android via l'interface JavaScript
            if (typeof AndroidPayment !== 'undefined') {
                console.log('AndroidPayment interface trouvée');
                if (result === 'success') {
                    AndroidPayment.onPaymentSuccess();
                } else {
                    AndroidPayment.onPaymentCancel();
                }
            } else {
                console.log('AndroidPayment interface non trouvée');
            }
            
            // Redirection avec des URLs absolues pour la détection dans WebView
            const baseUrl = 'http://10.0.2.2:3000';
            if (result === 'success') {
                // URL de succès avec plusieurs indicateurs pour une meilleure détection
                const successUrl = baseUrl + '/payment_success?amount=${amount}&user_id=${userId}&status=success&result=success&timestamp=' + Date.now();
                console.log('Redirection vers:', successUrl);
                window.location.href = successUrl;
            } else {
                // URL d'annulation
                const cancelUrl = baseUrl + '/payment_cancel?status=cancel&result=cancel&timestamp=' + Date.now();
                console.log('Redirection vers:', cancelUrl);
                window.location.href = cancelUrl;
            }
        }
        
        // Ajouter un listener pour déboguer les clics sur le bouton
        document.addEventListener('DOMContentLoaded', function() {
            const btnSuccess = document.querySelector('.btn-success');
            if (btnSuccess) {
                btnSuccess.addEventListener('click', function(e) {
                    console.log('Bouton Payer cliqué');
                    e.preventDefault();
                    processPayment('success');
                });
            }
            
            const btnCancel = document.querySelector('.btn-cancel');
            if (btnCancel) {
                btnCancel.addEventListener('click', function(e) {
                    console.log('Bouton Annuler cliqué');
                    e.preventDefault();
                    processPayment('cancel');
                });
            }
        });
    </script>
</body>
</html>
    `;
    
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    console.log('✅ HTML de paiement envoyé avec succès');
    res.send(html);
  } catch (error) {
    console.error('❌ Erreur lors de la génération de la page de paiement:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Routes de callback pour les paiements (pour la simulation)
app.get('/payment_success', (req, res) => {
  const { amount, user_id } = req.query;
  const html = `
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Paiement Réussi</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            background: #4CAF50;
            color: white;
            text-align: center;
            padding: 20px;
        }
        .container {
            background: rgba(255,255,255,0.1);
            padding: 40px;
            border-radius: 20px;
        }
        h1 { font-size: 48px; margin-bottom: 20px; }
        p { font-size: 18px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>✅</h1>
        <h2>Paiement Réussi!</h2>
        <p>Montant: ${amount || 'N/A'} TND</p>
        <p>Vous allez être redirigé...</p>
    </div>
    <script>
        // Notifier Android
        if (typeof AndroidPayment !== 'undefined') {
            AndroidPayment.onPaymentSuccess();
        }
        // Redirection après 2 secondes
        setTimeout(function() {
            window.location.href = 'payment_success?status=success&result=success';
        }, 2000);
    </script>
</body>
</html>
  `;
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(html);
});

app.get('/payment_cancel', (req, res) => {
  const html = `
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Paiement Annulé</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            background: #f44336;
            color: white;
            text-align: center;
            padding: 20px;
        }
        .container {
            background: rgba(255,255,255,0.1);
            padding: 40px;
            border-radius: 20px;
        }
        h1 { font-size: 48px; margin-bottom: 20px; }
        p { font-size: 18px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>❌</h1>
        <h2>Paiement Annulé</h2>
        <p>Vous allez être redirigé...</p>
    </div>
    <script>
        // Notifier Android
        if (typeof AndroidPayment !== 'undefined') {
            AndroidPayment.onPaymentCancel();
        }
        // Redirection après 2 secondes
        setTimeout(function() {
            window.location.href = 'payment_cancel?status=cancel&result=cancel';
        }, 2000);
    </script>
</body>
</html>
  `;
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(html);
});

// Route pour créer une session de paiement virtuel
// Supporte : Flouci (Tunisie), Stripe, et autres passerelles
app.post('/create-payment-session', async (req, res) => {
  try {
    const { amount, userId, userEmail, description, isSubscription } = req.body;
    
    console.log('💳 Création de session de paiement:', { amount, userId, description });
    
    // ============================================================
    // OPTION 1 : FLOUCI (Tunisie) - PAIEMENT PAR CARTE BANCAIRE VIRTUELLE
    // ============================================================
    // REMPLACEZ VOTRE_APP_TOKEN et VOTRE_APP_SECRET par vos vraies clés Flouci
    // Obtenez-les depuis https://flouci.com après inscription
    
    // Vérifier si axios est disponible
    let axios;
    try {
      axios = require('axios');
    } catch (e) {
      console.log('⚠️ Axios non installé. Installez-le avec: npm install axios');
      axios = null;
    }
    
    // Essayer d'utiliser Flouci si axios est disponible et les clés sont configurées
    if (axios) {
      // ⚠️ IMPORTANT : Remplacez ces valeurs par vos vraies clés Flouci
      const FLOUCI_APP_TOKEN = process.env.FLOUCI_APP_TOKEN || 'VOTRE_APP_TOKEN_ICI';
      const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'VOTRE_APP_SECRET_ICI';
      
      // Vérifier si les clés sont configurées (pas les valeurs par défaut)
      if (FLOUCI_APP_TOKEN && FLOUCI_APP_TOKEN !== 'VOTRE_APP_TOKEN_ICI' && 
          FLOUCI_APP_SECRET && FLOUCI_APP_SECRET !== 'VOTRE_APP_SECRET_ICI') {
        try {
          // URLs de callback pour la WebView Android
          // Note: Pour l'émulateur Android, utilisez 10.0.2.2 au lieu de localhost
          // Pour un appareil réel, utilisez l'IP locale de votre machine
          const baseUrl = process.env.SERVER_URL || 'http://10.0.2.2:3000';
          const successLink = `${baseUrl}/payment_success?amount=${amount}&user_id=${userId}&gateway=flouci`;
          const failLink = `${baseUrl}/payment_cancel?gateway=flouci`;
          
          console.log('🔗 URLs de callback Flouci:');
          console.log('   Succès:', successLink);
          console.log('   Échec:', failLink);
          
          const response = await axios.post('https://api.flouci.com/api/generatePayment', {
            app_token: FLOUCI_APP_TOKEN,
            app_secret: FLOUCI_APP_SECRET,
            amount: Math.round(amount * 1000), // Flouci utilise millimes (1000 millimes = 1 TND)
            success_link: successLink, // URL de callback après succès
            fail_link: failLink, // URL de callback après échec
            developer_tracking_id: userId,
            customer_name: userEmail,
            customer_email: userEmail,
            // Optionnel: Informations supplémentaires pour les cartes virtuelles
            receipt: description || (isSubscription ? 'Abonnement mensuel' : 'Réservation repas')
          }, {
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          if (response.data && response.data.result && response.data.result.link) {
            console.log('✅ Session Flouci créée:', response.data.result.id);
            console.log('💳 URL de paiement Flouci:', response.data.result.link);
            return res.json({ 
              success: true,
              url: response.data.result.link,
              paymentId: response.data.result.id,
              gateway: 'flouci'
            });
          } else {
            throw new Error('Réponse Flouci invalide');
          }
        } catch (flouciError) {
          console.error('❌ Erreur Flouci:', flouciError.response?.data || flouciError.message);
          console.log('📄 Fallback vers la simulation...');
          // Fallback vers la simulation si Flouci échoue
        }
      } else {
        console.log('⚠️ Clés Flouci non configurées. Utilisation de la simulation.');
        console.log('📝 Pour activer Flouci, configurez les variables d\'environnement FLOUCI_APP_TOKEN et FLOUCI_APP_SECRET');
      }
    } else {
      console.log('⚠️ Axios non disponible. Utilisation de la simulation.');
    }
    
    // ============================================================
    // OPTION 2 : STRIPE (International)
    // ============================================================
    /*
    const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY || 'sk_test_VOTRE_CLE');
    
    const session = await stripe.checkout.sessions.create({
      payment_method_types: ['card'],
      line_items: [{
        price_data: {
          currency: 'usd', // ou 'tnd' si disponible
          product_data: { 
            name: description || (isSubscription ? 'Abonnement mensuel' : 'Réservation repas'),
          },
          unit_amount: Math.round(amount * 100), // Convertir en centimes
        },
        quantity: 1,
      }],
      mode: 'payment',
      success_url: 'votre-app://payment_success?session_id={CHECKOUT_SESSION_ID}',
      cancel_url: 'votre-app://payment_cancel',
      customer_email: userEmail,
      metadata: {
        userId: userId,
        amount: amount.toString(),
        isSubscription: isSubscription ? 'true' : 'false'
      }
    });
    
    return res.json({ 
      success: true,
      url: session.url, 
      sessionId: session.id,
      gateway: 'stripe'
    });
    */
    
    // ============================================================
    // OPTION 3 : SIMULATION (Par défaut pour les tests)
    // ============================================================
    // Pour l'instant, retourner l'URL de la page de paiement simulée
    const paymentUrl = `http://10.0.2.2:3000/payment-page?amount=${amount}&userId=${userId}&email=${encodeURIComponent(userEmail || '')}&description=${encodeURIComponent(description || (isSubscription ? 'Abonnement mensuel' : 'Réservation repas'))}`;
    
    console.log('📄 Utilisation de la page de paiement simulée');
    console.log('   URL:', paymentUrl);
    console.log('   Note: L\'app Android utilise maintenant le HTML local directement');
    res.json({ 
      success: true,
      url: paymentUrl,
      gateway: 'simulation',
      message: 'Mode simulation activé. L\'app Android utilise maintenant le HTML local directement.'
    });
  } catch (error) {
    console.error('❌ Erreur lors de la création de la session de paiement:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route de test pour vérifier que la collection payments fonctionne
app.post('/test-payment-save', async (req, res) => {
  try {
    console.log('🧪 Test de sauvegarde dans la collection payments...');
    
    // Vérifier la connexion MongoDB
    if (mongoose.connection.readyState !== 1) {
      return res.status(500).json({ 
        success: false, 
        message: 'MongoDB n\'est pas connecté',
        readyState: mongoose.connection.readyState
      });
    }
    
    // Créer un paiement de test
    const testPayment = new Payment({
      studentId: 'TEST_STUDENT',
      userEmail: 'test@example.com',
      userName: 'Test User',
      paymentType: 'subscription',
      amount: 15.0,
      description: 'Test de sauvegarde',
      balanceBefore: 0,
      balanceAfter: 15.0,
      paymentMethod: 'carte_virtuelle',
      status: 'SUCCESS',
      paymentGateway: 'simulation'
    });
    
    console.log('💾 Tentative de sauvegarde du paiement de test...');
    await testPayment.save();
    
    console.log('✅ Paiement de test enregistré avec succès:', {
      paymentId: testPayment._id,
      collection: 'payments'
    });
    
    // Vérifier que le paiement est bien dans la base
    const foundPayment = await Payment.findById(testPayment._id);
    
    if (foundPayment) {
      // Supprimer le paiement de test
      await Payment.findByIdAndDelete(testPayment._id);
      console.log('🗑️ Paiement de test supprimé');
      
      res.status(200).json({ 
        success: true, 
        message: 'Test réussi! La collection payments fonctionne correctement.',
        paymentId: testPayment._id,
        found: true
      });
    } else {
      res.status(500).json({ 
        success: false, 
        message: 'Le paiement a été sauvegardé mais n\'a pas pu être retrouvé',
        paymentId: testPayment._id
      });
    }
  } catch (error) {
    console.error('❌ Erreur lors du test de sauvegarde:', error);
    console.error('   Détails:', {
      name: error.name,
      message: error.message,
      errors: error.errors,
      stack: error.stack
    });
    res.status(500).json({ 
      success: false, 
      message: 'Erreur lors du test',
      error: error.message,
      details: error.errors || null
    });
  }
});

// Route pour récupérer l'historique des paiements d'un utilisateur
app.get('/payments/user/:studentId', async (req, res) => {
  try {
    const { studentId } = req.params;
    console.log('📊 Récupération de l\'historique des paiements pour studentId:', studentId);
    
    // Vérifier la connexion MongoDB
    if (mongoose.connection.readyState !== 1) {
      return res.status(500).json({ 
        success: false, 
        message: 'MongoDB n\'est pas connecté',
        readyState: mongoose.connection.readyState
      });
    }
    
    const payments = await Payment.find({ studentId })
      .sort({ createdAt: -1 }) // Plus récents en premier
      .limit(100); // Limiter à 100 paiements
    
    console.log(`✅ ${payments.length} paiement(s) trouvé(s) pour ${studentId}`);
    
    res.status(200).json({ 
      success: true, 
      payments: payments,
      count: payments.length
    });
  } catch (error) {
    console.error('❌ Erreur lors de la récupération de l\'historique des paiements:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

// Route pour vérifier le statut d'un paiement (Flouci)
app.get('/verify-payment/:paymentId', async (req, res) => {
  try {
    const { paymentId } = req.params;
    
    // Exemple avec Flouci
    /*
    const axios = require('axios');
    const FLOUCI_APP_SECRET = process.env.FLOUCI_APP_SECRET || 'VOTRE_APP_SECRET';
    
    const response = await axios.get(`https://api.flouci.com/api/verifyPayment/${paymentId}`, {
      headers: {
        'apppublic': FLOUCI_APP_SECRET
      }
    });
    
    if (response.data && response.data.success && response.data.result.status === 'SUCCESS') {
      return res.json({ 
        success: true, 
        status: 'success',
        payment: response.data.result 
      });
    }
    */
    
    // Pour la simulation, retourner toujours succès
    res.json({ 
      success: true, 
      status: 'success',
      message: 'Mode simulation - Paiement toujours réussi'
    });
  } catch (error) {
    console.error('❌ Erreur vérification paiement:', error);
    res.status(500).json({ success: false, message: 'Erreur serveur', error: error.message });
  }
});

/* -------------------------------------------------------------------
   MIDDLEWARE DE GESTION D'ERREUR 404 (après toutes les routes)
------------------------------------------------------------------- */

// Gestion des routes non trouvées - doit être après toutes les routes
app.use((req, res) => {
  console.error(`❌ Route non trouvée: ${req.method} ${req.url}`);
  console.error(`   Headers:`, JSON.stringify(req.headers));
  console.error(`   Body:`, JSON.stringify(req.body));
  res.status(404).json({ 
    success: false, 
    message: `Route non trouvée: ${req.method} ${req.url}`,
    hint: 'Vérifiez que le serveur est démarré et que la route existe',
    availableRoutes: [
      'POST /register',
      'POST /login',
      'GET /menus',
      'POST /menus',
      'PUT /menus/:id',
      'DELETE /menus/:id',
      'POST /meal-reservations',  
      'GET /meal-reservations/user/:studentId',
      'PUT /meal-reservations/:id/use',
      'PUT /meal-reservations/:id/cancel',
      'POST /cold-meal-reservations',
      'GET /cold-meal-reservations/user/:studentId',
      'PUT /cold-meal-reservations/:id/use',
      'PUT /cold-meal-reservations/:id/cancel',
      'POST /subscribe (Payer abonnement 15 DNT)',
      'GET /user/:studentId/balance (Récupérer solde abonnement)',
      'GET /admin/reservations/stats?period=day|week|month (Statistiques réservations par période)',
      'GET /admin/users/stats (Statistiques utilisateurs avec abonnement)',
      'GET /admin/users (Récupérer tous les utilisateurs)',
      'PUT /admin/users/:id (Mettre à jour un utilisateur)',
      'PUT /admin/users/:id/block (Bloquer/Débloquer un utilisateur)',
      'PUT /admin/users/unblock-all (Débloquer tous les comptes)',
      'DELETE /admin/users/:id (Supprimer un utilisateur)',
      'GET /payments/user/:studentId (Récupérer historique des paiements)',
      'POST /test-payment-save (Test de sauvegarde dans payments)',
      'GET /payment-page',
      'GET /payment_success',
      'GET /payment_cancel',
      'POST /create-payment-session',
      'GET /test-connection',
      'POST /test-connection',
      'GET /comments (Récupérer tous les commentaires)',
      'GET /menus/:menuId/comments (Récupérer commentaires d\'un menu)',
      'POST /menus/:menuId/comments (Créer un commentaire sur un menu)',
      'DELETE /comments/:id (Supprimer un commentaire)',
      'POST /orders/comment (Créer une commande avec commentaire)',
      'GET /orders/comments (Récupérer toutes les commandes avec commentaires)'
    ]
  });
});

/* -------------------------------------------------------------------
   LANCEMENT DU SERVEUR
------------------------------------------------------------------- */

const PORT = 3000;
const HOST = '0.0.0.0'; // Écouter sur toutes les interfaces pour permettre la connexion depuis l'émulateur Android
app.listen(PORT, HOST, () => {
  console.log(`🚀 Serveur démarré sur http://localhost:${PORT}`);
  console.log(`📡 Serveur accessible depuis l'émulateur Android via http://10.0.2.2:${PORT}`);
  console.log('');
  console.log('✅ Route /orders/comments est ENREGISTRÉE et DISPONIBLE');
  console.log('');
  console.log('📡 Routes disponibles:');
  console.log('   - POST /register');
  console.log('   - POST /login');
  console.log('   - GET /menus');
  console.log('   - POST /menus (Créer un menu)');
  console.log('   - PUT /menus/:id (Modifier un menu)');
  console.log('   - DELETE /menus/:id (Supprimer un menu)');
  console.log('   - POST /meal-reservations (Déjeuner/Dîner)');
  console.log('   - GET /meal-reservations/user/:studentId');
  console.log('   - PUT /meal-reservations/:id/use (Scan QR)');
  console.log('   - PUT /meal-reservations/:id/cancel');
  console.log('   - POST /cold-meal-reservations (Repas Froid)');
  console.log('   - GET /cold-meal-reservations/user/:studentId');
  console.log('   - PUT /cold-meal-reservations/:id/use (Scan QR)');
  console.log('   - PUT /cold-meal-reservations/:id/cancel');
  console.log('');
  console.log('💳 Routes d\'abonnement:');
  console.log('   - POST /subscribe (Payer abonnement 15 DNT)');
  console.log('   - GET /user/:studentId/balance (Récupérer solde)');
  console.log('✅ Routes d\'abonnement enregistrées avec succès!');
  console.log('');
  console.log('💳 Routes de paiement:');
  console.log('   - GET /payment-page (Page de paiement simulée)');
  console.log('   - POST /create-payment-session (Créer une session de paiement)');
  console.log('   - GET /payment_success (Callback succès)');
  console.log('   - GET /payment_cancel (Callback annulation)');
  console.log('   - GET /verify-payment/:paymentId (Vérifier un paiement)');
  console.log('   - GET /payments/user/:studentId (Historique des paiements)');
  console.log('');
  console.log('🔧 Routes de test:');
  console.log('   - GET /test-connection (Test de connexion)');
  console.log('   - POST /test-connection (Test de connexion POST)');
  console.log('   - POST /test-payment-save (Test de sauvegarde dans payments)');
  console.log('');
  console.log('💡 Pour tester la connexion: node test-connection.js');
  console.log('');
  console.log('💬 Routes de commentaires:');
  console.log('   - GET /comments (Récupérer tous les commentaires)');
  console.log('   - GET /menus/:menuId/comments (Récupérer commentaires d\'un menu)');
  console.log('   - POST /menus/:menuId/comments (Créer un commentaire sur un menu)');
  console.log('   - DELETE /comments/:id (Supprimer un commentaire)');
  console.log('   - POST /orders/comment (Créer une commande avec commentaire)');
  console.log('   - GET /orders/comments (Récupérer toutes les commandes avec commentaires)');
});
