const http = require('http');

console.log('🔍 Test de connexion au serveur...\n');

const options = {
  hostname: 'localhost',
  port: 3000,
  path: '/test-connection',
  method: 'GET',
  timeout: 5000
};

const req = http.request(options, (res) => {
  console.log(`✅ Status: ${res.statusCode}`);
  console.log(`✅ Headers:`, res.headers);
  
  let data = '';
  res.on('data', (chunk) => {
    data += chunk;
  });
  
  res.on('end', () => {
    console.log(`✅ Réponse:`, data);
    console.log('\n✅ Le serveur fonctionne correctement!');
    process.exit(0);
  });
});

req.on('error', (e) => {
  console.error(`❌ Erreur de connexion: ${e.message}`);
  console.error('\n💡 Le serveur n\'est probablement pas démarré.');
  console.error('   Démarrez-le avec: node server.js');
  process.exit(1);
});

req.on('timeout', () => {
  console.error('❌ Timeout: Le serveur ne répond pas');
  req.destroy();
  process.exit(1);
});

req.end();
