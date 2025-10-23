package com.example.precional.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.precional.R;
import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.UserSettings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SettingsFragment extends Fragment {

    // API Key de OpenRouter hardcodeada
    private static final String OPENROUTER_API_KEY = "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";

    private Switch switchMorningReminder, switchEveningReminder,
                   switchDarkMode, switchAiEnabled;
    private MaterialButton btnSaveSettings;
    private MaterialCardView cardNotifications, cardAppearance, cardAiSettings;
    private AppDatabase database;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = AppDatabase.getDatabase(requireContext());
        initViews(view);
        loadCurrentSettings();
        setupSaveButton();
    }

    private void initViews(View view) {
        // Cards
        cardNotifications = view.findViewById(R.id.card_notifications);
        cardAppearance = view.findViewById(R.id.card_appearance);
        cardAiSettings = view.findViewById(R.id.card_ai_settings);

        // Switches
        switchMorningReminder = view.findViewById(R.id.switch_morning_reminder);
        switchEveningReminder = view.findViewById(R.id.switch_evening_reminder);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchAiEnabled = view.findViewById(R.id.switch_ai_enabled);

        // Botón guardar
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
    }

    private void loadCurrentSettings() {
        new Thread(() -> {
            UserSettings settings = database.userSettingsDao().getUserSettings();

            requireActivity().runOnUiThread(() -> {
                if (settings != null) {
                    // Configurar switches
                    switchMorningReminder.setChecked(settings.isMorningReminder());
                    switchEveningReminder.setChecked(settings.isEveningReminder());
                    switchDarkMode.setChecked(settings.isDarkModeEnabled());
                    switchAiEnabled.setChecked(settings.isAiEnabled());
                }
            });
        }).start();
    }

    private void setupSaveButton() {
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        new Thread(() -> {
            UserSettings settings = database.userSettingsDao().getUserSettings();
            if (settings == null) {
                settings = new UserSettings();
            }

            // Actualizar configuración
            settings.setMorningReminder(switchMorningReminder.isChecked());
            settings.setEveningReminder(switchEveningReminder.isChecked());
            settings.setDarkModeEnabled(switchDarkMode.isChecked());
            settings.setAiEnabled(switchAiEnabled.isChecked());

            // Usar la API Key hardcodeada
            settings.setAiApiKey(OPENROUTER_API_KEY);

            database.userSettingsDao().insertOrUpdate(settings);

            requireActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), "Configuración guardada exitosamente",
                             Toast.LENGTH_SHORT).show()
            );
        }).start();
    }
}
