"""
SmartLoan AI+ — Document Analysis Engine
PDF text extraction, financial pattern detection, and summary generation.
"""
import re
from typing import Dict, Any, List


class DocumentAnalyzer:
    """Analyze uploaded financial documents for insights."""

    INCOME_PATTERNS = [
        r'(?:salary|income|earnings|gross pay|net pay|take.?home)[:\s]*\$?([\d,]+\.?\d*)',
        r'\$?([\d,]+\.?\d*)\s*(?:per month|monthly|/mo)',
    ]
    EXPENSE_PATTERNS = [
        r'(?:expense|deduction|payment|debit)[:\s]*\$?([\d,]+\.?\d*)',
        r'(?:rent|mortgage|utilities|insurance)[:\s]*\$?([\d,]+\.?\d*)',
    ]
    ACCOUNT_PATTERNS = [
        r'(?:account|acct)[:\s#]*(\d{4,})',
        r'(?:balance)[:\s]*\$?([\d,]+\.?\d*)',
    ]

    def _extract_amounts(self, text: str, patterns: list) -> List[float]:
        amounts = []
        for pat in patterns:
            for match in re.finditer(pat, text, re.IGNORECASE):
                try:
                    val = float(match.group(1).replace(',', ''))
                    if val > 0:
                        amounts.append(val)
                except (ValueError, IndexError):
                    pass
        return amounts

    def _classify_document(self, text: str) -> str:
        lower = text.lower()
        if any(w in lower for w in ['salary slip', 'pay stub', 'payslip', 'earnings statement']):
            return 'salary_slip'
        if any(w in lower for w in ['bank statement', 'account statement', 'transaction']):
            return 'bank_statement'
        if any(w in lower for w in ['tax return', 'form 1040', 'w-2', 'tax assessment']):
            return 'tax_document'
        if any(w in lower for w in ['loan agreement', 'mortgage', 'promissory note']):
            return 'loan_document'
        return 'financial_document'

    def _detect_risks(self, text: str, incomes: list, expenses: list) -> List[Dict]:
        risks = []
        lower = text.lower()
        if any(w in lower for w in ['overdue', 'past due', 'late payment', 'penalty']):
            risks.append({'type': 'payment_issue', 'severity': 'high', 'message': 'Late/overdue payments detected.'})
        if any(w in lower for w in ['overdraft', 'insufficient funds', 'nsf']):
            risks.append({'type': 'overdraft', 'severity': 'medium', 'message': 'Overdraft activity detected.'})
        if any(w in lower for w in ['collection', 'charged off', 'default']):
            risks.append({'type': 'collections', 'severity': 'high', 'message': 'Collection or default indicators found.'})
        if incomes and expenses:
            avg_inc = sum(incomes) / len(incomes)
            avg_exp = sum(expenses) / len(expenses)
            if avg_exp > avg_inc * 0.8:
                risks.append({'type': 'high_expenses', 'severity': 'medium', 'message': 'Expenses appear high relative to income.'})
        return risks

    def analyze(self, text: str) -> Dict[str, Any]:
        doc_type = self._classify_document(text)
        incomes = self._extract_amounts(text, self.INCOME_PATTERNS)
        expenses = self._extract_amounts(text, self.EXPENSE_PATTERNS)
        balances = self._extract_amounts(text, self.ACCOUNT_PATTERNS)
        risks = self._detect_risks(text, incomes, expenses)

        recs = []
        if incomes:
            avg = sum(incomes) / len(incomes)
            recs.append(f"Detected income: avg ${avg:,.0f}. Consider loans within 3-5x annual income.")
        if expenses:
            recs.append(f"Detected {len(expenses)} expense entries. Review for optimization.")
        if risks:
            recs.append(f"Found {len(risks)} risk indicator(s). Address before applying.")
        if not recs:
            recs.append("Document parsed. Upload salary slips or bank statements for deeper analysis.")

        lines = [l.strip() for l in text.split('\n') if l.strip()]
        preview = '\n'.join(lines[:10]) + ('...' if len(lines) > 10 else '')

        return {
            'document_type': doc_type,
            'extracted': {'incomes': incomes[:10], 'expenses': expenses[:10], 'balances': balances[:5]},
            'risks': risks,
            'recommendations': recs,
            'summary': f"Analyzed {doc_type.replace('_', ' ').title()}: {len(incomes)} income entries, "
                       f"{len(expenses)} expense entries, {len(risks)} risk indicators.",
            'preview': preview,
            'word_count': len(text.split()),
            'line_count': len(lines)
        }
