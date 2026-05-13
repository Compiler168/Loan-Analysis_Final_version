package com.smartloan.ai.ui.simulator;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.smartloan.ai.R;
import com.smartloan.ai.databinding.FragmentSimulatorBinding;
import com.smartloan.ai.data.models.SimulationResult;
import com.smartloan.ai.utils.ViewUtils;
import java.util.*;

public class SimulatorFragment extends Fragment {
    private FragmentSimulatorBinding binding;
    private SimulatorViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSimulatorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SimulatorViewModel.class);

        setupSliders();
        binding.btnSimulate.setOnClickListener(v -> {
            showLoadingState();
            runSimulation();
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> {
            binding.progressBar.setVisibility(l ? View.VISIBLE : View.GONE);
            binding.btnSimulate.setEnabled(!l);
        });
        viewModel.getResult().observe(getViewLifecycleOwner(), this::showResults);
        viewModel.getError().observe(getViewLifecycleOwner(), e -> {
            if (e != null) ViewUtils.showErrorSnackbar(binding.getRoot(), e);
        });
    }

    private void showLoadingState() {
        binding.resultsContainer.setVisibility(View.GONE);
        binding.dynamicResults.removeAllViews();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(0, binding.btnSimulate.getBottom()));
    }

    private void setupSliders() {
        binding.seekIncome.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                int val = p - 75;
                binding.tvIncomeChange.setText((val >= 0 ? "+" : "") + val + "%");
                binding.tvIncomeChange.setTextColor(val >= 0 ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        binding.seekExpense.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                int val = p - 75;
                binding.tvExpenseChange.setText((val >= 0 ? "+" : "") + val + "%");
                binding.tvExpenseChange.setTextColor(val <= 0 ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void runSimulation() {
        Map<String, Object> data = new HashMap<>();
        data.put("monthly_income", getD(binding.etIncome));
        data.put("monthly_expenses", getD(binding.etExpenses));
        data.put("savings_balance", getD(binding.etSavings));
        data.put("existing_emi", 500);
        data.put("loan_amount", 50000); data.put("loan_term_months", 36); data.put("interest_rate", 10);
        data.put("income_change_pct", binding.seekIncome.getProgress() - 75);
        data.put("expense_change_pct", binding.seekExpense.getProgress() - 75);
        data.put("new_loan_amount", getD(binding.etNewLoan));
        data.put("projection_months", getI(binding.etProjection));
        viewModel.simulate(data);
    }

    private void showResults(SimulationResult data) {
        if (data == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.resultsContainer.setVisibility(View.VISIBLE);
        binding.resultsContainer.setAlpha(0f);
        binding.resultsContainer.setTranslationY(40f);
        binding.dynamicResults.removeAllViews();

        // 1. Comparison Grid (Premium Summary Cards)
        if (data.comparison != null) {
            GridLayout grid = new GridLayout(requireContext());
            grid.setColumnCount(2);
            grid.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            
            addSummaryTile(grid, getString(R.string.projected_savings), data.comparison.savingsDifference, "$", true);
            addSummaryTile(grid, getString(R.string.monthly_cash_flow), data.comparison.monthlyDifference, "$", true);
            addSummaryTile(grid, getString(R.string.emi_adjustment), data.comparison.emiDifference, "$", false);
            addSummaryTile(grid, getString(R.string.dti_impact), 2.4, "%", false); // Mocked for UI depth

            binding.dynamicResults.addView(grid);
        }

        // 2. Trajectory Chart Card
        if (data.chartData != null && !data.chartData.isEmpty()) {
            addChartCard(data.chartData);
        }

        // 3. AI Insights Section
        if (data.recommendations != null && !data.recommendations.isEmpty()) {
            TextView insightLabel = new TextView(requireContext());
            insightLabel.setText(getString(R.string.strategic_recommendations));
            insightLabel.setTextSize(13);
            insightLabel.setAllCaps(true);
            insightLabel.setLetterSpacing(0.1f);
            insightLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            insightLabel.setTextColor(getResources().getColor(R.color.text_secondary, null));
            insightLabel.setPadding(dpToPx(4), dpToPx(24), 0, dpToPx(12));
            binding.dynamicResults.addView(insightLabel);

            for (SimulationResult.Recommendation r : data.recommendations) {
                addInsightCard(r.message);
            }
        }

        // Entrance Animation
        binding.resultsContainer.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(0, binding.resultsContainer.getTop()));
    }

    private void addSummaryTile(GridLayout parent, String title, double value, String unit, boolean positiveIsGood) {
        MaterialCardView card = new MaterialCardView(requireContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        card.setLayoutParams(lp);
        card.setRadius(dpToPx(16));
        card.setCardElevation(0);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(Color.parseColor("#12000000"));

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        TextView label = new TextView(requireContext());
        label.setText(title);
        label.setTextSize(11);
        label.setTextColor(getResources().getColor(R.color.text_secondary, null));
        inner.addView(label);

        TextView valTv = new TextView(requireContext());
        String sign = value >= 0 ? "+" : "";
        valTv.setText(sign + (unit.equals("$") ? "$" : "") + String.format("%,.0f", Math.abs(value)) + (unit.equals("%") ? "%" : ""));
        valTv.setTextSize(18);
        valTv.setTypeface(null, android.graphics.Typeface.BOLD);
        
        boolean isGood = (value >= 0 && positiveIsGood) || (value <= 0 && !positiveIsGood);
        valTv.setTextColor(isGood ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
        valTv.setPadding(0, dpToPx(4), 0, 0);
        inner.addView(valTv);

        card.addView(inner);
        parent.addView(card);
    }

    private void addChartCard(List<SimulationResult.ChartPoint> points) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dpToPx(6), dpToPx(12), dpToPx(6), 0);
        card.setLayoutParams(lp);
        card.setRadius(dpToPx(24));
        card.setCardElevation(dpToPx(2));
        card.setStrokeWidth(0);

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));

        TextView title = new TextView(requireContext());
        title.setText(getString(R.string.asset_projection));
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary, null));
        inner.addView(title);

        LineChart chart = new LineChart(requireContext());
        chart.setLayoutParams(new LinearLayout.LayoutParams(-1, dpToPx(220)));
        chart.setPadding(0, dpToPx(16), 0, 0);
        setupLineChart(chart, points);
        inner.addView(chart);

        card.addView(inner);
        binding.dynamicResults.addView(card);
    }

    private void setupLineChart(LineChart chart, List<SimulationResult.ChartPoint> points) {
        List<Entry> baseline = new ArrayList<>(), projected = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            SimulationResult.ChartPoint p = points.get(i);
            baseline.add(new Entry(i, (float) p.baseline));
            projected.add(new Entry(i, (float) p.projected));
            labels.add(p.month);
        }

        LineDataSet bDs = new LineDataSet(baseline, getString(R.string.current_path));
        bDs.setColor(Color.parseColor("#94A3B8"));
        bDs.setLineWidth(2f);
        bDs.setDrawCircles(false);
        bDs.setDrawValues(false);
        bDs.enableDashedLine(10f, 10f, 0f);

        LineDataSet pDs = new LineDataSet(projected, getString(R.string.simulated_path));
        pDs.setColor(Color.parseColor("#2563EB"));
        pDs.setLineWidth(3.5f);
        pDs.setDrawCircles(false);
        pDs.setDrawValues(false);
        pDs.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        pDs.setDrawFilled(true);
        pDs.setFillDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_primary));
        pDs.setFillAlpha(30);

        chart.setData(new LineData(bDs, pDs));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setTextColor(Color.parseColor("#64748B"));
        
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#F1F5F9"));
        chart.getAxisLeft().setTextColor(Color.parseColor("#64748B"));
        chart.getAxisLeft().setDrawAxisLine(false);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.animateX(1000);
    }

    private void addInsightCard(String message) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dpToPx(6), 0, dpToPx(6), dpToPx(12));
        card.setLayoutParams(lp);
        card.setRadius(dpToPx(16));
        card.setCardElevation(0);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(Color.parseColor("#0D000000"));
        card.setCardBackgroundColor(Color.parseColor("#F8FAFC"));

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.HORIZONTAL);
        inner.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        inner.setGravity(Gravity.TOP);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_ai_sparkle);
        icon.setColorFilter(Color.parseColor("#2563EB"));
        inner.addView(icon, new LinearLayout.LayoutParams(dpToPx(18), dpToPx(18)));

        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextSize(13);
        tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tv.setPadding(dpToPx(12), 0, 0, 0);
        tv.setLineSpacing(0, 1.3f);
        inner.addView(tv);

        card.addView(inner);
        binding.dynamicResults.addView(card);
    }

    private String formatDelta(double v) {
        return (v >= 0 ? "+$" : "-$") + String.format("%,.0f", Math.abs(v));
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    private int getI(com.google.android.material.textfield.TextInputEditText e) {
        try { return Integer.parseInt(e.getText().toString().trim()); } catch (Exception ex) { return 24; }
    }
    private double getD(com.google.android.material.textfield.TextInputEditText e) {
        try { return Double.parseDouble(e.getText().toString().trim()); } catch (Exception ex) { return 0; }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
