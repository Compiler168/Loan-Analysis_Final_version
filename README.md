# SmartLoan AI+

![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/Compiler168/Loan-Analysis/ci-and-deploy.yml?branch=main)
![License](https://img.shields.io/github/license/Compiler168/Loan-Analysis)
![Last Commit](https://img.shields.io/github/last-commit/Compiler168/Loan-Analysis)

## Project Introduction

**Project Name:** SmartLoan AI+

**Tagline:** Intelligent loan decision automation and personal finance advisory for modern Android users.

**Executive Summary:**
SmartLoan AI+ is a production-ready fintech platform built around an **Android Application developed in Java** that connects to a secured Express.js backend and a dedicated FastAPI ML microservice. The system delivers loan probability scoring, credit health analytics, risk monitoring, AI-driven conversational advice, and report generation.

**Project Overview:**
The platform integrates:
- Android Application developed in Java for mobile financial experiences
- Express.js backend API with Firebase Firestore persistence
- FastAPI ML service for loan prediction, scoring, simulation, and chatbot reasoning
- Cloud-ready deployment pipelines with Docker and GitHub Actions

---

## Business Documentation

### Problem Statement
Many borrowers lack transparent, real-time decision support when evaluating loan eligibility and managing finance. Traditional apps provide generic guidance without ML-enabled risk analysis or tailored scenarios.

### Existing Challenges
- Fragmented loan advice across multiple services
- Limited visibility into approval probability and financial health
- Manual risk evaluation without predictive automation
- Poor mobile-first integration for Android users

### Proposed Solution
SmartLoan AI+ centralizes loan prediction, personal finance analytics, and conversational advisory into a single mobile experience backed by enterprise-grade backend services.

### Benefits
- Faster approval insight through AI models
- Personalized recommendations based on financial health
- Reduced risk through analytics and monitoring
- Secure, scalable cloud architecture
- Clear engineer-ready project structure

### Objectives
- Provide real-time loan approval scoring
- Enable health and risk analysis on customer data
- Maintain secure user authentication and session flow
- Build a reusable ML microservice for future feature expansion
- Deliver production-level documentation and cleanup

### Target Users
- Loan applicants seeking better approval insight
- Financial advisors requiring rapid analysis
- Mobile-first consumers in emerging markets
- Product teams validating AI-enabled fintech workflows

---

## Technical Documentation

### Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Android | Java, Android Jetpack | Mobile application UI and data handling |
| Backend | Node.js, Express.js | REST API, auth, Firestore integration |
| Database | Firebase Firestore | User, prediction, analysis, chat and report persistence |
| ML Service | Python, FastAPI | Loan prediction, health scoring, risk analysis, chatbot |
| CI/CD | GitHub Actions | Build, smoke test, Docker image publish, deploy |
| Containerization | Docker | Standardized backend and ML service packaging |

### System Architecture

```
Android App (Java)
   ↕ HTTPS
Express Backend (Node.js)
   ↕ HTTP
FastAPI ML Service (Python)
   ↕ Firestore
Firebase Firestore Database
```

### Frontend Architecture
- Android Application developed in Java
- Uses event-driven ViewModel and Activity/Fragment flows
- Network access via REST API with JWT authentication
- Local UI state managed through Android Jetpack patterns

### Backend Architecture
- `backend/src/server.js` bootstraps Express and Firestore
- Route modules separate domain logic: auth, loans, financial, chat, reports
- Firestore integration through custom model classes
- Auth middleware validates JWT tokens and protects API routes
- ML interactions proxied to the dedicated ML service via `axios`

### Database Architecture
- Firebase Firestore collections store structured documents
- Collections:
  - `users`
  - `predictions`
  - `analyses`
  - `chatSessions`
  - `reports`
  - `dashboards`

### Machine Learning Architecture
- `ml/main.py` exposes clean FastAPI endpoints
- Prediction, health scoring, risk analysis, simulation, NLP, and document parsing engines
- Lazy-loaded service modules for resource efficiency
- Ensemble model stack: XGBoost, Random Forest, Logistic Regression

### Security Architecture
- JWT-based authentication for all protected API routes
- Password hashing with `bcryptjs`
- Helmet middleware for HTTP security headers
- CORS restrictions configurable via environment
- Rate limiting for general and AI-specific endpoints
- Local secret management via `.env` templates and ignored credential files

---

## Feature Documentation

### User Features

#### Registration and Login
- Purpose: Create secure user accounts and authenticate.
- Inputs: `name`, `email`, `password`
- Processing Logic: Saves user data to Firestore, hashes password, issues JWT.
- Outputs: Auth token, user profile, optional Firebase custom token.
- Benefits: Secure onboarding, session persistence.
- Implementation: `backend/src/controllers/AuthController.js`, `backend/src/routes/auth.js`

#### Profile Management
- Purpose: Update personal and financial profile data.
- Inputs: profile fields like income, expenses, credit score.
- Processing Logic: Updates Firestore user records and refreshes dashboard.
- Outputs: updated user profile and auth-safe response.
- Benefits: Better prediction accuracy and personalized analytics.
- Implementation: `AuthController.updateProfile`

#### Loan Prediction
- Purpose: Estimate loan approval probability using ensemble ML.
- Inputs: client financial and loan request data.
- Processing Logic: Proxy to ML service `/predict` endpoint and persist results.
- Outputs: probability score, approval recommendation, top risk factors.
- Benefits: Faster decision support and user transparency.
- Implementation: `LoanController.predictLoan`, `ml/services/prediction_engine.py`

#### Financial Dashboard
- Purpose: Present summary analytics and financial health metrics.
- Inputs: user ID from authenticated session.
- Processing Logic: Reads dashboard documents from Firestore, regenerates if absent.
- Outputs: dashboard metrics, approval rates, summary totals.
- Benefits: Real-time financial overview.
- Implementation: `FinancialController.getDashboard`

#### AI Chat Assistant
- Purpose: Provide conversational financial advice and loan explanations.
- Inputs: chat message, session ID, optional user data.
- Processing Logic: Intent classification and contextual response generation.
- Outputs: conversational reply and session logging.
- Benefits: Natural advice flow with fintech intelligence.
- Implementation: `ChatController.sendMessage`, `ml/services/nlp_engine.py`

### AI Features

#### Loan Prediction Engine
- Purpose: Ensemble-based loan approval scoring.
- Inputs: financial profile and requested loan terms.
- Processing Logic: feature engineering, scaling, ensemble aggregation, risk reason generation.
- Outputs: approval probability, confidence, top factors, derived metrics.
- Benefits: Explainable predictions and better loan transparency.
- Implementation: `PredictionEngine.predict`

#### Health Scoring
- Purpose: Evaluate user financial health.
- Inputs: income, expenses, savings, credit score, debts.
- Processing Logic: ML-based health scoring model.
- Outputs: health score and risk classification.
- Benefits: Quick financial wellness check.
- Implementation: `HealthScorer`

#### Risk Analysis
- Purpose: Assess loan risk relative to current profile.
- Inputs: loan exposure and credit metrics.
- Processing Logic: risk model produces risk assessment features.
- Outputs: risk rating and recommended mitigation.
- Benefits: Safer lending decisions.
- Implementation: `RiskAnalyzer`

#### Simulation Engine
- Purpose: Model future financial scenarios.
- Inputs: income/expense changes, loan variables.
- Processing Logic: calculates projected cash flow and savings impact.
- Outputs: scenario summary, projected balance, recommendations.
- Benefits: Better planning before committing to loans.
- Implementation: `SimulationEngine`

### Banking Features
- Purpose: Provide autonomous loan and credit decision support.
- Inputs: loan application and profile data.
- Processing Logic: ties backend requests to ML service inference.
- Outputs: approval recommendations and financial advice.
- Benefits: Faster bank-like loan analysis on mobile.
- Implementation: `backend/src/controllers/LoanController.js`

### Analytics Features
- Purpose: Store and retrieve prediction, analysis, and report history.
- Inputs: authenticated user actions.
- Processing Logic: Firestore persistence and history retrieval.
- Outputs: historical charts, campaign metrics, session records.
- Benefits: User retention through tracked analytics.
- Implementation: Firestore model classes under `backend/src/models`

### Security Features
- Purpose: Secure access and protect sensitive data.
- Inputs: JWT tokens, request headers.
- Processing Logic: token verification and request sanitization.
- Outputs: authorized access or rejection.
- Benefits: secure production-readiness.
- Implementation: `backend/src/middleware/auth.js`, `helmet`, `rate-limit`

### Administrative Features
-
