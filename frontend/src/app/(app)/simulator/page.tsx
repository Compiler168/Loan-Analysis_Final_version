"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, Legend } from "recharts";
import { Loader2, TrendingUp, TrendingDown, Minus } from "lucide-react";
import { motion } from "framer-motion";

export default function SimulatorPage() {
  const [form, setForm] = useState({
    monthly_income: 7500, monthly_expenses: 3000, savings_balance: 25000, existing_emi: 500,
    loan_amount: 50000, loan_term_months: 36, interest_rate: 10,
    income_change_pct: 0, expense_change_pct: 0, new_loan_amount: 50000, projection_months: 24
  });
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const set = (k: string, v: number) => setForm(p => ({ ...p, [k]: v }));

  const simulate = async () => {
    setLoading(true);
    try { const r: any = await api.post('/financial/simulate', form); if (r.success) setResult(r.data); } catch {}
    setLoading(false);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Financial Simulator</h1>
        <p className="text-muted-foreground">What-if analysis & future financial projections</p>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        <Card className="glass-card">
          <CardHeader><CardTitle className="text-lg">Simulation Parameters</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-3">
              <h4 className="text-sm font-semibold text-muted-foreground">Base Financials</h4>
              {[["Monthly Income ($)", "monthly_income"], ["Monthly Expenses ($)", "monthly_expenses"], ["Savings ($)", "savings_balance"], ["Existing EMI ($)", "existing_emi"]].map(([l, k]) => (
                <div key={k} className="space-y-1"><Label className="text-xs">{l}</Label>
                  <Input type="number" value={(form as any)[k]} onChange={e => set(k, Number(e.target.value))} className="h-9" /></div>
              ))}
            </div>

            <div className="space-y-3 pt-2 border-t">
              <h4 className="text-sm font-semibold text-muted-foreground">What-If Scenarios</h4>
              <div className="space-y-1">
                <Label className="text-xs">Income Change (%)</Label>
                <div className="flex items-center gap-2">
                  <input type="range" min="-50" max="100" value={form.income_change_pct} onChange={e => set('income_change_pct', Number(e.target.value))} className="flex-1 accent-primary" />
                  <span className={`text-sm font-bold w-12 text-right ${form.income_change_pct > 0 ? 'text-emerald-500' : form.income_change_pct < 0 ? 'text-red-500' : ''}`}>{form.income_change_pct > 0 ? '+' : ''}{form.income_change_pct}%</span>
                </div>
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Expense Change (%)</Label>
                <div className="flex items-center gap-2">
                  <input type="range" min="-50" max="100" value={form.expense_change_pct} onChange={e => set('expense_change_pct', Number(e.target.value))} className="flex-1 accent-primary" />
                  <span className={`text-sm font-bold w-12 text-right ${form.expense_change_pct < 0 ? 'text-emerald-500' : form.expense_change_pct > 0 ? 'text-red-500' : ''}`}>{form.expense_change_pct > 0 ? '+' : ''}{form.expense_change_pct}%</span>
                </div>
              </div>
              <div className="space-y-1"><Label className="text-xs">New Loan Amount ($)</Label>
                <Input type="number" value={form.new_loan_amount} onChange={e => set('new_loan_amount', Number(e.target.value))} className="h-9" /></div>
              <div className="space-y-1"><Label className="text-xs">Projection (months)</Label>
                <Input type="number" min={6} max={60} value={form.projection_months} onChange={e => set('projection_months', Number(e.target.value))} className="h-9" /></div>
            </div>

            <Button onClick={simulate} disabled={loading} className="w-full mt-2">
              {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null} Run Simulation
            </Button>
          </CardContent>
        </Card>

        <div className="lg:col-span-2 space-y-4">
          {result ? (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-4">
              {/* Summary Cards */}
              <div className="grid grid-cols-3 gap-4">
                {[
                  { label: "Savings Diff", val: result.comparison.savings_difference, fmt: (v: number) => `${v >= 0 ? '+' : ''}$${Math.abs(v).toLocaleString()}`, icon: (v: number) => v >= 0 ? <TrendingUp className="h-5 w-5 text-emerald-500" /> : <TrendingDown className="h-5 w-5 text-red-500" /> },
                  { label: "Monthly Net Δ", val: result.comparison.monthly_difference, fmt: (v: number) => `${v >= 0 ? '+' : '-'}$${Math.abs(v).toLocaleString()}`, icon: (v: number) => v >= 0 ? <TrendingUp className="h-5 w-5 text-emerald-500" /> : <TrendingDown className="h-5 w-5 text-red-500" /> },
                  { label: "EMI Δ", val: result.comparison.emi_difference, fmt: (v: number) => `${v >= 0 ? '+' : '-'}$${Math.abs(v).toLocaleString()}`, icon: (v: number) => v === 0 ? <Minus className="h-5 w-5" /> : v > 0 ? <TrendingUp className="h-5 w-5 text-red-500" /> : <TrendingDown className="h-5 w-5 text-emerald-500" /> },
                ].map((s, i) => (
                  <Card key={i} className="glass-card">
                    <CardContent className="p-4 text-center">
                      <div className="flex justify-center mb-2">{(s.icon as any)(s.val)}</div>
                      <div className="text-lg font-bold">{s.fmt(s.val)}</div>
                      <div className="text-xs text-muted-foreground">{s.label}</div>
                    </CardContent>
                  </Card>
                ))}
              </div>

              {/* Chart */}
              <Card className="glass-card">
                <CardHeader><CardTitle className="text-lg">Savings Trajectory</CardTitle></CardHeader>
                <CardContent className="h-[300px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={result.chart_data}>
                      <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                      <XAxis dataKey="month" fontSize={11} tickLine={false} axisLine={false} interval={Math.floor(result.chart_data.length / 6)} />
                      <YAxis fontSize={11} tickLine={false} axisLine={false} tickFormatter={v => `$${(v/1000).toFixed(0)}k`} />
                      <RechartsTooltip contentStyle={{ backgroundColor: 'hsl(var(--card))', borderRadius: '8px' }} />
                      <Legend />
                      <Line type="monotone" dataKey="baseline" stroke="#94A3B8" strokeWidth={2} dot={false} name="Baseline" />
                      <Line type="monotone" dataKey="projected" stroke="#2563EB" strokeWidth={3} dot={false} name="Projected" />
                    </LineChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              {/* Recommendations */}
              <Card className="glass-card">
                <CardHeader><CardTitle className="text-lg">AI Recommendations</CardTitle></CardHeader>
                <CardContent className="space-y-2">
                  {result.recommendations.map((r: any, i: number) => (
                    <div key={i} className={`p-3 rounded-lg border text-sm ${r.type === 'positive' ? 'bg-emerald-500/5 border-emerald-500/20' : r.type === 'warning' ? 'bg-amber-500/5 border-amber-500/20' : r.type === 'danger' ? 'bg-red-500/5 border-red-500/20' : 'bg-blue-500/5 border-blue-500/20'}`}>
                      {r.message}
                    </div>
                  ))}
                </CardContent>
              </Card>
            </motion.div>
          ) : (
            <div className="flex items-center justify-center h-[400px] border-2 border-dashed rounded-xl">
              <p className="text-muted-foreground">Adjust parameters and run simulation</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
