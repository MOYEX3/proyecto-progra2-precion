package com.example.precional.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.precional.R;
import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.BloodPressureRecord;
import com.example.precional.data.entity.UserSettings;
import com.example.precional.service.AIRecommendationService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvGreeting, tvDate, tvLastPressure, tvStatus, tvRecommendation;
    private View statusIndicator;
    private LinearLayout lastReadingContainer, emptyState;
    private ProgressBar recommendationLoading;
    private LineChart pressureChart;
    private AppDatabase database;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = AppDatabase.getDatabase(requireContext());
        initViews(view);
        loadUserData();
        loadLastReading();
        loadChart();
        loadRecommendation();
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvDate = view.findViewById(R.id.tv_date);
        tvLastPressure = view.findViewById(R.id.tv_last_pressure);
        tvStatus = view.findViewById(R.id.tv_status);
        tvRecommendation = view.findViewById(R.id.tv_recommendation);
        statusIndicator = view.findViewById(R.id.status_indicator);
        lastReadingContainer = view.findViewById(R.id.last_reading_container);
        emptyState = view.findViewById(R.id.empty_state);
        recommendationLoading = view.findViewById(R.id.recommendation_loading);
        pressureChart = view.findViewById(R.id.pressure_chart);
    }

    private void loadUserData() {
        new Thread(() -> {
            UserSettings settings = database.userSettingsDao().getUserSettings();
            if (settings != null) {
                requireActivity().runOnUiThread(() -> {
                    tvGreeting.setText(getString(R.string.hello_user, settings.getUserName()));
                });
            }

            // Mostrar fecha actual
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
            String currentDate = dateFormat.format(new Date());
            requireActivity().runOnUiThread(() -> {
                tvDate.setText(getString(R.string.today_date, currentDate));
            });
        }).start();
    }

    private void loadLastReading() {
        new Thread(() -> {
            BloodPressureRecord lastRecord = database.bloodPressureDao().getLatestRecord();

            requireActivity().runOnUiThread(() -> {
                if (lastRecord != null) {
                    lastReadingContainer.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);

                    // Mostrar valores
                    String pressure = lastRecord.getSystolic() + "/" + lastRecord.getDiastolic();
                    tvLastPressure.setText(pressure);

                    // Mostrar estado
                    String status = lastRecord.getPressureStatus();
                    int statusColor = getStatusColor(status);
                    String statusText = getStatusText(status);

                    tvStatus.setText(statusText);
                    tvStatus.setTextColor(statusColor);
                    statusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));

                } else {
                    lastReadingContainer.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void loadChart() {
        new Thread(() -> {
            List<BloodPressureRecord> records = database.bloodPressureDao().getLast7Records();

            requireActivity().runOnUiThread(() -> {
                setupChart(records);
            });
        }).start();
    }

    private void setupChart(List<BloodPressureRecord> records) {
        if (records.isEmpty()) {
            pressureChart.setNoDataText("No hay datos suficientes para mostrar el gráfico");
            return;
        }

        ArrayList<Entry> systolicEntries = new ArrayList<>();
        ArrayList<Entry> diastolicEntries = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            BloodPressureRecord record = records.get(i);
            systolicEntries.add(new Entry(i, record.getSystolic()));
            diastolicEntries.add(new Entry(i, record.getDiastolic()));
        }

        LineDataSet systolicDataSet = new LineDataSet(systolicEntries, "Sistólica");
        systolicDataSet.setColor(Color.parseColor("#E74C3C"));
        systolicDataSet.setLineWidth(2f);
        systolicDataSet.setCircleColor(Color.parseColor("#E74C3C"));

        LineDataSet diastolicDataSet = new LineDataSet(diastolicEntries, "Diastólica");
        diastolicDataSet.setColor(Color.parseColor("#4A90E2"));
        diastolicDataSet.setLineWidth(2f);
        diastolicDataSet.setCircleColor(Color.parseColor("#4A90E2"));

        LineData lineData = new LineData(systolicDataSet, diastolicDataSet);
        pressureChart.setData(lineData);

        // Configurar el gráfico
        pressureChart.getDescription().setEnabled(false);
        pressureChart.setTouchEnabled(true);
        pressureChart.setDragEnabled(true);
        pressureChart.setScaleEnabled(true);
        pressureChart.setPinchZoom(true);

        // Configurar ejes
        XAxis xAxis = pressureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = pressureChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        pressureChart.getAxisRight().setEnabled(false);
        pressureChart.invalidate();
    }

    private void loadRecommendation() {
        recommendationLoading.setVisibility(View.VISIBLE);

        new Thread(() -> {
            BloodPressureRecord lastRecord = database.bloodPressureDao().getLatestRecord();
            String recommendation;

            if (lastRecord != null && lastRecord.getObservations() != null &&
                    lastRecord.getObservations().contains("💡 IA:")) {
                // Extraer solo la parte de IA de las observaciones
                String observations = lastRecord.getObservations();
                int aiIndex = observations.indexOf("💡 IA:");
                if (aiIndex != -1) {
                    recommendation = observations.substring(aiIndex + 6).trim();
                } else {
                    recommendation = AIRecommendationService.getShortTipForDashboard(lastRecord);
                }
            } else {
                recommendation = AIRecommendationService.getShortTipForDashboard(lastRecord);
            }

            requireActivity().runOnUiThread(() -> {
                recommendationLoading.setVisibility(View.GONE);
                tvRecommendation.setText(recommendation);
            });
        }).start();
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "normal":
                return requireContext().getColor(R.color.normal_pressure);
            case "elevated":
                return requireContext().getColor(R.color.elevated_pressure);
            case "high":
                return requireContext().getColor(R.color.high_pressure);
            default:
                return requireContext().getColor(R.color.medium_gray);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "normal":
                return getString(R.string.normal_status);
            case "elevated":
                return getString(R.string.elevated_status);
            case "high":
                return getString(R.string.high_status);
            default:
                return "Desconocido";
        }
    }
}
