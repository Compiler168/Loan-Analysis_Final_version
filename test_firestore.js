const { initializeFirebase } = require('./backend/src/config/firebase');
require('dotenv').config({ path: './backend/.env' });

async function test() {
  const db = await initializeFirebase();
  const snapshot = await db.collection('dashboards').get();
  console.log('Dashboards count:', snapshot.size);
  snapshot.forEach(doc => {
    console.log(doc.id, '=>', doc.data());
  });
  process.exit(0);
}
test().catch(err => {
  console.error(err);
  process.exit(1);
});
