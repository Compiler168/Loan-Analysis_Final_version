"""
SmartLoan AI+ — FastAPI ML Service
Main application entry point for all AI/ML endpoints.
"""
from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any

app = FastAPI(
    title="SmartLoan AI+ ML Service",
    description="AI/ML microservice for loan prediction, financial analysis, and intelligent advisory",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Mobile app URLs configured via deployment
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Lazy-loaded services
_engines = {}

def _get(name):
    if name not in _engines:
        if name == 'prediction':
            from services.prediction_engine import PredictionEngine
            _engines[name] = PredictionEngine()
        elif name == 'health':
            from services.health_scorer import HealthScorer
            _engines[name] = HealthScorer()
        elif name == 'risk':
            from services.risk_analyzer import RiskAnalyzer
            _engines[name] = RiskAnalyzer()
        elif name == 'nlp':
            from services.nlp_engine import NLPEngine
            _engines[name] = NLPEngine()
        elif name == 'sim':
            from services.simulation_engine import SimulationEngine
            _engines[name] = SimulationEngine()
        elif name == 'doc':
            from services.document_analyzer import DocumentAnalyzer
            _engines[name] = DocumentAnalyzer()
    return _engines[name]


# ─── Request Models ───
class LoanPredictionRequest(BaseModel):
    age: int = Field(35, ge=18, le=80)
    dependents: int = Field(0, ge=0, le=10)
    employment_status: str = Field("salaried")
    employment_years: float = Field(3.0, ge=0)
    monthly_income: float = Field(5000, gt=0)
    monthly_expenses: float = Field(2500, ge=0)
    credit_score: int = Field(650, ge=300, le=850)
    existing_loans: int = Field(0, ge=0)
    existing_emi: float = Field(0, ge=0)
    loan_amount: float = Field(50000, gt=0)
    loan_term_months: int = Field(36, ge=6, le=360)
    interest_rate: float = Field(10.0, gt=0, le=30)
    property_value: float = Field(0, ge=0)
    savings_balance: float = Field(10000, ge=0)
    missed_payments_last_year: int = Field(0, ge=0, le=12)
    bankruptcies: int = Field(0, ge=0)

class HealthScoreRequest(BaseModel):
    monthly_income: float = Field(gt=0)
    monthly_expenses: float = Field(ge=0)
    savings_balance: float = Field(ge=0)
    existing_emi: float = Field(0, ge=0)
    existing_loans: int = Field(0, ge=0)
    credit_score: int = Field(650, ge=300, le=850)
    employment_years: float = Field(0, ge=0)
    missed_payments_last_year: int = Field(0, ge=0)
    bankruptcies: int = Field(0, ge=0)
    age: int = Field(30, ge=18)
    dependents: int = Field(0, ge=0)
    property_value: float = Field(0, ge=0)

class RiskAnalysisRequest(BaseModel):
    monthly_income: float = Field(gt=0)
    monthly_expenses: float = Field(ge=0)
    existing_emi: float = Field(0, ge=0)
    credit_score: int = Field(650, ge=300, le=850)
    loan_amount: float = Field(0, ge=0)
    loan_term_months: int = Field(36, ge=6)
    interest_rate: float = Field(10.0, gt=0)
    savings_balance: float = Field(0, ge=0)
    missed_payments_last_year: int = Field(0, ge=0)
    bankruptcies: int = Field(0, ge=0)
    existing_loans: int = Field(0, ge=0)

class ChatRequest(BaseModel):
    message: str = Field(min_length=1)
    session_id: str = Field("default")
    user_data: Optional[Dict[str, Any]] = None

class SimulationRequest(BaseModel):
    monthly_income: float = Field(gt=0)
    monthly_expenses: float = Field(ge=0)
    savings_balance: float = Field(0, ge=0)
    existing_emi: float = Field(0, ge=0)
    loan_amount: float = Field(0, ge=0)
    loan_term_months: int = Field(36, ge=6)
    interest_rate: float = Field(10.0, gt=0)
    income_change_pct: float = Field(0)
    expense_change_pct: float = Field(0)
    new_loan_amount: Optional[float] = None
    projection_months: int = Field(24, ge=6, le=60)


# ─── Endpoints ───
@app.get("/")
async def root():
    return {"service": "SmartLoan AI+ ML Service", "status": "running", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

@app.post("/predict")
async def predict_loan(req: LoanPredictionRequest):
    try:
        return {"success": True, "data": _get('prediction').predict(req.model_dump())}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/health-score")
async def calc_health(req: HealthScoreRequest):
    try:
        return {"success": True, "data": _get('health').calculate_score(req.model_dump())}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/risk-analysis")
async def analyze_risk(req: RiskAnalysisRequest):
    try:
        return {"success": True, "data": _get('risk').analyze(req.model_dump())}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/chat")
async def chat(req: ChatRequest):
    try:
        return {"success": True, "data": _get('nlp').chat(req.message, req.session_id, req.user_data)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/simulate")
async def simulate(req: SimulationRequest):
    try:
        d = req.model_dump()
        if d.get('new_loan_amount') is None:
            d['new_loan_amount'] = d['loan_amount']
        return {"success": True, "data": _get('sim').simulate(d)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/analyze-document")
async def analyze_doc(file: UploadFile = File(...)):
    try:
        content = await file.read()
        text = ""
        if file.filename and file.filename.lower().endswith('.pdf'):
            try:
                import pdfplumber, io
                with pdfplumber.open(io.BytesIO(content)) as pdf:
                    for page in pdf.pages:
                        t = page.extract_text()
                        if t:
                            text += t + "\n"
            except Exception:
                text = content.decode('utf-8', errors='ignore')
        else:
            text = content.decode('utf-8', errors='ignore')
        if not text.strip():
            raise HTTPException(status_code=400, detail="Could not extract text")
        return {"success": True, "data": _get('doc').analyze(text)}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/model-info")
async def model_info():
    try:
        return {"success": True, "data": _get('prediction').get_model_info()}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
