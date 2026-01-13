// Script de test pour vérifier que les routes d'abonnement fonctionnent
const http = require('http');

const BASE_URL = 'http://localhost:3000';
const TEST_STUDENT_ID = '45646545341';

console.log('🧪 Test des routes d\'abonnement...\n');

// Test 1: GET /user/:studentId/balance
console.log('Test 1: GET /user/:studentId/balance');
const balanceUrl = `${BASE_URL}/user/${TEST_STUDENT_ID}/balance`;
console.log(`URL: ${balanceUrl}`);

const req1 = http.get(balanceUrl, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => {
    console.log(`Status: ${res.statusCode}`);
    console.log(`Réponse: ${data}\n`);
    
    if (res.statusCode === 200) {
      console.log('✅ Route GET /user/:studentId/balance fonctionne!\n');
    } else {
      console.log('❌ Route GET /user/:studentId/balance ne fonctionne pas!\n');
    }
    
    // Test 2: POST /subscribe
    console.log('Test 2: POST /subscribe');
    const subscribeUrl = `${BASE_URL}/subscribe`;
    console.log(`URL: ${subscribeUrl}`);
    
    const postData = JSON.stringify({
      studentId: TEST_STUDENT_ID,
      amount: 15
    });
    
    const req2 = http.request({
      hostname: 'localhost',
      port: 3000,
      path: '/subscribe',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(postData)
      }
    }, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        console.log(`Status: ${res.statusCode}`);
        console.log(`Réponse: ${data}\n`);
        
        if (res.statusCode === 200) {
          console.log('✅ Route POST /subscribe fonctionne!');
        } else {
          console.log('❌ Route POST /subscribe ne fonctionne pas!');
        }
        
        process.exit(0);
      });
    });
    
    req2.on('error', (e) => {
      console.error(`❌ Erreur: ${e.message}`);
      process.exit(1);
    });
    
    req2.write(postData);
    req2.end();
  });
});

req1.on('error', (e) => {
  console.error(`❌ Erreur: ${e.message}`);
  console.error('⚠️ Assurez-vous que le serveur est démarré sur le port 3000');
  process.exit(1);
});



