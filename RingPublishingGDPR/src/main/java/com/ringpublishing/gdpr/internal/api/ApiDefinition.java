package com.ringpublishing.gdpr.internal.api;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiDefinition
{

    @GET("func/verify")
    Call<VerifyResponse> verify(@QueryMap Map<String, String> options);

    @GET("mobile")
    Call<JsonObject> configuration(@Query("site") String brandName);
}
