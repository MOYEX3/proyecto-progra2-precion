package com.example.precional.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.precional.R;
import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.BloodPressureRecord;
import com.example.precional.ui.adapter.HistoryAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private ChipGroup filterChipGroup;
    private Chip chipDay, chipWeek, chipMonth;
    private MaterialButton btnExport;
    private HistoryAdapter adapter;
    private AppDatabase database;
    private List<BloodPressureRecord> allRecords;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = AppDatabase.getDatabase(requireContext());
        initViews(view);
        setupRecyclerView();
        setupFilterChips();
        loadRecords();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_history);
        emptyState = view.findViewById(R.id.empty_state);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        chipDay = view.findViewById(R.id.chip_day);
        chipWeek = view.findViewById(R.id.chip_week);
        chipMonth = view.findViewById(R.id.chip_month);
        btnExport = view.findViewById(R.id.btn_export);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int selectedId = checkedIds.get(0);
                applyFilter(selectedId);
            }
        });

        btnExport.setOnClickListener(v -> exportHistory());
    }

    private void loadRecords() {
        new Thread(() -> {
            allRecords = database.bloodPressureDao().getAllRecords();

            requireActivity().runOnUiThread(() -> {
                if (allRecords.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    applyFilter(chipDay.getId()); // Aplicar filtro por defecto
                }
            });
        }).start();
    }

    private void applyFilter(int selectedChipId) {
        if (allRecords == null || allRecords.isEmpty()) {
            return;
        }

        List<BloodPressureRecord> filteredRecords = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        long filterTime;

        if (selectedChipId == chipDay.getId()) {
            // Últimas 24 horas
            filterTime = currentTime - (24 * 60 * 60 * 1000);
        } else if (selectedChipId == chipWeek.getId()) {
            // Última semana
            filterTime = currentTime - (7 * 24 * 60 * 60 * 1000);
        } else {
            // Último mes
            filterTime = currentTime - (30L * 24 * 60 * 60 * 1000);
        }

        for (BloodPressureRecord record : allRecords) {
            if (record.getTimestamp() >= filterTime) {
                filteredRecords.add(record);
            }
        }

        adapter.updateRecords(filteredRecords);

        if (filteredRecords.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void exportHistory() {
        // TODO: Implementar exportación de historial
        // Por ahora solo mostramos un mensaje
        android.widget.Toast.makeText(getContext(),
            "Función de exportación en desarrollo",
            android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos cuando el fragment se vuelve visible
        loadRecords();
    }
}
