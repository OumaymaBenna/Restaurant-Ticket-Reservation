// Script pour tester un paiement réel avec un vrai studentId
const mongoose = require('mongoose');

// Connexion MongoDB
mongoose.connect('mongodb://127.0.0.1:27017/restaurant')
  .then(async () => {
    console.log('✅ Connecté à MongoDB');
    
    // Schéma Payment
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
      balanceBefore: { type: Number, required: true },
      balanceAfter: { type: Number, required: true },
      paymentMethod: { type: String, default: 'carte_virtuelle' },
      status: { 
        type: String, 
        enum: ['SUCCESS', 'FAILED', 'PENDING', 'CANCELLED'], 
        default: 'SUCCESS' 
      },
      paymentGateway: { type: String, default: 'simulation' },
      transactionId: { type: String, default: '' },
      createdAt: { type: Date, default: Date.now }
    });
    
    const Payment = mongoose.model('Payment', paymentSchema, 'payments');
    
    // Récupérer un vrai utilisateur
    const User = mongoose.model('User', new mongoose.Schema({}), 'users');
    const user = await User.findOne({ role: 'etudiant' });
    
    if (!user) {
      console.log('❌ Aucun utilisateur étudiant trouvé');
      process.exit(1);
    }
    
    console.log('👤 Utilisateur trouvé:', {
      studentId: user.studentId,
      email: user.email,
      fullName: user.fullName,
      balance: user.subscriptionBalance || 0
    });
    
    // Créer un paiement réel
    const currentBalance = user.subscriptionBalance || 0;
    const paymentAmount = 15.0;
    const newBalance = currentBalance + paymentAmount;
    
    console.log('\n💾 Création d\'un paiement réel...');
    const payment = new Payment({
      studentId: user.studentId,
      userEmail: user.email,
      userName: user.fullName,
      paymentType: 'subscription',
      amount: paymentAmount,
      description: 'Test de paiement réel - 15 DNT',
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      paymentMethod: 'carte_virtuelle',
      status: 'SUCCESS',
      paymentGateway: 'simulation'
    });
    
    try {
      await payment.save();
      console.log('✅ Paiement enregistré avec succès!');
      console.log('   Payment ID:', payment._id);
      console.log('   Student ID:', payment.studentId);
      console.log('   Amount:', payment.amount);
      
      // Vérifier dans la base
      const found = await Payment.findById(payment._id);
      if (found) {
        console.log('✅ Paiement retrouvé dans la base de données');
        console.log('   Total paiements dans la collection:', await Payment.countDocuments());
      } else {
        console.log('❌ Paiement non retrouvé dans la base');
      }
      
      // Afficher tous les paiements
      console.log('\n📋 Tous les paiements dans la collection:');
      const allPayments = await Payment.find().sort({ createdAt: -1 }).limit(10);
      allPayments.forEach((p, i) => {
        console.log(`   ${i + 1}. ${p.paymentType} - ${p.amount} DNT - ${p.studentId} - ${p.createdAt}`);
      });
      
    } catch (error) {
      console.error('❌ Erreur lors de la sauvegarde:', error);
      console.error('   Détails:', {
        name: error.name,
        message: error.message,
        errors: error.errors
      });
    }
    
    mongoose.connection.close();
    process.exit(0);
  })
  .catch(err => {
    console.error('❌ Erreur de connexion MongoDB:', err);
    process.exit(1);
  });



