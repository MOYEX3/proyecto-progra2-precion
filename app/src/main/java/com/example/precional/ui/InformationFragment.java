package com.example.precional.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.precional.R;
import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.UserSettings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class InformationFragment extends Fragment {

    private TextInputEditText etUserName, etUserAge, etBloodType, etUserWeight,
                              etUserHeight, etAllergies, etMedicalNotes,
                              etEmergencyName, etEmergencyPhone, etEmergencyEmail;
    private AutoCompleteTextView spinnerUserGender;
    private MaterialButton btnSaveInformation, btnCallEmergency, btnEmailEmergency;
    private AppDatabase database;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = AppDatabase.getDatabase(requireContext());
        initViews(view);
        setupGenderSpinner();
        loadCurrentInformation();
        setupButtons();
    }

    private void initViews(View view) {
        // User information
        etUserName = view.findViewById(R.id.et_user_name);
        etUserAge = view.findViewById(R.id.et_user_age);
        spinnerUserGender = view.findViewById(R.id.spinner_user_gender);
        etBloodType = view.findViewById(R.id.et_blood_type);
        etUserWeight = view.findViewById(R.id.et_user_weight);
        etUserHeight = view.findViewById(R.id.et_user_height);
        etAllergies = view.findViewById(R.id.et_allergies);
        etMedicalNotes = view.findViewById(R.id.et_medical_notes);

        // Emergency contact information
        etEmergencyName = view.findViewById(R.id.et_emergency_name);
        etEmergencyPhone = view.findViewById(R.id.et_emergency_phone);
        etEmergencyEmail = view.findViewById(R.id.et_emergency_email);

        // Buttons
        btnSaveInformation = view.findViewById(R.id.btn_save_information);
        btnCallEmergency = view.findViewById(R.id.btn_call_emergency);
        btnEmailEmergency = view.findViewById(R.id.btn_email_emergency);
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
        spinnerUserGender.setOnItemClickListener((parent, view, position, id) ->
            spinnerUserGender.setText(genderOptions[position], false)
        );

        // Establecer valor por defecto
        spinnerUserGender.setText(getString(R.string.other), false);
    }

    private void loadCurrentInformation() {
        new Thread(() -> {
            UserSettings settings = database.userSettingsDao().getUserSettings();

            requireActivity().runOnUiThread(() -> {
                if (settings != null) {
                    // Información del usuario
                    etUserName.setText(settings.getUserName());
                    etUserAge.setText(String.valueOf(settings.getUserAge()));

                    // Nuevos campos
                    if (settings.getBloodType() != null) {
                        etBloodType.setText(settings.getBloodType());
                    }

                    etUserWeight.setText(String.valueOf(settings.getUserWeight()));
                    etUserHeight.setText(String.valueOf(settings.getUserHeight()));

                    if (settings.getAllergies() != null) {
                        etAllergies.setText(settings.getAllergies());
                    }

                    if (settings.getMedicalNotes() != null) {
                        etMedicalNotes.setText(settings.getMedicalNotes());
                    }

                    // Contacto de emergencia
                    if (settings.getEmergencyContactName() != null) {
                        etEmergencyName.setText(settings.getEmergencyContactName());
                    }

                    if (settings.getEmergencyContactPhone() != null) {
                        etEmergencyPhone.setText(settings.getEmergencyContactPhone());
                    }

                    if (settings.getEmergencyContactEmail() != null) {
                        etEmergencyEmail.setText(settings.getEmergencyContactEmail());
                    }

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
                }
            });
        }).start();
    }

    private void setupButtons() {
        btnSaveInformation.setOnClickListener(v -> saveInformation());

        btnCallEmergency.setOnClickListener(v -> {
            String phone = etEmergencyPhone.getText() != null ? etEmergencyPhone.getText().toString().trim() : "";
            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No hay número de contacto de emergencia configurado",
                             Toast.LENGTH_SHORT).show();
            }
        });

        btnEmailEmergency.setOnClickListener(v -> {
            String email = etEmergencyEmail.getText() != null ? etEmergencyEmail.getText().toString().trim() : "";
            if (!email.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Informe de presión arterial");
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No hay correo de contacto de emergencia configurado",
                             Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveInformation() {
        String name = etUserName.getText() != null ? etUserName.getText().toString().trim() : "";
        String ageStr = etUserAge.getText() != null ? etUserAge.getText().toString().trim() : "";
        String bloodType = etBloodType.getText() != null ? etBloodType.getText().toString().trim() : "";
        String weightStr = etUserWeight.getText() != null ? etUserWeight.getText().toString().trim() : "";
        String heightStr = etUserHeight.getText() != null ? etUserHeight.getText().toString().trim() : "";
        String allergies = etAllergies.getText() != null ? etAllergies.getText().toString().trim() : "";
        String medicalNotes = etMedicalNotes.getText() != null ? etMedicalNotes.getText().toString().trim() : "";
        String emergencyName = etEmergencyName.getText() != null ? etEmergencyName.getText().toString().trim() : "";
        String emergencyPhone = etEmergencyPhone.getText() != null ? etEmergencyPhone.getText().toString().trim() : "";
        String emergencyEmail = etEmergencyEmail.getText() != null ? etEmergencyEmail.getText().toString().trim() : "";
        String genderText = spinnerUserGender.getText() != null ? spinnerUserGender.getText().toString().trim() : "";

        if (name.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor completa los campos obligatorios de nombre y edad",
                         Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float weight = weightStr.isEmpty() ? 0 : Float.parseFloat(weightStr);
            int height = heightStr.isEmpty() ? 0 : Integer.parseInt(heightStr);
            String gender = convertGenderToInternal(genderText);

            new Thread(() -> {
                UserSettings settings = database.userSettingsDao().getUserSettings();
                if (settings == null) {
                    settings = new UserSettings();
                }

                // Actualizar información personal
                settings.setUserName(name);
                settings.setUserAge(age);
                settings.setUserGender(gender);
                settings.setBloodType(bloodType);
                settings.setUserWeight(weight);
                settings.setUserHeight(height);
                settings.setAllergies(allergies);
                settings.setMedicalNotes(medicalNotes);

                // Actualizar contacto de emergencia
                settings.setEmergencyContactName(emergencyName);
                settings.setEmergencyContactPhone(emergencyPhone);
                settings.setEmergencyContactEmail(emergencyEmail);

                database.userSettingsDao().insertOrUpdate(settings);

                requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Información guardada exitosamente",
                                 Toast.LENGTH_SHORT).show()
                );
            }).start();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Por favor ingresa valores numéricos válidos",
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
