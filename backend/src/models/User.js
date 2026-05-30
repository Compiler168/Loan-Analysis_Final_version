/**
 * SmartLoan AI+ — User Model (Firestore)
 */
const bcrypt = require('bcryptjs');
const { initializeFirebase } = require('../config/firebase');

class User {
  constructor(data = {}) {
    this.id = data.id || null;
    this.name = data.name || '';
    this.email = data.email || '';
    this.password = data.password || '';
    this.role = data.role || 'user';
    this.profile = data.profile || {
      monthly_income: 0,
      monthly_expenses: 0,
      credit_score: 650,
      employment_status: 'salaried',
      employment_years: 0,
      existing_loans: 0,
      existing_emi: 0,
      savings_balance: 0,
      dependents: 0,
      age: 30,
      property_value: 0,
    };
    this.lastLogin = data.lastLogin || new Date();
    this.status = data.status || 'active';
    this.createdAt = data.createdAt || new Date();
    this.updatedAt = data.updatedAt || new Date();
  }

  async hashPassword() {
    // Only hash if password is provided and doesn't look like an already-hashed bcrypt string
    if (this.password && !this.password.startsWith('$2a$') && !this.password.startsWith('$2b$')) {
      this.password = await bcrypt.hash(this.password, 10);
    }
  }

  async comparePassword(candidatePassword) {
    return bcrypt.compare(candidatePassword, this.password);
  }

  toJSON() {
    const { password, ...obj } = this;
    return obj;
  }

  static async findOne(query) {
    try {
      const db = await initializeFirebase();
      let queryRef = db.collection('users');

      if (query.email) {
        queryRef = queryRef.where('email', '==', query.email.toLowerCase());
      }
      if (query._id) {
        return this.findById(query._id);
      }

      const snapshot = await queryRef.limit(1).get();
      if (snapshot.empty) return null;

      const doc = snapshot.docs[0];
      return new User({ ...doc.data(), id: doc.id });
    } catch (err) {
      console.error('Error finding user:', err);
      return null;
    }
  }

  static async findById(id) {
    try {
      const db = await initializeFirebase();
      const doc = await db.collection('users').doc(id).get();
      if (!doc.exists) return null;
      return new User({ ...doc.data(), id: doc.id });
    } catch (err) {
      console.error('Error finding user by ID:', err);
      return null;
    }
  }

  async save() {
    try {
      await this.hashPassword();
      const db = await initializeFirebase();
      
      const userData = {
        name: this.name,
        email: this.email.toLowerCase(),
        password: this.password,
        role: this.role,
        profile: this.profile,
        status: this.status,
        lastLogin: this.lastLogin,
        createdAt: this.createdAt,
        updatedAt: new Date()
      };

      if (this.id) {
        // Update existing document
        await db.collection('users').doc(this.id).update(userData);
      } else {
        // Create new document
        userData.createdAt = new Date();
        const docRef = await db.collection('users').add(userData);
        this.id = docRef.id;
      }
      return this;
    } catch (err) {
      console.error('Error saving user:', err);
      throw err;
    }
  }

  static async countDocuments(query = {}) {
    try {
      const db = await initializeFirebase();
      const snapshot = await db.collection('users').get();
      return snapshot.size;
    } catch (err) {
      console.error('Error counting users:', err);
      return 0;
    }
  }

  static async create(userData) {
    const user = new User(userData);
    await user.save();
    return user;
  }
}

module.exports = User;
