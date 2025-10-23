package com.example.precional.service.ai;

import com.example.precional.data.entity.BloodPressureRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Servicio para comunicarse con la API de OpenRouter
 */
public class OpenRouterService {
    // URL directa a la API sin posibilidad de error
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    // API key hardcodeada directamente en esta clase para evitar problemas de referencia
    private static final String API_KEY = "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";

    // Modelo más económico y rápido pero efectivo
    private static final String MODEL = "anthropic/claude-3-haiku";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client;

    public OpenRouterService(String apiKey) {
        // Ignoramos el parámetro apiKey y usamos la constante hardcodeada

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        this.client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Genera una recomendación personalizada basada en el registro de presión arterial
     */
    public String getRecommendation(BloodPressureRecord record, String userObservations,
                                   String userName, String userSex, Integer userAge) {
        if (record == null) {
            return "No hay datos suficientes para generar una recomendación.";
        }

        try {
            JSONObject jsonBody = new JSONObject();

            try {
                jsonBody.put("model", MODEL);

                JSONArray messagesArray = new JSONArray();

                // Mensaje del sistema - instrucciones para la IA
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "Eres un asistente médico especializado en presión arterial. Responde en español, de forma breve (máximo 3 líneas) y NO recomiendes medicamentos específicos.");
                messagesArray.put(systemMessage);

                // Mensaje del usuario - datos del paciente
                StringBuilder userContent = new StringBuilder();
                userContent.append("Analiza estos valores de presión arterial:\n");
                userContent.append("- Sistólica: ").append(record.getSystolic()).append(" mmHg\n");
                userContent.append("- Diastólica: ").append(record.getDiastolic()).append(" mmHg\n");

                if (userName != null && !userName.isEmpty()) {
                    userContent.append("- Paciente: ").append(userName);
                    if (userAge != null) userContent.append(", ").append(userAge).append(" años");
                    if (userSex != null && !userSex.isEmpty()) userContent.append(", ").append(userSex);
                    userContent.append("\n");
                }

                if (userObservations != null && !userObservations.isEmpty()) {
                    userContent.append("- Observaciones: ").append(userObservations).append("\n");
                }

                userContent.append("\nDame una recomendación concisa basada en estos valores.");

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", userContent.toString());
                messagesArray.put(userMessage);

                jsonBody.put("messages", messagesArray);
                jsonBody.put("max_tokens", 150);
                jsonBody.put("temperature", 0.7);

            } catch (JSONException e) {
                android.util.Log.e("OpenRouter", "Error creando JSON: " + e.getMessage());
                return "Error preparando consulta: " + e.getMessage();
            }

            // Crear y enviar la solicitud HTTP con la API key hardcodeada
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY) // Usamos la API key constante
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://github.com/Precional")
                    .addHeader("X-Title", "Precional-App")
                    .build();

            android.util.Log.d("OpenRouter", "Enviando solicitud a: " + API_URL);
            android.util.Log.d("OpenRouter", "Contenido JSON: " + jsonBody.toString());
            android.util.Log.d("OpenRouter", "API key (primeros caracteres): " + API_KEY.substring(0, 15) + "...");

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    android.util.Log.e("OpenRouter", "Error HTTP " + response.code() + ": " + responseBody);
                    if (response.code() == 401) {
                        return "Error de autenticación (401). La API key podría no ser válida.";
                    }
                    return "Error " + response.code() + " al conectar con la IA. Por favor, intenta más tarde.";
                }

                // Procesar respuesta exitosa
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray choices = jsonResponse.getJSONArray("choices");

                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        return message.getString("content");
                    } else {
                        return "No se obtuvo una recomendación clara de la IA.";
                    }
                } catch (JSONException e) {
                    android.util.Log.e("OpenRouter", "Error procesando respuesta: " + e.getMessage());
                    android.util.Log.e("OpenRouter", "Respuesta recibida: " + responseBody);
                    return "Error al procesar la respuesta de la IA.";
                }
            }
        } catch (IOException e) {
            android.util.Log.e("OpenRouter", "Error de conexión: " + e.getMessage(), e);
            return "Error de conexión: " + e.getMessage();
        }
    }
}
