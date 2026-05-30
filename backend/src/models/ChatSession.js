/**
 * SmartLoan AI+ — Chat Session Model (Firestore)
 */
const { initializeFirebase } = require('../config/firebase');

class ChatSession {
  constructor(data = {}) {
    this.id = data.id || null;
    this.userId = data.userId || '';
    this.sessionId = data.sessionId || '';
    this.messages = data.messages || [];
    this.messageCount = data.messageCount || 0;
    this.lastActivity = data.lastActivity || new Date();
    this.createdAt = data.createdAt || new Date();
    this.updatedAt = data.updatedAt || new Date();
  }

  async save() {
    try {
      const db = await initializeFirebase();
      
      if (this.id) {
        await db.collection('chatSessions').doc(this.id).update({
          ...this,
          updatedAt: new Date()
        });
      } else {
        const docRef = await db.collection('chatSessions').add({
          ...this,
          createdAt: new Date(),
          updatedAt: new Date()
        });
        this.id = docRef.id;
      }
      return this;
    } catch (err) {
      console.error('Error saving chat session:', err);
      throw err;
    }
  }

  static async create(data) {
    const session = new ChatSession(data);
    await session.save();
    return session;
  }

  static async find(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('chatSessions');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }

      const snapshot = await queryRef.get();
      let docs = snapshot.docs.map(doc => new ChatSession({ ...doc.data(), id: doc.id }));
      docs.sort((a, b) => (b.lastActivity || 0) - (a.lastActivity || 0));
      return docs;
    } catch (err) {
      console.error('Error finding chat sessions:', err);
      return [];
    }
  }

  static async findOne(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('chatSessions');

      if (query.userId && query.sessionId) {
        queryRef = queryRef
          .where('userId', '==', query.userId)
          .where('sessionId', '==', query.sessionId);
      }

      const snapshot = await queryRef.limit(1).get();
      if (snapshot.empty) return null;

      const doc = snapshot.docs[0];
      return new ChatSession({ ...doc.data(), id: doc.id });
    } catch (err) {
      console.error('Error finding chat session:', err);
      return null;
    }
  }

  static async countDocuments(query = {}) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('chatSessions');

      if (query.userId) {
        queryRef = queryRef.where('userId', '==', query.userId);
      }

      const snapshot = await queryRef.get();
      return snapshot.size;
    } catch (err) {
      console.error('Error counting chat sessions:', err);
      return 0;
    }
  }

  static async findOneAndDelete(query) {
    try {
      const db = await initializeFirebase();
      if (query._id && query.userId) {
        const docRef = db.collection('chatSessions').doc(query._id);
        const doc = await docRef.get();
        if (doc.exists && doc.data().userId === query.userId) {
          await docRef.delete();
          return { deletedCount: 1 };
        }
      }
      return null;
    } catch (err) {
      console.error('Error deleting chat session:', err);
      return null;
    }
  }
}

module.exports = ChatSession;
