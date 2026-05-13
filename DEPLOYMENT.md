# 🚀 Deployment & Environment Setup Guide

## Quick Environment Setup

### Backend (.env File)
```bash
# Create file: backend/.env

# Server Configuration
NODE_ENV=development
PORT=5000

# Database
MONGODB_URI=mongodb+srv://username:password@cluster-name.mongodb.net/smartloan?retryWrites=true&w=majority

# Authentication
JWT_SECRET=your_super_secret_jwt_key_minimum_32_characters_here

# ML Service
ML_SERVICE_URL=http://localhost:8000

# CORS - Allow Mobile Origins (comma-separated)
MOBILE_ORIGINS=http://10.0.2.2:5000,http://localhost:5000

# Optional: Firebase, Sentry, etc.
# FIREBASE_PROJECT_ID=your-project
# SENTRY_DSN=https://...
```

### ML Service (.env File)
```bash
# Create file: ml-service/.env

# Python Configuration
PYTHONUNBUFFERED=1
PYTHONDONTWRITEBYTECODE=1

# Server
PORT=8000
HOST=0.0.0.0

# Logging
LOG_LEVEL=INFO

# Model Configuration
MODEL_PATH=./models
BATCH_SIZE=32

# Optional: Monitoring
# SENTRY_DSN=https://...
```

### Backend Development Start
```bash
# Install dependencies
cd backend
npm install

# Create .env file with values above
nano .env  # or edit in VS Code

# Start development server with auto-reload
npm run dev

# Backend should be running on http://localhost:5000
# Test: curl http://localhost:5000/api/health
```

### ML Service Development Start
```bash
# Create virtual environment
cd ml-service
python -m venv venv

# Activate venv
# On Windows:
venv\Scripts\activate
# On macOS/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Create .env file (if needed)
# Start server
python main.py

# ML Service should be running on http://localhost:8000
# Test: curl http://localhost:8000/docs (Swagger UI)
```

---

## Production Deployment Guide

### Option 1: Railway.app (Recommended for Beginners)

#### Backend Deployment
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Initialize project
cd backend
railway init

# Link to Railway project
railway link

# Set environment variables in Railway dashboard
# MONGODB_URI, JWT_SECRET, ML_SERVICE_URL, MOBILE_ORIGINS

# Deploy
git push  # Auto-deploys on push to main branch
```

#### ML Service Deployment
```bash
# Create Python project in Railway dashboard
# Set environment variables
# Connect GitHub repository
# Auto-deploys on push

# Update backend's ML_SERVICE_URL to Railway URL
```

### Option 2: Render.com

#### Backend Deployment
```
1. Push code to GitHub
2. Go to Render.com
3. Create New Web Service
4. Connect GitHub repository (backend)
5. Set Environment Variables:
   - NODE_ENV=production
   - MONGODB_URI=...
   - JWT_SECRET=...
   - ML_SERVICE_URL=https://ml-service-name.onrender.com
   - MOBILE_ORIGINS=https://android-app.com
6. Deploy!
```

#### ML Service Deployment
```
1. Go to Render.com
2. Create New Web Service
3. Connect GitHub repository (ml-service)
4. Build Command: pip install -r requirements.txt
5. Start Command: uvicorn main:app --host 0.0.0.0 --port 8000
6. Set Environment Variables
7. Deploy!
```

### Option 3: Heroku (Paid)

#### Backend Setup
```bash
# Install Heroku CLI
# Create Procfile in backend/
echo "web: npm start" > Procfile

# Deploy
heroku create smartloan-backend
heroku config:set MONGODB_URI="..."
heroku config:set JWT_SECRET="..."
git push heroku main
```

#### ML Service Setup
```bash
# Create Procfile in ml-service/
echo "web: uvicorn main:app --host 0.0.0.0 --port \$PORT" > Procfile

heroku create smartloan-ml
git push heroku main
```

---

## Docker Deployment (Advanced)

### Backend Dockerfile
```dockerfile
# Create: backend/Dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install --production

COPY . .

EXPOSE 5000

CMD ["npm", "start"]
```

### ML Service Dockerfile (Already Included)
```dockerfile
# ml-service/Dockerfile already exists
FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### Docker Compose (Local Full Stack)
```yaml
# Create: docker-compose.yml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "5000:5000"
    environment:
      - MONGODB_URI=mongodb://mongo:27017/smartloan
      - ML_SERVICE_URL=http://ml-service:8000
    depends_on:
      - mongo
      - ml-service

  ml-service:
    build: ./ml-service
    ports:
      - "8000:8000"

  mongo:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db

volumes:
  mongo-data:
```

**Run entire stack locally:**
```bash
docker-compose up
```

---

## Android App Configuration

### Build Configuration (android/app/build.gradle)
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.smartloan.ai"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
        
        // API Configuration
        buildConfigField "String", "API_BASE_URL", '"https://api.smartloan.ai"'
        buildConfigField "String", "ML_SERVICE_URL", '"https://ml.smartloan.ai"'
    }
    
    buildTypes {
        debug {
            buildConfigField "String", "API_BASE_URL", '"http://10.0.2.2:5000"'
            buildConfigField "String", "ML_SERVICE_URL", '"http://10.0.2.2:8000"'
        }
        
        release {
            minifyEnabled true
            buildConfigField "String", "API_BASE_URL", '"https://api.smartloan.ai"'
            buildConfigField "String", "ML_SERVICE_URL", '"https://ml.smartloan.ai"'
        }
    }
}

dependencies {
    // Retrofit for API calls
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // JWT handling
    implementation 'com.auth0:java-jwt:4.4.0'
    
    // Room for local storage
    implementation 'androidx.room:room-runtime:2.5.1'
    
    // Other essential libraries...
}
```

### Retrofit API Client Setup
```kotlin
// Create: android/app/src/main/java/com/smartloan/ai/api/ApiClient.kt

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("loans/predict")
    suspend fun predictLoan(@Body request: LoanPredictionRequest): Response<PredictionResponse>
    
    // Add other endpoints...
}
```

### Android Manifest Configuration
```xml
<!-- android/app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- For document uploads -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <application
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:theme="@style/Theme.SmartLoan">
        
        <!-- Activities, Services, etc. -->
        
    </application>
</manifest>
```

---

## Production Checklist

### Before Deploying
- [ ] Backend: All environment variables set correctly
- [ ] ML Service: All models loaded and tested
- [ ] Database: MongoDB Atlas cluster active and whitelisted
- [ ] CORS: Mobile origins configured on backend
- [ ] JWT Secret: Strong, random, 32+ characters
- [ ] TLS/HTTPS: Enabled on all services
- [ ] Rate Limiting: Configured for expected load
- [ ] Monitoring: Sentry/DataDog configured (optional)

### After Deploying
- [ ] Backend health check: GET /api/health
- [ ] ML Service health: GET /health
- [ ] Test login flow on Android app
- [ ] Test prediction with demo user
- [ ] Monitor logs for errors
- [ ] Set up alerts for downtime

### Security Checklist
- [ ] HTTPS/TLS enforced
- [ ] MongoDB encryption enabled
- [ ] JWT secrets not in version control
- [ ] API keys rotated regularly
- [ ] Rate limiting active
- [ ] CORS restricted to mobile origins
- [ ] Regular security audits scheduled

---

## Monitoring & Maintenance

### Backend Monitoring
```bash
# Check logs (Railway/Render)
railway logs  # Railway
# Or via web dashboard

# Monitor database size
# In MongoDB Atlas dashboard > Metrics

# Set up alerts for:
# - API response time > 1000ms
# - Error rate > 5%
# - Database connection failures
```

### ML Service Monitoring
```bash
# Check model accuracy
# Run periodic validation on test dataset

# Monitor inference time
# Track predictions per minute

# Alerts for:
# - Inference time > 1000ms
# - Model load failures
# - Memory usage > 2GB
```

### Database Monitoring
```bash
# MongoDB Atlas Metrics:
# - Connection count
# - Query latency
# - Database size growth
# - Backup status

# Set backups: Weekly automated snapshots
```

---

## Update & Maintenance Procedures

### Updating Dependencies
```bash
# Backend
cd backend
npm outdated        # Check for updates
npm update          # Update to latest compatible versions
npm audit fix       # Security updates

# ML Service
cd ml-service
pip list --outdated
pip install --upgrade <package-name>
```

### Model Retraining
```bash
# Periodic retraining (monthly/quarterly)
cd ml-service
python training/train_models.py

# Validate model performance
python tests/test_engines.py

# Update models in production
# Upload new .pkl files to deployment environment
```

### Database Backups
```bash
# MongoDB Atlas handles automated backups
# Weekly snapshots retained for 30 days
# Manual backup before major deployments
```

---

## Troubleshooting Deployment

### Backend won't start
```bash
# Check environment variables
echo $MONGODB_URI
echo $JWT_SECRET

# Check database connection
# Try connecting directly: mongosh "mongodb+srv://..."

# Review logs for specific errors
```

### ML Service not responding
```bash
# Check if Python dependencies installed
pip list

# Verify model files exist
ls -la models/

# Check port is not in use
lsof -i :8000

# Run with verbose logging
python main.py --log-level DEBUG
```

### Android can't connect to backend
```bash
# Development: Use 10.0.2.2 for emulator
# Production: Verify CORS includes app domain
# Check firewall rules on deployment platform

# Test endpoint directly in Postman
# GET http://10.0.2.2:5000/api/health
```

---

**Deployment guide complete! Your production environment is ready.** ✅