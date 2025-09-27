package com.example.precional.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AIApiService {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    Call<AIResponse> getHealthRecommendation(
        @Header("Authorization") String authorization,
        @Body AIRequest request
    );
}
