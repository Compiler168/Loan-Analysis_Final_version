/**
 * SmartLoan AI+ — Prediction Model (Firestore)
 */
const { initializeFirebase } = require('../config/firebase');

class Prediction {
  constructor(data = {}) {
    this.id = data.id || null;
    this.userId = data.userId || '';
    this.input = data.input || {};
    this.result = data.result || {};
    this.status = data.status || 'review';
    this.createdAt = data.createdAt || new Date();
    this.updatedAt = data.updatedAt || new Date();
  }

  async save() {
    try {
      const db = await initializeFirebase();
      
      if (this.id) {
        await db.collection('predictions').doc(this.id).update({
          ...this,
          updatedAt: new Date()
        });
      } else {
        const docRef = await db.collection('predictions').add({
          ...this,
          createdAt: new Date(),
          updatedAt: new Date()
        });
        this.id = docRef.id;
      }
      return this;
    } catch (err) {
      console.error('Error saving prediction:', err);
      throw err;
    }
  }

  static async create(data) {
    const prediction = new Prediction(data);
    await prediction.save();
    return prediction;
  }

  static async find(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('predictions');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }

      const snapshot = await queryRef.get();
      let docs = snapshot.docs.map(doc => new Prediction({ ...doc.data(), id: doc.id }));
      docs.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
      return docs.slice(0, 50);
    } catch (err) {
      console.error('Error finding predictions:', err);
      return [];
    }
  }

  static async findOne(query) {
    try {
      const db = await initializeFirebase();

      // Case 1: Lookup by document ID + userId
      if (query._id && query.userId) {
        const doc = await db.collection('predictions').doc(query._id).get();
        if (doc.exists && doc.data().userId === query.userId) {
          return new Prediction({ ...doc.data(), id: doc.id });
        }
        return null;
      }

      // Case 2: Query by userId (returns latest by createdAt)
      let queryRef = db.collection('predictions');
      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }

      const snapshot = await queryRef.get();
      if (snapshot.empty) return null;

      let docs = snapshot.docs.map(doc => new Prediction({ ...doc.data(), id: doc.id }));
      // Sort by createdAt descending to get the latest
      docs.sort((a, b) => {
        const aTime = a.createdAt instanceof Date ? a.createdAt.getTime() : new Date(a.createdAt).getTime();
        const bTime = b.createdAt instanceof Date ? b.createdAt.getTime() : new Date(b.createdAt).getTime();
        return bTime - aTime;
      });
      return docs[0];
    } catch (err) {
      console.error('Error finding prediction:', err);
      return null;
    }
  }

  static async countDocuments(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('predictions');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }
      if (query.status) {
        queryRef = queryRef.where('status', '==', query.status);
      }

      const snapshot = await queryRef.get();
      return snapshot.size;
    } catch (err) {
      console.error('Error counting predictions:', err);
      return 0;
    }
  }

  static async findOneAndDelete(query) {
    try {
      const db = await initializeFirebase();
      if (query._id && query.userId) {
        const docRef = db.collection('predictions').doc(query._id);
        const doc = await docRef.get();
        if (doc.exists && doc.data().userId === query.userId) {
          await docRef.delete();
          return { deletedCount: 1 };
        }
      }
      return null;
    } catch (err) {
      console.error('Error deleting prediction:', err);
      return null;
    }
  }
}

module.exports = Prediction;
