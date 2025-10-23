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

        if (settings.getN8nWebhookUrl() == null || settings.getN8nWebhookUrl().trim().isEmpty()) {
            callback.onError("Debe configurar la URL del webhook de n8n en ajustes");
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
        Log.d(TAG, "Enviando alerta de emergencia a: " + settings.getN8nWebhookUrl());
        Log.d(TAG, "Diagnóstico: " + diagnostico + " (" + systolic + "/" + diastolic + ")");

        n8nService.sendEmergencyAlert(settings.getN8nWebhookUrl(), request)
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
     * Genera el diagnóstico basado en los valores de presión arterial
     */
    private String getDiagnostico(int systolic, int diastolic) {
        if (systolic < 90 || diastolic < 60) {
            return "Presión baja";
        } else if (systolic < 120 && diastolic < 80) {
            return "Presión normal";
        } else if (systolic < 130 && diastolic < 80) {
            return "Presión elevada";
        } else if (systolic < 140 || diastolic < 90) {
            return "Hipertensión nivel 1";
        } else if (systolic < 180 || diastolic < 120) {
            return "Hipertensión nivel 2";
        } else {
            return "Crisis hipertensiva";
        }
    }

    /**
     * Callback para recibir el resultado del envío
     */
    public interface EmergencyNotificationCallback {
        void onSuccess();
        void onError(String message);
    }
}

