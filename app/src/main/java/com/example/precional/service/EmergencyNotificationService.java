package com.example.precional.service;

import android.content.Context;
import android.util.Log;

import com.example.precional.api.N8nApiService;
import com.example.precional.api.N8nRequest;
import com.example.precional.data.entity.UserSettings;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class EmergencyNotificationService {
    private static final String TAG = "EmergencyNotification";
    private static final String N8N_WEBHOOK_URL = "https://primary-production-38135.up.railway.app/webhook/precion-arterial";

    private final Context context;
    private final N8nApiService n8nService;

    public EmergencyNotificationService(Context context) {
        this.context = context;

        // Configurar cliente HTTP con timeouts razonables
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Crear Retrofit para n8n (la URL base se usará desde el webhook completo)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://placeholder.com/") // URL base dummy, usaremos @Url
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        n8nService = retrofit.create(N8nApiService.class);
    }

    /**
     * Envía una notificación de emergencia a través de n8n
     */
    public void sendEmergencyAlert(UserSettings settings, int systolic, int diastolic,
                                   EmergencyNotificationCallback callback) {
        // Validar que exista configuración de emergencia
        if (settings == null) {
            callback.onError("No hay configuración de usuario");
            return;
        }

        if (settings.getEmergencyContactPhone() == null || settings.getEmergencyContactPhone().trim().isEmpty()) {
            callback.onError("Debe registrar un contacto de emergencia antes de continuar");
            return;
        }

        // Generar diagnóstico
        String diagnostico = getDiagnostico(systolic, diastolic);

        // Crear request
        N8nRequest request = new N8nRequest(
                settings.getUserName() != null ? settings.getUserName() : "Usuario",
                settings.getEmergencyContactName() != null ? settings.getEmergencyContactName() : "Contacto de emergencia",
                settings.getEmergencyContactPhone(),
                settings.getEmergencyContactEmail() != null ? settings.getEmergencyContactEmail() : "correo@noconfigurado.com",
                systolic,
                diastolic,
                diagnostico
        );

        // Enviar petición
        Log.d(TAG, "Enviando alerta de emergencia a: " + N8N_WEBHOOK_URL);
        Log.d(TAG, "Diagnóstico: " + diagnostico + " (" + systolic + "/" + diastolic + ")");

        n8nService.sendEmergencyAlert(N8N_WEBHOOK_URL, request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Alerta enviada exitosamente");
                            callback.onSuccess();
                        } else {
                            String error = "Error en el servidor: " + response.code();
                            Log.e(TAG, error);
                            callback.onError(error);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        String error = "Error de conexión: " + t.getMessage();
                        Log.e(TAG, error, t);
                        callback.onError(error);
                    }
                });
    }

    /**
     * Determina el diagnóstico basado en los valores de presión
     */
    private String getDiagnostico(int systolic, int diastolic) {
        if (systolic >= 180 || diastolic >= 110) {
            return "Crisis hipertensiva";
        } else if (systolic >= 140 || diastolic >= 90) {
            return "Presión alta";
        } else if (systolic >= 130 || diastolic >= 80) {
            return "Presión elevada";
        } else if (systolic >= 90 && diastolic >= 60) {
            return "Presión normal";
        } else {
            return "Presión baja";
        }
    }

    /**
     * Callback para notificaciones de emergencia
     */
    public interface EmergencyNotificationCallback {
        void onSuccess();
        void onError(String error);
    }
}
