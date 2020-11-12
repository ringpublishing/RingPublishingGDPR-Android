package com.ringpublishing.gdpr.internal.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ringpublishing.gdpr.BuildConfig;
import com.ringpublishing.gdpr.internal.network.Network;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import androidx.annotation.NonNull;
import okhttp3.HttpUrl;
import okhttp3.HttpUrl.Builder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Api
{

    private final String TAG = Api.class.getCanonicalName();

    private final ApiDefinition apiDefinition;

    private final Network network;

    private final String brandName;

    public Api(@NonNull Context applicationContext, @NonNull String tenantId, @NonNull String brandName)
    {
        network = new Network(applicationContext);
        apiDefinition = network.createRetrofit(tenantId).create(ApiDefinition.class);
        this.brandName = brandName;
    }

    public void verify(final Map<String, String> consents, @NonNull VerifyApiCallback callback)
    {
        apiDefinition.verify(consents)
                .enqueue(new Callback<VerifyResponse>()
                {
                    @Override
                    public void onResponse(@NonNull Call<VerifyResponse> call, @NonNull Response<VerifyResponse> response)
                    {
                        final VerifyResponse body = response.body();
                        if (body == null || !body.isValid())
                        {
                            Log.i(TAG, "Verify response outdated");
                            callback.onOutdated(response.raw().toString());
                        }
                        else
                        {
                            Log.i(TAG, "Verify response actual");
                            callback.onActual(response.raw().toString());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VerifyResponse> call, @NonNull Throwable throwable)
                    {
                        Log.w(TAG, "Verify request failure", throwable);

                        callback.onFail("Request fail: " + throwable.getLocalizedMessage());
                    }
                });
    }

    public void configuration(@NonNull ConfigurationCallback callback)
    {
        apiDefinition.configuration(brandName)
                .enqueue(new Callback<JsonObject>()
                {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
                    {
                        if (!response.isSuccessful())
                        {
                            Log.w(TAG, "Configuration response not successful");
                            callback.onConfigurationFailure();
                            return;
                        }
                        final JsonObject jsonObject = response.body();
                        if (jsonObject == null)
                        {
                            Log.w(TAG, "Configuration response body is null");
                            callback.onConfigurationFailure();
                            return;
                        }

                        final JsonElement urlElement = jsonObject.get(BuildConfig.CMP_JSON_CONFIGURATION_FIELD_HOST);
                        if (urlElement == null)
                        {
                            Log.w(TAG, "Configuration response body parameter is null");
                            callback.onConfigurationFailure();
                            return;
                        }

                        final String url = urlElement.getAsString();
                        if (TextUtils.isEmpty(url))
                        {
                            Log.w(TAG, "Configuration response body parameter is empty");
                            callback.onConfigurationFailure();
                            return;
                        }

                        Log.w(TAG, "Configuration response url with brandName is: " + url);
                        callback.onConfigurationSuccess(url);
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable)
                    {
                        //Only when we received invalid data
                        Log.w(TAG, "Configuration response failure", throwable);
                        callback.onConfigurationFailure();
                    }
                });
    }

    public Network getNetwork()
    {
        return network;
    }

    public interface VerifyApiCallback
    {

        void onOutdated(String status);

        void onActual(String status);

        void onFail(String status);
    }

    public interface ConfigurationCallback
    {

        void onConfigurationSuccess(String response);

        void onConfigurationFailure();
    }
}
