/**
 * SmartLoan AI+ — Analysis History Model (Firestore)
 */
const { initializeFirebase } = require('../config/firebase');

class Analysis {
  constructor(data = {}) {
    this.id = data.id || null;
    this.userId = data.userId || '';
    this.type = data.type || 'health_score';
    this.input = data.input || {};
    this.result = data.result || {};
    this.createdAt = data.createdAt || new Date();
    this.updatedAt = data.updatedAt || new Date();
  }

  async save() {
    try {
      const db = await initializeFirebase();
      
      if (this.id) {
        await db.collection('analyses').doc(this.id).update({
          ...this,
          updatedAt: new Date()
        });
      } else {
        const docRef = await db.collection('analyses').add({
          ...this,
          createdAt: new Date(),
          updatedAt: new Date()
        });
        this.id = docRef.id;
      }
      return this;
    } catch (err) {
      console.error('Error saving analysis:', err);
      throw err;
    }
  }

  static async create(data) {
    const analysis = new Analysis(data);
    await analysis.save();
    return analysis;
  }

  static async find(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('analyses');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }

      const snapshot = await queryRef.get();
      let docs = snapshot.docs.map(doc => new Analysis({ ...doc.data(), id: doc.id }));
      docs.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
      return docs;
    } catch (err) {
      console.error('Error finding analyses:', err);
      return [];
    }
  }

  static async findOne(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('analyses');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }
      if (query.type) {
        queryRef = queryRef.where('type', '==', query.type);
      }

      const snapshot = await queryRef.get();
      if (snapshot.empty) return null;

      let docs = snapshot.docs.map(doc => new Analysis({ ...doc.data(), id: doc.id }));
      // Sort by createdAt descending to get the latest
      docs.sort((a, b) => {
        const aTime = a.createdAt instanceof Date ? a.createdAt.getTime() : new Date(a.createdAt).getTime();
        const bTime = b.createdAt instanceof Date ? b.createdAt.getTime() : new Date(b.createdAt).getTime();
        return bTime - aTime;
      });
      return docs[0];
    } catch (err) {
      console.error('Error finding analysis:', err);
      return null;
    }
  }

  static async countDocuments(query = {}) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('analyses');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }

      const snapshot = await queryRef.get();
      return snapshot.size;
    } catch (err) {
      console.error('Error counting analyses:', err);
      return 0;
    }
  }
}

module.exports = Analysis;
