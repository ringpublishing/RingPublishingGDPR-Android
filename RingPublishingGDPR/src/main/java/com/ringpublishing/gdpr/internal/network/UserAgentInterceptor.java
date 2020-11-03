package com.ringpublishing.gdpr.internal.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor
{

    public static final String USER_AGENT_HEADER = "User-Agent";

    private String userAgentValue;

    public UserAgentInterceptor(String userAgentValue)
    {
        this.userAgentValue = userAgentValue;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException
    {
        final Request originalRequest = chain.request();
        final Request requestWithUserAgent = originalRequest.newBuilder()
                .removeHeader(USER_AGENT_HEADER)
                .addHeader(USER_AGENT_HEADER, userAgentValue)
                .build();

        return chain.proceed(requestWithUserAgent);
    }

}

