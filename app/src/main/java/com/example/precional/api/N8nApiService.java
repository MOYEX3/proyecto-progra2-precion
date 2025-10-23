package com.example.precional.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface N8nApiService {
    @POST
    Call<ResponseBody> sendEmergencyAlert(@Url String url, @Body N8nRequest request);
}

