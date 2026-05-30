/**
 * SmartLoan AI+ — Report Model (Firestore)
 */
const { initializeFirebase } = require('../config/firebase');

class Report {
  constructor(data = {}) {
    this.id = data.id || null;
    this.userId = data.userId || '';
    this.type = data.type || 'financial_summary';
    this.title = data.title || '';
    this.sections = data.sections || [];
    this.metadata = data.metadata || {
      health_score: 0,
      loan_probability: 0,
      risk_level: 'medium',
      credit_score: 650,
    };
    this.createdAt = data.createdAt || new Date();
    this.updatedAt = data.updatedAt || new Date();
  }

  async save() {
    try {
      const db = await initializeFirebase();
      
      if (this.id) {
        await db.collection('reports').doc(this.id).update({
          ...this,
          updatedAt: new Date()
        });
      } else {
        const docRef = await db.collection('reports').add({
          ...this,
          createdAt: new Date(),
          updatedAt: new Date()
        });
        this.id = docRef.id;
      }
      return this;
    } catch (err) {
      console.error('Error saving report:', err);
      throw err;
    }
  }

  static async create(data) {
    const report = new Report(data);
    await report.save();
    return report;
  }

  static async find(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('reports');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }

      const snapshot = await queryRef.get();
      let docs = snapshot.docs.map(doc => new Report({ ...doc.data(), id: doc.id }));
      docs.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
      return docs;
    } catch (err) {
      console.error('Error finding reports:', err);
      return [];
    }
  }

  static async findOne(query) {
    try {
      const db = await initializeFirebase();
      
      if (query._id && query.userId) {
        const doc = await db.collection('reports').doc(query._id).get();
        if (doc.exists && doc.data().userId === query.userId) {
          return new Report({ ...doc.data(), id: doc.id });
        }
      }
      return null;
    } catch (err) {
      console.error('Error finding report:', err);
      return null;
    }
  }

  static async findOneAndDelete(query) {
    try {
      const db = await initializeFirebase();
      if (query._id && query.userId) {
        const docRef = db.collection('reports').doc(query._id);
        const doc = await docRef.get();
        if (doc.exists && doc.data().userId === query.userId) {
          await docRef.delete();
          return { deletedCount: 1 };
        }
      }
      return null;
    } catch (err) {
      console.error('Error deleting report:', err);
      return null;
    }
  }

  static async select(fields) {
    // Placeholder for chaining
    return this;
  }

  static async sort(order = {}) {
    // Placeholder for chaining
    return this;
  }
}

module.exports = Report;
