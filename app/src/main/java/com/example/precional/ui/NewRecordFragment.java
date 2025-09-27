package com.example.precional.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.precional.api.AIRequest;
import com.example.precional.api.AIResponse;
import com.example.precional.api.ApiClient;
import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.BloodPressureRecord;
import com.example.precional.data.entity.UserSettings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewRecordFragment extends Fragment {

    private TextInputEditText etName, etAge, etSystolic, etDiastolic, etDateTime, etObservations;
    private AutoCompleteTextView spinnerGender;
    private TextInputLayout observationsLayout;
    private MaterialButton btnSaveRecord; // Eliminé btnGetAISuggestions ya que no existe
    private AppDatabase database;
    private Calendar selectedDateTime; // Esta línea estaba faltando

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = AppDatabase.getDatabase(requireContext());
        initViews(view);
        setupGenderSpinner();
        setupDateTimePicker();
        loadUserDefaults();
        setupSaveButton();
        // Eliminé setupAISuggestionsButton() ya que no existe el botón
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_name);
        etAge = view.findViewById(R.id.et_age);
        etSystolic = view.findViewById(R.id.et_systolic);
        etDiastolic = view.findViewById(R.id.et_diastolic);
        etDateTime = view.findViewById(R.id.et_date_time);
        etObservations = view.findViewById(R.id.et_observations);
        spinnerGender = view.findViewById(R.id.spinner_gender);
        btnSaveRecord = view.findViewById(R.id.btn_save_record);
        observationsLayout = view.findViewById(R.id.observations_layout);

        selectedDateTime = Calendar.getInstance();
        updateDateTimeDisplay();
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

        spinnerGender.setAdapter(adapter);

        // Configurar para que funcione como un spinner
        spinnerGender.setInputType(0); // Deshabilitar entrada de teclado
        spinnerGender.setKeyListener(null); // Prevenir edición manual

        // Mostrar dropdown al hacer clic
        spinnerGender.setOnClickListener(v -> {
            spinnerGender.showDropDown();
        });

        // Manejar selección
        spinnerGender.setOnItemClickListener((parent, view, position, id) -> {
            spinnerGender.setText(genderOptions[position], false);
        });

        // Establecer valor por defecto
        spinnerGender.setText(getString(R.string.other), false);
    }

    private void setupDateTimePicker() {
        etDateTime.setOnClickListener(v -> showDateTimePicker());
    }

    private void showDateTimePicker() {
        Calendar current = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Mostrar selector de hora después del selector de fecha
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    requireContext(),
                    (timeView, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateDateTimeDisplay();
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true
                );
                timePickerDialog.show();
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        etDateTime.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void loadUserDefaults() {
        new Thread(() -> {
            UserSettings settings = database.userSettingsDao().getUserSettings();
            if (settings != null) {
                requireActivity().runOnUiThread(() -> {
                    etName.setText(settings.getUserName());
                    etAge.setText(String.valueOf(settings.getUserAge()));

                    String gender = settings.getUserGender();
                    switch (gender) {
                        case "male":
                            spinnerGender.setText(getString(R.string.male), false);
                            break;
                        case "female":
                            spinnerGender.setText(getString(R.string.female), false);
                            break;
                        case "other":
                            spinnerGender.setText(getString(R.string.other), false);
                            break;
                    }
                });
            }
        }).start();
    }

    private void setupSaveButton() {
        btnSaveRecord.setOnClickListener(v -> saveRecord());
    }

    private void saveRecord() {
        // Validar campos obligatorios
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String systolicStr = etSystolic.getText().toString().trim();
        String diastolicStr = etDiastolic.getText().toString().trim();
        String genderText = spinnerGender.getText().toString().trim();
        String observations = etObservations.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty() || systolicStr.isEmpty() ||
            diastolicStr.isEmpty() || genderText.isEmpty()) {
            Toast.makeText(getContext(), "Por favor completa todos los campos obligatorios",
                         Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            int systolic = Integer.parseInt(systolicStr);
            int diastolic = Integer.parseInt(diastolicStr);

            // Validar rangos de presión arterial
            if (systolic < 50 || systolic > 250 || diastolic < 30 || diastolic > 150) {
                Toast.makeText(getContext(), getString(R.string.invalid_pressure),
                             Toast.LENGTH_SHORT).show();
                return;
            }

            // Convertir género a formato interno
            String gender = convertGenderToInternal(genderText);

            // Si hay observaciones, procesarlas con IA antes de guardar
            if (!observations.isEmpty() && !ApiClient.getApiKey().isEmpty()) {
                processObservationsWithAI(name, age, gender, systolic, diastolic, observations);
            } else {
                // Guardar directamente si no hay observaciones o no hay API
                saveRecordToDatabase(name, age, gender, systolic, diastolic, observations);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Por favor ingresa valores numéricos válidos",
                         Toast.LENGTH_SHORT).show();
        }
    }

    private void processObservationsWithAI(String name, int age, String gender, int systolic, int diastolic, String observations) {
        // Mostrar loading
        btnSaveRecord.setEnabled(false);
        btnSaveRecord.setText("⏳ Procesando con IA...");

        // Construir prompt corto para análisis de observaciones
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analiza estas observaciones médicas de presión arterial y responde en MÁXIMO 50 palabras:\n\n");
        prompt.append("Presión: ").append(systolic).append("/").append(diastolic).append(" mmHg\n");
        prompt.append("Observaciones del paciente: ").append(observations).append("\n\n");
        prompt.append("Da UNA recomendación breve y práctica de estilo de vida. NO menciones medicamentos.\n");
        prompt.append("Respuesta en español, máximo 50 palabras:");

        // Crear mensaje para la API
        List<AIRequest.Message> messages = new ArrayList<>();
        messages.add(new AIRequest.Message("user", prompt.toString()));

        AIRequest request = new AIRequest(
            "x-ai/grok-4-fast:free",
            messages,
            100, // Tokens reducidos para respuesta corta
            0.5  // Temperature más baja para respuestas más consistentes
        );

        // Llamar a la API
        String authHeader = "Bearer " + ApiClient.getApiKey();
        ApiClient.getAIService().getHealthRecommendation(authHeader, request)
            .enqueue(new Callback<AIResponse>() {
                @Override
                public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                    btnSaveRecord.setEnabled(true);
                    btnSaveRecord.setText(getString(R.string.save_record));

                    String aiResponse = "";
                    if (response.isSuccessful() && response.body() != null &&
                        response.body().getChoices() != null && !response.body().getChoices().isEmpty()) {
                        aiResponse = response.body().getChoices().get(0).getMessage().getContent();
                    }

                    // Combinar observaciones del usuario con respuesta de IA
                    String finalObservations = observations;
                    if (!aiResponse.isEmpty()) {
                        finalObservations += "\n\n💡 IA: " + aiResponse;
                    }

                    saveRecordToDatabase(name, age, gender, systolic, diastolic, finalObservations);
                }

                @Override
                public void onFailure(Call<AIResponse> call, Throwable t) {
                    btnSaveRecord.setEnabled(true);
                    btnSaveRecord.setText(getString(R.string.save_record));
                    // Guardar sin IA si falla
                    saveRecordToDatabase(name, age, gender, systolic, diastolic, observations);
                }
            });
    }

    private void saveRecordToDatabase(String name, int age, String gender, int systolic, int diastolic, String observations) {
        // Crear registro
        BloodPressureRecord record = new BloodPressureRecord(
            name, age, gender, systolic, diastolic,
            selectedDateTime.getTimeInMillis(), observations
        );

        // Guardar en base de datos
        new Thread(() -> {
            database.bloodPressureDao().insert(record);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), getString(R.string.record_saved),
                             Toast.LENGTH_SHORT).show();
                clearForm();

                // Navegar de vuelta al dashboard
                if (getActivity() != null) {
                    com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        getActivity().findViewById(R.id.bottom_navigation);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_dashboard);
                    }
                }
            });
        }).start();
    }

    private void clearForm() {
        etSystolic.setText("");
        etDiastolic.setText("");
        etObservations.setText("");
        selectedDateTime = Calendar.getInstance();
        updateDateTimeDisplay();
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
