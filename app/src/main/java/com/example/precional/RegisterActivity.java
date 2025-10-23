package com.example.precional;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.UserSettings;
import com.example.precional.databinding.ActivityRegisterBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private SharedPreferences sharedPreferences;
    private AppDatabase database;

    private static final String PREFS_NAME = "PrecionalPrefs";
    private static final String IS_FIRST_RUN = "isFirstRun";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityRegisterBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            database = AppDatabase.getDatabase(this);

            setupSpinners();
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al inicializar la pantalla: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupSpinners() {
        try {
            // Configurar spinner de género
            String[] genderOptions = {"Masculino", "Femenino", "Otro"};
            ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, genderOptions);
            binding.spinnerRegisterGender.setAdapter(genderAdapter);

            // Configurar spinner de tipo de sangre
            String[] bloodTypeOptions = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            ArrayAdapter<String> bloodTypeAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, bloodTypeOptions);
            binding.spinnerRegisterBloodType.setAdapter(bloodTypeAdapter);

            // Establecer valores por defecto de forma segura
            if (binding.spinnerRegisterGender != null) {
                binding.spinnerRegisterGender.setText("Masculino", false);
            }
            if (binding.spinnerRegisterBloodType != null) {
                binding.spinnerRegisterBloodType.setText("O+", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error configurando spinners: " + e.getMessage());
        }
    }

    private void setupListeners() {
        if (binding.btnRegisterSave != null) {
            binding.btnRegisterSave.setOnClickListener(v -> validateAndSave());
        }
    }

    private void validateAndSave() {
        try {
            // Validar que el binding esté disponible
            if (binding == null) {
                showMessage("Error: Vista no disponible");
                return;
            }

            // Obtener valores de los campos con validación nula
            String fullName = binding.etRegisterFullName != null ?
                binding.etRegisterFullName.getText().toString().trim() : "";
            String username = binding.etRegisterUsername != null ?
                binding.etRegisterUsername.getText().toString().trim() : "";
            String password = binding.etRegisterPassword != null ?
                binding.etRegisterPassword.getText().toString().trim() : "";
            String ageStr = binding.etRegisterAge != null ?
                binding.etRegisterAge.getText().toString().trim() : "";
            String gender = binding.spinnerRegisterGender != null ?
                binding.spinnerRegisterGender.getText().toString() : "Otro";
            String bloodType = binding.spinnerRegisterBloodType != null ?
                binding.spinnerRegisterBloodType.getText().toString() : "O+";
            String weightStr = binding.etRegisterWeight != null ?
                binding.etRegisterWeight.getText().toString().trim() : "";
            String heightStr = binding.etRegisterHeight != null ?
                binding.etRegisterHeight.getText().toString().trim() : "";
            String allergies = binding.etRegisterAllergies != null ?
                binding.etRegisterAllergies.getText().toString().trim() : "";
            String chronicDiseases = binding.etRegisterChronicDiseases != null ?
                binding.etRegisterChronicDiseases.getText().toString().trim() : "";
            String comments = binding.etRegisterComments != null ?
                binding.etRegisterComments.getText().toString().trim() : "";

            // Validar campos obligatorios
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() ||
                ageStr.isEmpty() || gender.isEmpty()) {
                showMessage(getString(R.string.required_fields));
                return;
            }

            // Convertir valores numéricos con validación
            int age;
            float weight = 0f;
            int height = 0;

            try {
                age = Integer.parseInt(ageStr);
                if (age < 1 || age > 150) {
                    showMessage("Edad debe estar entre 1 y 150 años");
                    return;
                }

                if (!weightStr.isEmpty()) {
                    weight = Float.parseFloat(weightStr);
                    if (weight < 0 || weight > 500) {
                        showMessage("Peso debe estar entre 0 y 500 kg");
                        return;
                    }
                }

                if (!heightStr.isEmpty()) {
                    height = Integer.parseInt(heightStr);
                    if (height < 0 || height > 300) {
                        showMessage("Altura debe estar entre 0 y 300 cm");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                showMessage(getString(R.string.invalid_numeric_values));
                return;
            }

            // Guardar credenciales en SharedPreferences
            try {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(IS_FIRST_RUN, false);
                editor.putString(USERNAME_KEY, username);
                editor.putString(PASSWORD_KEY, password);
                editor.apply();
            } catch (Exception e) {
                showMessage("Error guardando credenciales: " + e.getMessage());
                return;
            }

            // Guardar información del usuario en la base de datos
            saveUserToDatabase(fullName, age, gender, bloodType, weight, height, allergies, chronicDiseases, comments);

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error en validación: " + e.getMessage());
        }
    }

    private void saveUserToDatabase(String fullName, int age, String gender, String bloodType,
                                  float weight, int height, String allergies, String chronicDiseases, String comments) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Validar que la base de datos esté disponible
                if (database == null) {
                    runOnUiThread(() -> showMessage("Error: Base de datos no disponible"));
                    return;
                }

                // Convertir género al formato interno
                String internalGender = "other";
                if ("Masculino".equals(gender)) {
                    internalGender = "male";
                } else if ("Femenino".equals(gender)) {
                    internalGender = "female";
                }

                // Crear o actualizar UserSettings
                UserSettings settings = database.userSettingsDao().getUserSettings();
                if (settings == null) {
                    settings = new UserSettings();
                }

                // Actualizar configuración de forma segura
                settings.setUserName(fullName != null ? fullName : "Usuario");
                settings.setUserAge(age);
                settings.setUserGender(internalGender);
                settings.setBloodType(bloodType != null ? bloodType : "O+");
                settings.setUserWeight(weight);
                settings.setUserHeight(height);
                settings.setAllergies(allergies != null ? allergies : "");

                // Construir notas médicas de forma segura
                StringBuilder medicalNotesBuilder = new StringBuilder();
                if (comments != null && !comments.isEmpty()) {
                    medicalNotesBuilder.append(comments);
                }
                if (chronicDiseases != null && !chronicDiseases.isEmpty()) {
                    if (medicalNotesBuilder.length() > 0) {
                        medicalNotesBuilder.append("\n");
                    }
                    medicalNotesBuilder.append("Enfermedades crónicas: ").append(chronicDiseases);
                }
                settings.setMedicalNotes(medicalNotesBuilder.toString());

                // Guardar en base de datos
                database.userSettingsDao().insertOrUpdate(settings);

                // Una vez guardado, redirigir a la pantalla de inicio de sesión
                runOnUiThread(() -> {
                    showMessage(getString(R.string.registration_successful));
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showMessage("Error al guardar los datos: " + e.getMessage()));
            } finally {
                executor.shutdown();
            }
        });
    }

    private void showMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Liberar referencia para evitar memory leaks
    }
}
