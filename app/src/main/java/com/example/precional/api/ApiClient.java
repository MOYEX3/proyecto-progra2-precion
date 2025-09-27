package com.example.precional.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static String BASE_URL = ; // URL para OpenRouter/Grok
    private static String API_KEY = ; // Tu API key

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
        retrofit = null; // Resetear para usar nueva URL
    }

    public static void setApiKey(String apiKey) {
        API_KEY = apiKey;
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurar logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configurar cliente HTTP
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            httpClient.connectTimeout(60, TimeUnit.SECONDS); // Aumentar timeout para Grok
            httpClient.readTimeout(60, TimeUnit.SECONDS);

            // Crear cliente Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static AIApiService getAIService() {
        return getClient().create(AIApiService.class);
    }
}
