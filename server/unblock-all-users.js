/**
 * Script pour débloquer tous les comptes utilisateurs
 * 
 * Utilisation:
 *   node unblock-all-users.js
 * 
 * Ou avec curl:
 *   curl -X PUT http://localhost:3000/admin/users/unblock-all
 */

const mongoose = require('mongoose');

// Connexion MongoDB
mongoose.connect('mongodb://127.0.0.1:27017/restaurant')
  .then(() => {
    console.log('✅ Connecté à MongoDB');
    unblockAllUsers();
  })
  .catch(err => {
    console.error('❌ Erreur de connexion MongoDB :', err.message);
    process.exit(1);
  });

// Schéma Utilisateur (simplifié pour ce script)
const userSchema = new mongoose.Schema({
  fullName: { type: String, required: true },
  email: { type: String, required: true, unique: true },
  studentId: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  isBlocked: { type: Boolean, default: false },
  blockedUntil: { type: Date, default: null },
  role: { type: String, enum: ['etudiant', 'admin'], default: 'etudiant' }
}, { collection: 'users' });

const User = mongoose.model('User', userSchema);

async function unblockAllUsers() {
  try {
    console.log('🔓 Déblocage de tous les comptes utilisateurs...');
    
    const now = new Date();
    
    // Trouver tous les utilisateurs bloqués (isBlocked: true OU blockedUntil dans le futur)
    const blockedUsers = await User.find({
      $or: [
        { isBlocked: true },
        { blockedUntil: { $exists: true, $ne: null, $gt: now } }
      ]
    });
    
    console.log(`📋 Trouvé ${blockedUsers.length} compte(s) bloqué(s) à débloquer`);
    
    if (blockedUsers.length > 0) {
      // Afficher les détails des comptes bloqués
      console.log('\n📝 Détails des comptes bloqués:');
      blockedUsers.forEach((user, index) => {
        console.log(`   ${index + 1}. ${user.email || user.studentId}`);
        console.log(`      - isBlocked: ${user.isBlocked}`);
        if (user.blockedUntil) {
          const blockedDate = new Date(user.blockedUntil);
          const daysRemaining = Math.ceil((blockedDate - now) / (1000 * 60 * 60 * 24));
          console.log(`      - Bloqué jusqu'au: ${blockedDate.toLocaleDateString('fr-FR')}`);
          console.log(`      - Jours restants: ${daysRemaining}`);
        }
      });
    }
    
    // Mettre à jour tous les utilisateurs bloqués
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
    
    console.log(`\n✅ ${result.modifiedCount} compte(s) débloqué(s) avec succès`);
    
    if (result.modifiedCount > 0) {
      const allUsers = await User.find({});
      console.log(`\n📊 Résumé:`);
      console.log(`   - Total d'utilisateurs dans la base: ${allUsers.length}`);
      console.log(`   - Comptes débloqués: ${result.modifiedCount}`);
    } else {
      console.log('ℹ️  Aucun compte n\'était bloqué.');
    }
    
    mongoose.connection.close();
    console.log('\n✅ Opération terminée');
    process.exit(0);
  } catch (error) {
    console.error('❌ Erreur lors du déblocage:', error);
    mongoose.connection.close();
    process.exit(1);
  }
}

