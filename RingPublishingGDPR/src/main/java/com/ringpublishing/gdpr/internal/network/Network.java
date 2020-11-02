package com.ringpublishing.gdpr.internal.network;

import android.content.Context;

import com.ringpublishing.gdpr.BuildConfig;

import androidx.annotation.NonNull;
import okhttp3.HttpUrl.Builder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Network
{

    private Context appContext;

    public Network(Context appContext)
    {
        this.appContext = appContext;
    }

    @NonNull
    public Retrofit createRetrofit(@NonNull String tenantId)
    {
        final String url = new Builder()
                .scheme("https")
                .host(BuildConfig.CMP_HOST)
                .addPathSegment(tenantId)
                .build()
                .toString();

        return new Retrofit.Builder().baseUrl(url.endsWith("/") ? url : url + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient())
                .build();
    }

    @NonNull
    private OkHttpClient createOkHttpClient()
    {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(createUserAgentInterceptor())
                .build();
    }

    private UserAgentInterceptor createUserAgentInterceptor()
    {
        return new UserAgentInterceptor(createUserAgentHeader());
    }

    public String createUserAgentHeader()
    {
        return UserAgent.getInterceptorHeader(appContext);
    }
}
