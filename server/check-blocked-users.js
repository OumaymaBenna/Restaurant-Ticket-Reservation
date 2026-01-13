/**
 * Script pour vérifier l'état des comptes bloqués dans la base de données
 */

const mongoose = require('mongoose');

mongoose.connect('mongodb://127.0.0.1:27017/restaurant')
  .then(() => {
    console.log('✅ Connecté à MongoDB\n');
    checkBlockedUsers();
  })
  .catch(err => {
    console.error('❌ Erreur de connexion MongoDB :', err.message);
    process.exit(1);
  });

const userSchema = new mongoose.Schema({}, { strict: false, collection: 'users' });
const User = mongoose.model('User', userSchema);

async function checkBlockedUsers() {
  try {
    const now = new Date();
    
    // Récupérer tous les utilisateurs
    const allUsers = await User.find({});
    console.log(`📊 Total d'utilisateurs dans la base: ${allUsers.length}\n`);
    
    // Utilisateurs avec isBlocked = true
    const blockedByFlag = await User.find({ isBlocked: true });
    console.log(`🔒 Utilisateurs avec isBlocked = true: ${blockedByFlag.length}`);
    if (blockedByFlag.length > 0) {
      blockedByFlag.forEach((user, index) => {
        console.log(`   ${index + 1}. Email: ${user.email || 'N/A'}, StudentId: ${user.studentId || 'N/A'}`);
        if (user.blockedUntil) {
          const blockedDate = new Date(user.blockedUntil);
          const daysRemaining = Math.ceil((blockedDate - now) / (1000 * 60 * 60 * 24));
          console.log(`      - Bloqué jusqu'au: ${blockedDate.toLocaleDateString('fr-FR')}`);
          console.log(`      - Jours restants: ${daysRemaining}`);
        }
      });
    }
    
    // Utilisateurs avec blockedUntil dans le futur
    const blockedByDate = await User.find({
      blockedUntil: { $exists: true, $ne: null, $gt: now }
    });
    console.log(`\n📅 Utilisateurs avec blockedUntil dans le futur: ${blockedByDate.length}`);
    if (blockedByDate.length > 0) {
      blockedByDate.forEach((user, index) => {
        console.log(`   ${index + 1}. Email: ${user.email || 'N/A'}, StudentId: ${user.studentId || 'N/A'}`);
        const blockedDate = new Date(user.blockedUntil);
        const daysRemaining = Math.ceil((blockedDate - now) / (1000 * 60 * 60 * 24));
        console.log(`      - Bloqué jusqu'au: ${blockedDate.toLocaleDateString('fr-FR')}`);
        console.log(`      - Jours restants: ${daysRemaining}`);
        console.log(`      - isBlocked: ${user.isBlocked || false}`);
      });
    }
    
    // Rechercher l'utilisateur tk@gmail.com spécifiquement
    const specificUser = await User.findOne({ email: 'tk@gmail.com' });
    if (specificUser) {
      console.log(`\n🔍 Détails de l'utilisateur tk@gmail.com:`);
      console.log(`   - Email: ${specificUser.email}`);
      console.log(`   - StudentId: ${specificUser.studentId}`);
      console.log(`   - isBlocked: ${specificUser.isBlocked || false}`);
      if (specificUser.blockedUntil) {
        const blockedDate = new Date(specificUser.blockedUntil);
        const daysRemaining = Math.ceil((blockedDate - now) / (1000 * 60 * 60 * 24));
        console.log(`   - blockedUntil: ${blockedDate.toLocaleDateString('fr-FR')}`);
        console.log(`   - Jours restants: ${daysRemaining}`);
      } else {
        console.log(`   - blockedUntil: null`);
      }
    } else {
      console.log(`\n❌ Utilisateur tk@gmail.com non trouvé dans la base de données`);
    }
    
    mongoose.connection.close();
    console.log('\n✅ Vérification terminée');
    process.exit(0);
  } catch (error) {
    console.error('❌ Erreur:', error);
    mongoose.connection.close();
    process.exit(1);
  }
}

