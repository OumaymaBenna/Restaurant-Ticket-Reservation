// Script de test pour vérifier que la route /orders/comments fonctionne
const http = require('http');

const options = {
  hostname: 'localhost',
  port: 3000,
  path: '/orders/comments',
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
};

console.log('🧪 Test de la route GET /orders/comments...');
console.log('   URL: http://localhost:3000/orders/comments');

const req = http.request(options, (res) => {
  console.log(`   Status: ${res.statusCode}`);
  console.log(`   Headers:`, res.headers);

  let data = '';

  res.on('data', (chunk) => {
    data += chunk;
  });

  res.on('end', () => {
    console.log('   Réponse:', data);
    if (res.statusCode === 200) {
      console.log('✅ Route fonctionne correctement!');
    } else if (res.statusCode === 404) {
      console.log('❌ Erreur 404 - Route non trouvée. Redémarrez le serveur Node.js.');
    } else {
      console.log(`⚠️  Code de réponse: ${res.statusCode}`);
    }
  });
});

req.on('error', (error) => {
  console.error('❌ Erreur de connexion:', error.message);
  console.error('   Vérifiez que le serveur est démarré sur le port 3000');
});

req.end();

