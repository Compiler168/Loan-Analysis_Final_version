# 🏦 SmartLoan AI+

**Intelligent Financial Advisory & Loan Prediction Platform**

A mobile-first, AI-powered fintech platform featuring a native Android application, intelligent ML models for loan prediction, a robust Express.js REST API backend, and custom-built AI/ML services. Designed for seamless mobile user experience with enterprise-grade security and performance.

---

## 📑 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Quick Start (Local Development)](#quick-start-local-development)
- [Deployment](#deployment)
- [API Reference](#api-reference)
- [Environment Variables](#environment-variables)
- [Testing](#testing)
- [Demo Credentials](#demo-credentials)
- [License](#license)

---

## Architecture Overview

SmartLoan AI+ uses a **3-tier microservice architecture** optimized for mobile applications:

```
┌──────────────────────────────────────────────────────────────────┐
│                     MOBILE CLIENT LAYER                          │
│           ┌─────────────────────────────────────┐                │
│           │    Native Android App (Kotlin/Java) │                │
│           │    Material Design 3 Components     │                │
│           └────────────────┬────────────────────┘                │
│                            │                                     │
├────────────────────────────┼─────────────────────────────────────┤
│                            ▼      API GATEWAY                    │
│           ┌──────────────────────────────────┐                  │
│           │  Express.js Backend (Node.js)    │                  │
│           │  JWT Auth · Security · Rate Limit│                  │
│           └────────────────┬─────────────────┘                  │
│                            │                                     │
├────────────────────────────┼─────────────────────────────────────┤
│                            ▼    AI/ML SERVICES                   │
│           ┌──────────────────────────────────┐                  │
│           │  FastAPI ML Service (Python)     │                  │
│           │  Predictions · NLP · Analytics   │                  │
│           └──────────────────────────────────┘                  │
│                                                                  │
├──────────────────────────────────────────────────────────────────┤
│                      DATA PERSISTENCE LAYER                      │
│  ┌────────────────────┐      ┌──────────────────────┐           │
│  │ MongoDB Atlas      │      │ Trained ML Models    │           │
│  │ (User Data & Logs) │      │ (.pkl files)         │           │
│  └────────────────────┘      └──────────────────────┘           │
└──────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Mobile Frontend (Android)
| Technology | Purpose |
|---|---|
| **Kotlin/Java** | Native Android development |
| **Android Studio** | IDE and build system |
| **Material Design 3** | Modern UI components and guidelines |
| **Retrofit** | Type-safe HTTP client |
| **Room Database** | Local data persistence |
| **Jetpack** | AndroidX libraries (Navigation, ViewModel, etc.) |

### Backend API
| Technology | Purpose |
|---|---|
| **Express.js** | REST API framework |
| **Mongoose** | MongoDB ODM for data modeling |
| **JWT (JSON Web Tokens)** | Stateless authentication (7-day expiry) |
| **bcryptjs** | Password hashing with 10-round salt |
| **Helmet** | HTTP security headers |
| **express-rate-limit** | API rate limiting (200 req/15min, 50 req/15min for AI) |
| **express-validator** | Request validation and sanitization |
| **Multer** | File upload handling (documents, reports) |
| **Axios** | Internal HTTP client for ML service calls |
| **CORS** | Cross-origin resource sharing for mobile origins |

### AI/ML Service
| Technology | Purpose |
|---|---|
| **FastAPI** | High-performance async Python API |
| **Scikit-Learn** | Logistic Regression, Random Forest, StandardScaler |
| **XGBoost** | Gradient boosted trees (primary prediction model) |
| **NLTK** | Natural language processing for chatbot |
| **Pandas & NumPy** | Data manipulation and analysis |
| **Joblib** | ML model persistence (.pkl files) |
| **PDFPlumber** | Document analysis and data extraction |

### Database
| Technology | Purpose |
|---|---|
| **MongoDB Atlas** | Cloud NoSQL database for users, predictions, chat sessions |
| **Mongoose Schema** | Data validation and relationships |

---

## Features

### 🤖 AI/ML Capabilities
- **Loan Prediction Engine**: XGBoost ensemble model with 85%+ accuracy
- **Credit Health Scoring**: Real-time financial health assessment
- **Risk Analysis**: Comprehensive loan risk evaluation
- **NLP Chatbot**: Natural language financial advisory
- **Simulation Engine**: Loan scenario analysis
- **Document Analysis**: Automatic document parsing and data extraction

### 📱 Mobile App Features
- **User Authentication**: Secure JWT-based authentication with 7-day session
- **Loan Prediction**: Request real-time loan approval predictions
- **Financial Dashboard**: View financial metrics, health scores, and history
- **Chat Support**: AI-powered financial advisory chatbot
- **Report Generation**: Download PDF reports
- **Secure Data Storage**: Local and cloud persistence with encryption
- **Offline Support**: Basic offline functionality with cached data

### 🔐 Security Features
- Helmet security headers
- CORS with mobile-specific origins
- JWT token-based authentication
- Password hashing with bcryptjs (10-round salt)
- Rate limiting (200 req/15min general, 50 req/15min for AI operations)
- Request validation and sanitization
- Helmet content security policies
- HTTPS-ready configuration

---

## Project Structure

```
smartloan-ai-mobile/
├── android/                    # Native Android Application
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/                    # Kotlin/Java source
│   │       │   │   ├── ui/                  # Activities, Fragments
│   │       │   │   ├── viewmodel/           # ViewModels
│   │       │   │   ├── repository/          # Data repositories
│   │       │   │   ├── api/                 # Retrofit API client
│   │       │   │   ├── database/            # Room database
│   │       │   │   └── util/                # Utilities
│   │       │   ├── res/                     # Resources (layouts, strings, drawable)
│   │       │   └── AndroidManifest.xml
│   │       ├── test/                        # Unit tests
│   │       └── androidTest/                 # Instrumented tests
│   ├── gradle/
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradle.properties
│
├── backend/                    # Express.js API Server
│   ├── src/
│   │   ├── server.js                        # Express app entry point
│   │   ├── config/
│   │   │   └── database.js                  # MongoDB connection
│   │   ├── middleware/
│   │   │   └── auth.js                      # JWT authentication
│   │   ├── models/                          # Mongoose schemas
│   │   │   ├── User.js
│   │   │   ├── Prediction.js
│   │   │   ├── ChatSession.js
│   │   │   ├── Analysis.js
│   │   │   └── Report.js
│   │   └── routes/                          # API endpoints
│   │       ├── auth.js                      # Authentication (register, login)
│   │       ├── loans.js                     # Loan prediction & history
│   │       ├── financial.js                 # Financial metrics
│   │       ├── chat.js                      # Chatbot
│   │       └── reports.js                   # Report generation
│   ├── package.json                         # Dependencies
│   └── .env                                 # Environment configuration
│
├── ml-service/                 # FastAPI ML Service
│   ├── main.py                              # FastAPI app entry point
│   ├── requirements.txt                     # Python dependencies
│   ├── services/                            # ML engines
│   │   ├── prediction_engine.py             # XGBoost predictions
│   │   ├── health_scorer.py                 # Health scoring
│   │   ├── risk_analyzer.py                 # Risk analysis
│   │   ├── nlp_engine.py                    # Chatbot NLP
│   │   ├── simulation_engine.py             # Scenario simulation
│   │   └── document_analyzer.py             # Document parsing
│   ├── models/                              # Trained ML models
│   │   ├── xgboost_model.pkl                # Primary prediction model
│   │   ├── rf_model.pkl                     # Random forest model
│   │   ├── lr_model.pkl                     # Logistic regression
│   │   └── model_metadata.json
│   ├── data/
│   │   └── loan_dataset.csv                 # Training dataset (12K samples)
│   ├── training/
│   │   ├── train_models.py
│   │   └── generate_data.py
│   ├── tests/
│   │   └── test_engines.py
│   ├── Dockerfile                           # Container configuration
│   └── render.yaml                          # Deployment configuration
│
├── README.md                   # Project documentation (this file)
└── .gitignore

```

---

## Quick Start (Local Development)

### Prerequisites
- **Node.js 18+** (for backend)
- **Python 3.8+** (for ML service)
- **MongoDB Atlas** account (or local MongoDB)
- **Android Studio** (for mobile app development)
- **Git**

### 1. Backend Setup

```bash
cd backend
npm install

# Create .env file
cat > .env << EOF
PORT=5000
NODE_ENV=development
MONGODB_URI=mongodb+srv://<user>:<password>@cluster.mongodb.net/smartloan?retryWrites=true
JWT_SECRET=your_jwt_secret_key_here
ML_SERVICE_URL=http://localhost:8000
MOBILE_ORIGINS=http://localhost:5000
EOF

# Start backend
npm run dev
```

Backend will run on `http://localhost:5000`

### 2. ML Service Setup

```bash
cd ml-service
python -m venv venv

# On Windows
venv\Scripts\activate
# On macOS/Linux
source venv/bin/activate

pip install -r requirements.txt

# Start ML service
python main.py
```

ML Service will run on `http://localhost:8000`

### 3. Android App Setup

```bash
cd android
# Open in Android Studio: File > Open > select android folder
# Or build from command line:
./gradlew build

# Run on emulator or device:
./gradlew installDebug
```

---

## API Reference

### Base URL
- **Development**: `http://localhost:5000/api`
- **Production**: `https://api.smartloan.ai/api`

### Authentication
All endpoints (except `/auth/register` and `/auth/login`) require JWT token in header:
```
Authorization: Bearer <token>
```

### Core Endpoints

#### Auth
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token
- `GET /auth/profile` - Get user profile

#### Loans
- `POST /loans/predict` - Get loan prediction
- `GET /loans/history` - Get prediction history

#### Financial
- `GET /financial/metrics` - Get financial metrics
- `POST /financial/analyze` - Financial analysis

#### Chat
- `POST /chat/message` - Send message to chatbot
- `GET /chat/history` - Get chat history

#### Reports
- `POST /reports/generate` - Generate PDF report
- `GET /reports/list` - List user reports

---

## Deployment

### Backend (Node.js)
**Recommended Platforms**:
- Railway.app
- Render.com
- Heroku (with paid dynos)
- AWS EC2 + Load Balancer
- DigitalOcean App Platform

**Example with Render.com**:
```bash
git push main  # Trigger auto-deployment
```

### ML Service (Python)
**Recommended Platforms**:
- Railway.app
- Render.com (includes `render.yaml`)
- AWS EC2 + Elastic Beanstalk
- Google Cloud Run
- DigitalOcean App Platform

**Docker Deployment**:
```bash
docker build -t smartloan-ml .
docker run -p 8000:8000 smartloan-ml
```

### Environment Variables (Production)

**Backend (.env)**:
```
NODE_ENV=production
PORT=5000
MONGODB_URI=<production_mongodb_atlas_uri>
JWT_SECRET=<strong_random_secret>
ML_SERVICE_URL=https://ml-service.example.com
MOBILE_ORIGINS=https://android-app-domain.com,https://app.example.com
```

**ML Service (.env)**:
```
PYTHONUNBUFFERED=1
MODEL_PATH=/models
LOG_LEVEL=INFO
```

---

## Testing

### Backend Tests
```bash
cd backend
npm test
```

### ML Service Tests
```bash
cd ml-service
source venv/bin/activate
pytest tests/
```

### Android Tests
```bash
cd android
./gradlew test              # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

---

## Demo Credentials

For quick testing with in-memory fallback:

```
Email: demo@smartloan.ai
Password: demo123
Role: Admin
```

Note: Requires MongoDB Atlas connection to persist data beyond session.

---

## Environment Variables

### Backend
| Variable | Description | Example |
|---|---|---|
| `PORT` | Server port | `5000` |
| `NODE_ENV` | Environment | `development` \| `production` |
| `MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/db` |
| `JWT_SECRET` | JWT signing secret | `your_random_secret_key` |
| `ML_SERVICE_URL` | ML service base URL | `http://localhost:8000` |
| `MOBILE_ORIGINS` | Allowed mobile origins (comma-separated) | `http://localhost:5000` |

### ML Service
| Variable | Description | Example |
|---|---|---|
| `PYTHONUNBUFFERED` | Python output buffering | `1` |
| `MODEL_PATH` | Path to trained models | `/models` |
| `LOG_LEVEL` | Logging level | `INFO` |
| `PORT` | ML service port | `8000` |

---

## Performance Metrics

### Model Accuracy
- **XGBoost** (Ensemble): 85.2%
- **Random Forest**: 82.1%
- **Logistic Regression**: 79.3%

### API Response Times (Development)
- `/loans/predict`: ~500-800ms
- `/financial/metrics`: ~150-250ms
- `/chat/message`: ~300-600ms
- `/reports/generate`: ~1-3 seconds

---

## Security Checklist

- ✅ HTTPS enforced in production
- ✅ JWT token expiry (7 days)
- ✅ Password hashing (bcryptjs 10-round salt)
- ✅ Rate limiting enabled
- ✅ Input validation on all endpoints
- ✅ Helmet security headers
- ✅ CORS configured for mobile origins only
- ✅ Sensitive data not logged
- ✅ Environment variables for secrets
- ✅ Regular model updates

---

## Troubleshooting

### Backend won't connect to ML service
- Ensure ML service is running on `http://localhost:8000`
- Check `ML_SERVICE_URL` in `.env`
- Verify no firewall blocking port 8000

### MongoDB connection fails
- Check `MONGODB_URI` in `.env`
- Verify MongoDB Atlas cluster is active
- Whitelist your IP in Atlas security settings

### Android app can't reach backend
- Use `http://10.0.2.2:5000` for Android emulator (instead of `http://localhost:5000`)
- For physical device, use actual machine IP address
- Verify backend CORS includes mobile origin

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Support

For issues, feature requests, or questions:
- 📧 Email: support@smartloan.ai
- 💬 Issues: GitHub Issues
- 📱 Mobile App Support: In-app help chat

---

**Made with ❤️ for Mobile-First Financial Technology**
| **Pandas + NumPy** | Data processing and feature engineering |
| **NLTK** | NLP text processing |
| **pdfplumber** | PDF document text extraction |
| **Joblib** | Model serialization/deserialization |
| **Pydantic** | Request/response data validation |
| **Uvicorn** | ASGI production server |

### Android App
| Technology | Purpose |
|---|---|
| **Android SDK 34** (Java) | Native Android development |
| **Material Design 3** | Modern UI components |
| **Retrofit 2** + OkHttp 4 | Type-safe REST API client |
| **Gson** | JSON serialization |
| **Navigation Component** | Fragment-based navigation |
| **ViewModel + LiveData** | MVVM architecture |
| **MPAndroidChart** | Native chart rendering |
| **Glide** | Image loading and caching |
| **ViewBinding** | Type-safe view references |

### Database & Infrastructure
| Technology | Purpose |
|---|---|
| **MongoDB Atlas** | Cloud-hosted NoSQL database |
| **Vercel** | Frontend + backend serverless deployment |
| **Render** | ML service Docker deployment |
| **Docker** | ML service containerization |

---

## Features

| Feature | Description |
|---|---|
| 📊 **AI Dashboard** | Real-time financial stats, AI insights, income/expense charts (Area, Bar, Radar), EMI forecast, growth tracking |
| 🏦 **Loan Prediction** | Ensemble ML prediction (LR + RF + XGB) with per-model probability, risk factors, and feature importance |
| 💯 **Financial Health Score** | Custom 0-100 scoring algorithm across 5 dimensions: Savings, Spending, Debt, Credit, Stability |
| ⚠️ **Risk Analyzer** | 6-dimension risk assessment (DTI, EMI Burden, Credit, Savings, Default, Overspending) with severity heatmap |
| 🤖 **AI Chatbot** | Custom NLP engine with 18 intent categories, pattern matching, context memory, and financial reasoning |
| 🔮 **Financial Simulator** | What-if projections comparing baseline vs. scenario trajectories over 6-60 months |
| 📄 **Document Analyzer** | PDF/text upload with regex-based financial data extraction and risk detection |
| 📝 **Reports** | Downloadable financial analysis reports with historical data |
| 👤 **Admin Panel** | User management, ML model metrics, platform analytics |
| 🔐 **Auth System** | JWT-based authentication, bcrypt password hashing, role-based access (user/admin) |
| 🌙 **Dark/Light Mode** | Full theme switching with next-themes |
| 📱 **Android App** | Native companion app with all features via Retrofit REST client |

---

## AI/ML Deep Dive

### Trained Models

The platform trains **3 machine learning models** on a synthetically generated financial dataset of **12,000 samples** with **21 engineered features**:

| Model | Algorithm | Configuration | Accuracy | F1 Score | ROC-AUC |
|---|---|---|---|---|---|
| **Logistic Regression** | Linear classifier | C=1.0, LBFGS solver, StandardScaler | 85.1% | 0.863 | 0.925 |
| **Random Forest** | Bagged decision trees | 200 trees, max_depth=15, min_samples_split=5 | 88.0% | 0.888 | 0.955 |
| **XGBoost** | Gradient boosted trees | 200 estimators, max_depth=8, lr=0.1 | 87.5% | 0.884 | 0.958 |

### Ensemble Strategy

The final prediction uses a **weighted ensemble**:

```
Final Score = LR(0.20) + RF(0.40) + XGB(0.40)
```

- All three models vote independently
- Confidence is measured by model agreement (high: ≥ 2/3 agree, medium: ≥ 1/3, low: none agree)
- Feature importance is extracted from Random Forest for explainability

### 21 Input Features

**16 raw features** collected from the user:
`age`, `dependents`, `employment_status`, `employment_years`, `monthly_income`, `monthly_expenses`, `credit_score`, `existing_loans`, `existing_emi`, `loan_amount`, `loan_term_months`, `interest_rate`, `property_value`, `savings_balance`, `missed_payments_last_year`, `bankruptcies`

**5 derived/engineered features** computed at prediction time:
- `dti_ratio` — Debt-to-Income ratio: `(expenses + EMI) / income`
- `requested_emi` — EMI for the requested loan using amortization formula
- `total_emi_burden` — `(existing_emi + requested_emi) / income`
- `savings_ratio` — `(income - expenses - EMI) / income`
- `loan_to_income_ratio` — `loan_amount / (income × 12)`

### Top Feature Importances (Random Forest)

| Feature | Importance |
|---|---|
| Credit Score | 35.4% |
| Savings Ratio | 9.2% |
| DTI Ratio | 8.3% |
| Total EMI Burden | 7.9% |
| Employment Years | 4.2% |
| Bankruptcies | 4.1% |
| Missed Payments | 3.7% |

### Synthetic Data Generation

The training dataset is generated by `training/generate_data.py` with realistic distributions:
- **Demographics**: Age (21-65), dependents (0-5), 5 employment types (salaried 45%, self_employed 20%, freelancer 15%, business_owner 12%, retired 8%)
- **Financial variables**: Lognormal income distributions per employment type, beta-distributed expense ratios, Gaussian credit scores adjusted by tenure and employment
- **Approval logic**: Multi-factor scoring system (credit ≥750: +30pts, DTI ≤0.3: +20pts, etc.) with Gaussian noise and a threshold of 45 to simulate realistic approval decisions

### Financial Health Scoring Algorithm

Custom 0-100 score computed across 5 weighted categories:
- **Savings Efficiency** (22%): savings rate + emergency fund coverage
- **Debt Management** (25%): DTI ratio + EMI/income + missed payments + bankruptcies
- **Spending Discipline** (20%): expense-to-income ratio
- **Credit Standing** (18%): linear scaling of credit score (300-850 → 0-100)
- **Financial Stability** (15%): employment tenure + age + dependents + property

Grades: A+ (≥95) through F (<40), with an actionable improvement roadmap.

### Risk Analyzer (6 Dimensions)

| Dimension | Metric | Safe Threshold |
|---|---|---|
| Debt-to-Income | DTI ratio | < 35% |
| EMI Burden | Total EMI / Income | < 40% |
| Credit Risk | Credit score | > 700 |
| Savings Adequacy | Emergency fund months | > 6 months |
| Default Probability | Composite score | < 20% |
| Overspending | Expense / Income | < 60% |

---

## Chatbot Architecture

> **Important**: The chatbot is built **entirely from scratch** — it does **NOT** use any external AI API (no OpenAI, no Google Gemini, no Claude). It is a **custom NLP engine** implemented in Python.

### How It Works

The chatbot (`ml-service/services/nlp_engine.py`) is a **rule-based NLP system** with the following components:

#### 1. Intent Classification Engine
- **18 defined intents** covering loan inquiry, credit advice, budgeting, risk explanation, EMI calculation, debt management, savings, financial health, simulation, DTI explanation, greetings, help, reports, and loan comparison
- Each intent has **keyword patterns** (5-7 per intent, ~120 total patterns)
- Classification uses a **dual-scoring algorithm**:
  - **Exact substring match**: scores based on pattern length relative to message length + 0.5 bonus
  - **Word overlap scoring**: Jaccard-like similarity between message words and pattern words
  - Best-scoring intent is selected

#### 2. Context Memory System
- Maintains **per-session conversation history** (up to 20 messages per session)
- Stores role, content, detected intent, and timestamps
- Enables context-aware responses based on conversation flow

#### 3. Financial Reasoning Engine
- When user financial data is available (from profile), responses include **personalized calculations**:
  - Real DTI ratio with status indicators (✅/⚠️/❌)
  - EMI calculation using amortization formula: `P × r × (1+r)^n / ((1+r)^n - 1)`
  - Budget allocation based on 50/30/20 rule with actual dollar amounts
  - Savings potential and emergency fund analysis
- Each response includes **smart suggestion chips** for guided conversation flow

#### 4. Response Generation
- Markdown-formatted responses with emojis, bold text, and structured lists
- Financial advice follows established frameworks (50/30/20 budgeting, avalanche/snowball debt strategies)
- Contextual suggestions based on detected intent category

### APIs Used

| Service | API | Purpose |
|---|---|---|
| **ML Service** (internal) | FastAPI REST | `/predict`, `/health-score`, `/risk-analysis`, `/chat`, `/simulate`, `/analyze-document`, `/model-info` |
| **Backend ↔ ML** | Axios HTTP | Express backend proxies requests to FastAPI ML service on port 8000 |
| **Frontend ↔ Backend** | Axios + JWT | Next.js makes authenticated API calls to Express on port 5000 |
| **Android ↔ Backend** | Retrofit 2 + OkHttp | Android app makes REST calls with Bearer token authentication |

> **No external AI APIs are called.** All intelligence is self-contained within the Python ML service.

---

## API Reference

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login and receive JWT |
| `GET` | `/api/auth/me` | Get current user profile |
| `PUT` | `/api/auth/profile` | Update user profile |

### Loan Prediction
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/loans/predict` | Run ML ensemble prediction |
| `GET` | `/api/loans/history` | Get prediction history |
| `GET` | `/api/loans/stats` | Get prediction statistics |

### Financial Analysis
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/financial/health-score` | Calculate financial health score |
| `POST` | `/api/financial/risk-analysis` | Run risk assessment |
| `POST` | `/api/financial/simulate` | Run what-if simulation |
| `GET` | `/api/financial/dashboard` | Get dashboard data |
| `GET` | `/api/financial/history` | Get analysis history |

### AI Chatbot
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/chat/message` | Send message to AI chatbot |
| `GET` | `/api/chat/history` | Get chat session list |
| `GET` | `/api/chat/session/:id` | Get session messages |
| `POST` | `/api/chat/analyze-document` | Upload and analyze financial document |

### Admin & Reports
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/reports/list` | Get user reports |
| `POST` | `/api/reports/generate` | Generate a financial report |
| `GET` | `/api/admin/users` | List all users (admin) |
| `GET` | `/api/admin/stats` | Platform statistics (admin) |

### ML Service (Internal)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Health check |
| `POST` | `/predict` | Raw ML prediction |
| `POST` | `/health-score` | Financial health calculation |
| `POST` | `/risk-analysis` | Risk assessment |
| `POST` | `/chat` | NLP chatbot |
| `POST` | `/simulate` | Financial simulation |
| `POST` | `/analyze-document` | Document analysis |
| `GET` | `/model-info` | Model performance metadata |

---

## Project Structure

```
SmartLoan-AI/
├── frontend/                    # Next.js 14 Web Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── (auth)/          # Auth pages (login, register)
│   │   │   ├── (app)/           # Protected pages
│   │   │   │   ├── dashboard/   # AI Dashboard
│   │   │   │   ├── loan-prediction/ # ML Prediction Wizard
│   │   │   │   ├── analysis/    # Financial Health & Risk
│   │   │   │   ├── chatbot/     # AI Financial Advisor
│   │   │   │   ├── simulator/   # What-if Simulator
│   │   │   │   ├── reports/     # Financial Reports
│   │   │   │   ├── admin/       # Admin Panel
│   │   │   │   └── settings/    # User Settings
│   │   │   ├── layout.tsx       # Root layout
│   │   │   └── page.tsx         # Landing page
│   │   ├── components/
│   │   │   ├── ui/              # ShadCN UI primitives
│   │   │   └── layout/          # Header, Sidebar
│   │   ├── contexts/            # AuthContext (React Context)
│   │   ├── lib/                 # API client, utilities
│   │   └── styles/              # Global CSS
│   ├── tailwind.config.js
│   ├── next.config.js
│   └── package.json
│
├── backend/                     # Express.js API Gateway
│   ├── api/
│   │   └── index.js             # Vercel serverless entry
│   ├── src/
│   │   ├── server.js            # Main Express app
│   │   ├── config/
│   │   │   └── database.js      # MongoDB Atlas connection
│   │   ├── middleware/
│   │   │   └── auth.js          # JWT + Admin middleware
│   │   ├── models/              # Mongoose schemas
│   │   │   ├── User.js          # User + embedded profile
│   │   │   ├── Prediction.js    # Loan prediction results
│   │   │   ├── ChatSession.js   # Chat history
│   │   │   ├── Analysis.js      # Health/risk/simulation
│   │   │   └── Report.js        # Generated reports
│   │   └── routes/
│   │       ├── auth.js          # Register, login, profile
│   │       ├── loans.js         # Predict, history, stats
│   │       ├── financial.js     # Health, risk, simulate, dashboard
│   │       ├── chat.js          # Chatbot, documents
│   │       ├── reports.js       # Report generation
│   │       └── admin.js         # Admin endpoints
│   ├── .env                     # Environment variables
│   └── package.json
│
├── ml-service/                  # Python FastAPI ML Service
│   ├── main.py                  # FastAPI app + all endpoints
│   ├── services/                # AI/ML engine modules
│   │   ├── prediction_engine.py # Ensemble ML prediction
│   │   ├── nlp_engine.py        # Custom NLP chatbot
│   │   ├── health_scorer.py     # Financial health algorithm
│   │   ├── risk_analyzer.py     # Multi-dimensional risk
│   │   ├── simulation_engine.py # What-if projections
│   │   └── document_analyzer.py # PDF/text analysis
│   ├── training/                # Model training pipeline
│   │   ├── train_models.py      # Full training script
│   │   └── generate_data.py     # Synthetic data generator
│   ├── models/                  # Saved model artifacts
│   │   ├── logistic_regression.pkl
│   │   ├── random_forest.pkl    # ~20 MB
│   │   ├── xgboost_model.pkl    # ~1.3 MB
│   │   ├── scaler.pkl
│   │   ├── label_encoder.pkl
│   │   ├── feature_columns.pkl
│   │   └── model_metadata.json
│   ├── data/
│   │   └── loan_dataset.csv     # 12,000 samples (~1.4 MB)
│   ├── tests/
│   │   └── test_engines.py      # Pytest test suite
│   ├── Dockerfile               # Docker containerization
│   ├── render.yaml              # Render.com deployment
│   └── requirements.txt
│
├── android/                     # Android Companion App
│   ├── app/
│   │   ├── src/main/java/com/smartloan/ai/
│   │   │   ├── SmartLoanApp.java     # Application class
│   │   │   ├── data/
│   │   │   │   ├── api/              # Retrofit API service
│   │   │   │   ├── models/           # Data models (POJO)
│   │   │   │   └── repository/       # Data repository layer
│   │   │   ├── ui/
│   │   │   │   ├── auth/             # Login/Register
│   │   │   │   ├── main/             # Main activity
│   │   │   │   ├── dashboard/        # Dashboard fragment
│   │   │   │   ├── prediction/       # Loan prediction
│   │   │   │   ├── chatbot/          # AI chatbot
│   │   │   │   ├── analysis/         # Health & risk
│   │   │   │   ├── simulator/        # Simulator
│   │   │   │   ├── reports/          # Reports
│   │   │   │   ├── admin/            # Admin panel
│   │   │   │   └── settings/         # Settings
│   │   │   └── utils/
│   │   │       ├── Constants.java    # API URLs, config
│   │   │       ├── TokenManager.java # JWT token storage
│   │   │       └── ViewUtils.java    # UI utilities
│   │   ├── src/main/res/            # Layouts, drawables, values
│   │   └── build.gradle              # App-level dependencies
│   ├── build.gradle                  # Project-level config
│   └── settings.gradle
│
├── vercel.json                  # Vercel monorepo deployment
├── .gitignore
└── README.md
```

---

## Quick Start (Local Development)

### Prerequisites

- **Node.js** 18+ and npm
- **Python** 3.9+ with pip
- **Android Studio** (for mobile app, optional)
- **MongoDB Atlas** account (optional — app falls back to in-memory)

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/smartloan-ai.git
cd smartloan-ai
```

### 2. Start the ML Service (Port 8000)

```bash
cd ml-service

# Create virtual environment
python -m venv venv
venv\Scripts\activate          # Windows
# source venv/bin/activate     # Mac/Linux

# Install dependencies
pip install -r requirements.txt

# Train ML models (auto-generates synthetic data if needed)
python training/train_models.py

# Start FastAPI server
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### 3. Start the Backend (Port 5000)

```bash
cd backend
npm install

# Configure environment (create .env file)
# PORT=5000
# JWT_SECRET=your_jwt_secret
# ML_SERVICE_URL=http://localhost:8000
# MONGODB_URI=your_mongodb_atlas_uri   (optional)

npm run dev
```

### 4. Start the Frontend (Port 3000)

```bash
cd frontend
npm install
npm run dev
```

### 5. Open in Browser

Navigate to **http://localhost:3000**

### 6. Android App (Optional)

1. Open the `android/` directory in **Android Studio**
2. Update `Constants.java` with your backend URL
3. Build and run on emulator or device

---

## Deployment

### Frontend + Backend → Vercel

The project is configured as a Vercel monorepo via `vercel.json`:
- Frontend builds with `@vercel/next`
- Backend deploys as serverless function via `@vercel/node` at `/backend/api/index.js`
- API routes are rewritten: `/api/*` → serverless backend

```bash
vercel --prod
```

Required Vercel environment variables:
- `JWT_SECRET`
- `MONGODB_URI`
- `ML_SERVICE_URL` (your deployed ML service URL)

### ML Service → Render (Docker)

The ML service deploys as a Docker container on Render:

```bash
# render.yaml is pre-configured
# Push to GitHub and connect via Render dashboard
```

Or build and run Docker locally:
```bash
cd ml-service
docker build -t smartloan-ml .
docker run -p 8000:8000 smartloan-ml
```

---

## Demo Credentials

| Field | Value |
|---|---|
| **Email** | `demo@smartloan.ai` |
| **Password** | `demo123` |
| **Role** | Admin (full access) |

The demo user is automatically seeded on first startup with a pre-configured financial profile.

---

## Environment Variables

### Backend (`backend/.env`)

```env
PORT=5000
JWT_SECRET=your_secret_key
ML_SERVICE_URL=http://localhost:8000
NODE_ENV=development
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/smartloan_ai
```

### Frontend

```env
NEXT_PUBLIC_API_URL=http://localhost:5000/api
```

### ML Service

No environment variables required for local development. The ML service reads model files from the `models/` directory.

---

## Testing

### ML Service Tests (Pytest)

```bash
cd ml-service
pytest tests/test_engines.py -v
```

Tests cover:
- ✅ Health Scorer — score range 0-100, grade validation, 5 breakdown categories
- ✅ Risk Analyzer — risk level classification, ≥ 5 dimensions
- ✅ NLP Engine — intent classification accuracy (greeting, credit help)
- ✅ Simulation Engine — baseline vs. projected trajectories, chart data
- ✅ Document Analyzer — document type detection, income extraction

### Backend Health Check

```bash
curl http://localhost:5000/api/health
```

### ML Service Health Check

```bash
curl http://localhost:8000/health
```

---

## Database Schema (MongoDB)

### User
- `name`, `email`, `password` (bcrypt hashed)
- `role` (user | admin), `status` (active | inactive | suspended)
- `profile` — embedded: income, expenses, credit score, employment, savings, etc.
- `lastLogin`, timestamps

### Prediction
- `userId`, `input` (all 16 fields), `result` (ensemble + per-model), `status`

### ChatSession
- `userId`, `sessionId`, `messages[]` (role, content, intent, confidence, suggestions)
- `messageCount`, `lastActivity`

### Analysis
- `userId`, `type` (health_score | risk_analysis | simulation), `input`, `result`

### Report
- `userId`, `type`, `data`, generated timestamps

---

## Key Technical Decisions

1. **No External AI APIs**: The entire AI layer (prediction, chatbot, scoring) runs on custom code — zero dependency on OpenAI, Google, or other paid APIs. This ensures full control, zero API costs, and offline capability.

2. **Weighted Ensemble over Single Model**: Using 3 diverse algorithms with weighted voting provides more robust predictions than any single model alone.

3. **In-Memory Fallback**: The backend gracefully degrades without MongoDB — all routes work with in-memory data stores, making it easy to develop and demo without a database.

4. **Lazy-Loaded ML Engines**: The FastAPI service uses lazy initialization (`_get()` pattern) to load heavy ML models only when first requested, reducing startup time.

5. **Feature Engineering at Prediction Time**: Derived features (DTI, EMI burden, savings ratio, LTI) are computed on-the-fly from raw inputs, ensuring consistency between training and inference.

---

## License

This project is developed for educational and demonstration purposes.

---

**Built with ❤️ by Majid Wandar**
