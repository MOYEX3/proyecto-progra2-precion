package com.example.precional;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.precional.databinding.ActivityLoginBinding;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "PrecionalPrefs";
    private static final String IS_FIRST_RUN = "isFirstRun";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Verificar si es la primera ejecución de la app
        boolean isFirstRun = sharedPreferences.getBoolean(IS_FIRST_RUN, true);
        if (isFirstRun) {
            // Si es la primera vez, redirigir al registro
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvForgotPassword.setOnClickListener(v -> {
            // Solo mostrar un mensaje por ahora
            Snackbar.make(binding.getRoot(), "Esta función estará disponible pronto", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validar campos
        if (username.isEmpty() || password.isEmpty()) {
            showMessage(getString(R.string.required_fields));
            return;
        }

        // Verificar credenciales
        String savedUsername = sharedPreferences.getString(USERNAME_KEY, "");
        String savedPassword = sharedPreferences.getString(PASSWORD_KEY, "");

        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            // Login exitoso
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Error de autenticación
            showMessage(getString(R.string.login_error));
            binding.etPassword.setText("");
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
