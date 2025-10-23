package com.example.precional.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.precional.R;
import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.UserSettings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsFragment extends Fragment {

    private TextInputEditText etUserName, etUserAge, etEmergencyName, etEmergencyPhone, etEmergencyEmail, etN8nWebhookUrl;
    private AutoCompleteTextView spinnerUserGender;
    private Switch switchMorningReminder, switchEveningReminder, switchHypertension,
                   switchDiabetes, switchDarkMode, switchAiEnabled;
    private MaterialButton btnSaveSettings;
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
        setupGenderSpinner();
        loadCurrentSettings();
        setupSaveButton();
    }

    private void initViews(View view) {
        etUserName = view.findViewById(R.id.et_user_name);
        etUserAge = view.findViewById(R.id.et_user_age);
        etEmergencyName = view.findViewById(R.id.et_emergency_name);
        etEmergencyPhone = view.findViewById(R.id.et_emergency_phone);
        etEmergencyEmail = view.findViewById(R.id.et_emergency_email);
        etN8nWebhookUrl = view.findViewById(R.id.et_n8n_webhook_url);
        spinnerUserGender = view.findViewById(R.id.spinner_user_gender);
        switchMorningReminder = view.findViewById(R.id.switch_morning_reminder);
        switchEveningReminder = view.findViewById(R.id.switch_evening_reminder);
        switchHypertension = view.findViewById(R.id.switch_hypertension);
        switchDiabetes = view.findViewById(R.id.switch_diabetes);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchAiEnabled = view.findViewById(R.id.switch_ai_enabled);
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
    }

    private void setupGenderSpinner() {
        String[] genderOptions = {
            getString(R.string.male),
            getString(R.string.female),
            getString(R.string.other)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            genderOptions
        );

        spinnerUserGender.setAdapter(adapter);

        // Configurar para que funcione como un spinner
        spinnerUserGender.setInputType(0);
        spinnerUserGender.setKeyListener(null);

        // Mostrar dropdown al hacer clic
        spinnerUserGender.setOnClickListener(v -> spinnerUserGender.showDropDown());

        // Manejar selección
        spinnerUserGender.setOnItemClickListener((parent, view, position, id) -> {
            spinnerUserGender.setText(genderOptions[position], false);
        });

        // Establecer valor por defecto
        spinnerUserGender.setText(getString(R.string.other), false);
    }

    private void loadCurrentSettings() {
        new Thread(() -> {
            UserSettings settings = database.userSettingsDao().getUserSettings();

            requireActivity().runOnUiThread(() -> {
                if (settings != null) {
                    etUserName.setText(settings.getUserName());
                    etUserAge.setText(String.valueOf(settings.getUserAge()));
                    etEmergencyName.setText(settings.getEmergencyContactName());
                    etEmergencyPhone.setText(settings.getEmergencyContactPhone());
                    etEmergencyEmail.setText(settings.getEmergencyContactEmail());
                    etN8nWebhookUrl.setText(settings.getN8nWebhookUrl());

                    // Configurar género
                    String gender = settings.getUserGender();
                    switch (gender) {
                        case "male":
                            spinnerUserGender.setText(getString(R.string.male), false);
                            break;
                        case "female":
                            spinnerUserGender.setText(getString(R.string.female), false);
                            break;
                        case "other":
                            spinnerUserGender.setText(getString(R.string.other), false);
                            break;
                    }

                    // Configurar switches
                    switchMorningReminder.setChecked(settings.isMorningReminder());
                    switchEveningReminder.setChecked(settings.isEveningReminder());
                    switchHypertension.setChecked(settings.isHasHypertension());
                    switchDiabetes.setChecked(settings.isHasDiabetes());
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
        String name = etUserName.getText().toString().trim();
        String ageStr = etUserAge.getText().toString().trim();
        String emergencyName = etEmergencyName.getText().toString().trim();
        String emergencyPhone = etEmergencyPhone.getText().toString().trim();
        String emergencyEmail = etEmergencyEmail.getText().toString().trim();
        String n8nWebhookUrl = etN8nWebhookUrl.getText().toString().trim();
        String genderText = spinnerUserGender.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor completa los campos obligatorios",
                         Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            String gender = convertGenderToInternal(genderText);

            new Thread(() -> {
                UserSettings settings = database.userSettingsDao().getUserSettings();
                if (settings == null) {
                    settings = new UserSettings();
                }

                // Actualizar configuración
                settings.setUserName(name);
                settings.setUserAge(age);
                settings.setUserGender(gender);
                settings.setEmergencyContactName(emergencyName);
                settings.setEmergencyContactPhone(emergencyPhone);
                settings.setEmergencyContactEmail(emergencyEmail);
                settings.setN8nWebhookUrl(n8nWebhookUrl);
                settings.setMorningReminder(switchMorningReminder.isChecked());
                settings.setEveningReminder(switchEveningReminder.isChecked());
                settings.setHasHypertension(switchHypertension.isChecked());
                settings.setHasDiabetes(switchDiabetes.isChecked());
                settings.setDarkModeEnabled(switchDarkMode.isChecked());
                settings.setAiEnabled(switchAiEnabled.isChecked());

                database.userSettingsDao().insertOrUpdate(settings);

                requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Configuración guardada exitosamente",
                                 Toast.LENGTH_SHORT).show()
                );
            }).start();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Por favor ingresa una edad válida",
                         Toast.LENGTH_SHORT).show();
        }
    }

    private String convertGenderToInternal(String genderText) {
        if (genderText.equals(getString(R.string.male))) {
            return "male";
        } else if (genderText.equals(getString(R.string.female))) {
            return "female";
        } else {
            return "other";
        }
    }
}
