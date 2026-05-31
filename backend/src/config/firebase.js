/**
 * SmartLoan AI+ — Firebase Firestore Configuration
 */
const path = require('path');
const fs = require('fs');

let admin = null;
let db = null;

// Initialize Firebase Admin SDK
const initializeFirebase = async () => {
  try {
    // Check if already initialized
    if (admin && admin.apps && admin.apps.length > 0) {
      console.log('✅ Firebase already initialized');
      return db;
    }

    // Lazy load firebase-admin only when needed
    try {
      admin = require('firebase-admin');
    } catch (err) {
      console.warn('⚠️ firebase-admin not available. Using mock Firestore.');
      return null;
    }

    // Try to load service account from multiple paths
    let serviceAccount = null;

    if (process.env.FIREBASE_SERVICE_ACCOUNT) {
      try {
        serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
        console.log(`✅ Service account loaded from Environment Variable`);
      } catch (err) {
        console.warn(`⚠️ Failed to parse FIREBASE_SERVICE_ACCOUNT env var: ${err.message}`);
      }
    }

    if (!serviceAccount) {
      const credentialsPaths = [
        path.join(__dirname, '../../firebase-key.json'), // backend/firebase-key.json
        process.env.GOOGLE_APPLICATION_CREDENTIALS || '',
        './firebase-key.json'
      ];

      for (const credPath of credentialsPaths) {
        if (credPath && fs.existsSync(credPath)) {
          try {
            const fileContent = fs.readFileSync(credPath, 'utf-8');
            serviceAccount = JSON.parse(fileContent);
            console.log(`✅ Service account loaded from: ${credPath}`);
            break;
          } catch (err) {
            console.warn(`⚠️ Failed to load credentials from ${credPath}: ${err.message}`);
          }
        }
      }
    }

    let projectId = process.env.FIREBASE_PROJECT_ID;
if (!projectId && serviceAccount && serviceAccount.project_id) {
  projectId = serviceAccount.project_id;
  console.log('✅ Project ID derived from service account credentials');
}
if (!projectId) {
  throw new Error('FIREBASE_PROJECT_ID is not defined and could not be inferred');
}
const options = { projectId };
    if (serviceAccount) {
      options.credential = admin.credential.cert(serviceAccount);
    } else {
      console.warn('⚠️ No service account credentials found. Using default credentials chain.');
    }

    admin.initializeApp(options);

    db = admin.firestore();
    
    console.log(`\n📦 Firebase Firestore Initialized: Project "${projectId}"`);
    return db;
  } catch (err) {
    console.error(`\n⚠️ Firebase Initialization Warning: ${err.message}`);
    return null;
  }
};

// Connection error handling
const connectFirebase = async () => {
  try {
    const firestore = await initializeFirebase();
    
    if (firestore) {
      console.log('✅ Firestore initialized successfully');
      return true;
    }
    return false;
  } catch (err) {
    console.error(`⚠️ Firestore initialization error: ${err.message}`);
    return false;
  }
};

module.exports = {
  initializeFirebase,
  connectFirebase,
  admin
};
