package com.ringpublishing.gdpr.internal.api;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ringpublishing.gdpr.BuildConfig;
import com.ringpublishing.gdpr.internal.log.Logger;
import com.ringpublishing.gdpr.internal.network.Network;

import java.util.Map;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Api
{
    private final ApiDefinition apiDefinition;

    private final Network network;

    private final String brandName;

    private final Boolean forcedGDPRApplies;

    private final Logger log = Logger.get();

    public Api(@NonNull Context applicationContext, @NonNull String tenantId, @NonNull String brandName, int timeoutInSeconds, Boolean forcedGDPRApplies)
    {
        network = new Network(applicationContext, timeoutInSeconds);
        apiDefinition = network.createRetrofit(tenantId).create(ApiDefinition.class);
        this.brandName = brandName;
        this.forcedGDPRApplies = forcedGDPRApplies;
    }

    public void verify(final Map<String, String> consents, @NonNull VerifyCallback callback)
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
                            log.info( "After call verify api on response we have information, that consents are outdated");
                            callback.onOutdated(response.raw().toString());
                        }
                        else
                        {
                            log.info( "After call verify api on response we have information, that consents are actual");
                            callback.onActual(response.raw().toString());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VerifyResponse> call, @NonNull Throwable throwable)
                    {
                        log.warn("After call verify api on failure response we have error: " + throwable.getLocalizedMessage());

                        callback.onFailure("Request fail: " + throwable.getLocalizedMessage());
                    }
                });
    }

    public void configuration(@NonNull ConfigurationCallback callback)
    {
        if (forcedGDPRApplies == null)
        {
            apiDefinition.configuration(brandName).enqueue(configurationCallback(callback));
        }
        else
        {
            apiDefinition.configuration(brandName, forcedGDPRApplies ? "1" : "0").enqueue(configurationCallback(callback));
        }
    }

    @NonNull
    private Callback<JsonObject> configurationCallback(@NonNull ConfigurationCallback callback)
    {
        return new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                if (!response.isSuccessful())
                {
                    log.warn("After configuration api call response is not successful");
                    callback.onFailure();
                    return;
                }
                final JsonObject jsonObject = response.body();
                if (jsonObject == null)
                {
                    log.warn("After configuration api call response body is null");
                    callback.onFailure();
                    return;
                }

                parseResponseParameters(jsonObject);
            }

            private void parseResponseParameters(JsonObject jsonObject)
            {
                final JsonElement urlElement = jsonObject.get(BuildConfig.CMP_JSON_CONFIGURATION_FIELD_HOST);
                if (urlElement == null)
                {
                    log.warn("Configuration response body parameter CMP_JSON_CONFIGURATION_FIELD_HOST is null");
                    callback.onFailure();
                    return;
                }

                final String url = urlElement.getAsString();
                if (TextUtils.isEmpty(url))
                {
                    log.warn("Configuration response body parameter CMP_JSON_CONFIGURATION_FIELD_HOST is empty");
                    callback.onFailure();
                    return;
                }

                final JsonElement gdprAppliesElement = jsonObject.get(BuildConfig.CMP_JSON_CONFIGURATION_FIELD_GDPR_APPLIES);

                boolean gdprApplies = true;

                if (gdprAppliesElement != null)
                {
                    try
                    {
                        gdprApplies = gdprAppliesElement.getAsBoolean();
                        log.info(String.format("Configuration response url with brandName is:%s gdprApplies:%s ", url, gdprApplies));
                    }
                    catch (ClassCastException | IllegalStateException cce)
                    {
                        log.warn("Configuration response body parameter gdprApplies is not boolean!");
                        callback.onFailure();
                        return;
                    }
                }

                callback.onSuccess(url, gdprApplies);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable)
            {
                //Only when we received invalid data
                log.warn("Configuration response is failure. Error: " + throwable.getLocalizedMessage());
                callback.onFailure();
            }
        };
    }

    public Network getNetwork()
    {
        return network;
    }

    public interface VerifyCallback
    {

        void onActual(@NonNull String status);

        void onOutdated(@NonNull String status);

        void onFailure(@NonNull String status);
    }

    public interface ConfigurationCallback
    {

        void onSuccess(@NonNull String host, boolean gdprApplies);

        void onFailure();
    }
}
