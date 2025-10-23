package com.example.precional;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.precional.data.database.AppDatabase;
import com.example.precional.data.entity.UserSettings;
import com.example.precional.ui.DashboardFragment;
import com.example.precional.ui.HistoryFragment;
import com.example.precional.ui.InformationFragment;
import com.example.precional.ui.NewRecordFragment;
import com.example.precional.ui.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST_CODE = 1001;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton addRecordButton;
    private FloatingActionButton emergencyButton;
    private AppDatabase database;
    private String pendingEmergencyCall = null; // Para manejar llamada después del permiso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar base de datos
        database = AppDatabase.getDatabase(this);
        initializeUserSettings();

        // Inicializar vistas
        initViews();
        setupBottomNavigation();
        setupFloatingButtons();

        // Cargar fragment inicial
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        addRecordButton = findViewById(R.id.add_record_button);
        emergencyButton = findViewById(R.id.emergency_button);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
                showAddButton(true);
            } else if (itemId == R.id.nav_new_record) {
                selectedFragment = new NewRecordFragment();
                showAddButton(false);
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                showAddButton(true);
            } else if (itemId == R.id.nav_information) {
                selectedFragment = new InformationFragment();
                showAddButton(false);
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                showAddButton(false);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void setupFloatingButtons() {
        addRecordButton.setOnClickListener(v -> bottomNavigation.setSelectedItemId(R.id.nav_new_record));

        emergencyButton.setOnClickListener(v -> showEmergencyDialog());
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    private void showAddButton(boolean show) {
        if (show) {
            addRecordButton.show();
        } else {
            addRecordButton.hide();
        }
    }

    private void initializeUserSettings() {
        // No es necesario inicializar los ajustes de usuario aquí
        // ya que ahora se configuran durante el registro
    }

    private void showEmergencyDialog() {
        new Thread(() -> {
            UserSettings settings = database.userSettingsDao().getUserSettings();
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.emergency_dialog_title));
                builder.setMessage(getString(R.string.emergency_title));

                // Opción 1: Me siento bien
                builder.setNeutralButton(getString(R.string.feeling_okay),
                    (dialog, which) -> dialog.dismiss());

                // Opción 2: Me siento más o menos mal - pregunta si quiere llamar
                builder.setNegativeButton(getString(R.string.feeling_unwell),
                    (dialog, which) -> showCallConfirmationDialog(settings));

                // Opción 3: Me siento muy mal - llama directamente
                builder.setPositiveButton(getString(R.string.feeling_very_bad),
                    (dialog, which) -> makeEmergencyCall(settings, true));

                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();

                // Personalizar colores de los botones
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.alert_red));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.warning_yellow));
            });
        }).start();
    }

    private void showCallConfirmationDialog(UserSettings settings) {
        if (settings == null || settings.getEmergencyContactPhone().isEmpty()) {
            Toast.makeText(this, getString(R.string.no_emergency_contact), Toast.LENGTH_LONG).show();
            // Navegar a configuración
            bottomNavigation.setSelectedItemId(R.id.nav_settings);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Contactar Emergencia");
        builder.setMessage("¿Deseas llamar a " + settings.getEmergencyContactName() +
                          "?\nTeléfono: " + settings.getEmergencyContactPhone());

        builder.setPositiveButton("Llamar", (dialog, which) -> makeEmergencyCall(settings, false));

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.alert_red));
    }

    private void makeEmergencyCall(UserSettings settings, boolean isDirect) {
        // Verificar si hay contacto configurado
        if (settings == null || settings.getEmergencyContactPhone().isEmpty()) {
            if (isDirect) {
                Toast.makeText(this, "No hay contacto de emergencia configurado. Ve a Configuración.", Toast.LENGTH_LONG).show();
                bottomNavigation.setSelectedItemId(R.id.nav_settings);
            } else {
                Toast.makeText(this, getString(R.string.no_emergency_contact), Toast.LENGTH_LONG).show();
            }
            return;
        }

        String phoneNumber = settings.getEmergencyContactPhone();

        // Verificar permisos de llamada
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            // Guardar número para llamar después del permiso
            pendingEmergencyCall = phoneNumber;
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CALL_PHONE},
                CALL_PERMISSION_REQUEST_CODE);
        } else {
            // Realizar la llamada
            performCall(phoneNumber, settings.getEmergencyContactName(), isDirect);
        }
    }

    private void performCall(String phoneNumber, String contactName, boolean isDirect) {
        try {
            String message = isDirect ?
                "Llamando directamente a " + contactName + "..." :
                "Llamando a " + contactName + "...";

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
            Toast.makeText(this, "Error al realizar la llamada: " + e.getMessage(),
                         Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, realizar la llamada pendiente
                if (pendingEmergencyCall != null) {
                    new Thread(() -> {
                        UserSettings settings = database.userSettingsDao().getUserSettings();
                        runOnUiThread(() -> {
                            performCall(pendingEmergencyCall,
                                      settings != null ? settings.getEmergencyContactName() : "Contacto",
                                      false);
                        });
                    }).start();
                    pendingEmergencyCall = null;
                }
            } else {
                Toast.makeText(this, getString(R.string.permission_needed), Toast.LENGTH_LONG).show();
                pendingEmergencyCall = null;
            }
        }
    }
}