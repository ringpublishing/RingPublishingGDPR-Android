package com.ringpublishing.gdpr.internal.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TenantConfiguration
{

    private boolean gdprApplies;

    @Nullable
    private String host;

    @NonNull
    private Map<String, String> additionalQueryParameters = Collections.emptyMap();

    public void setAdditionalQueryParameters(@NonNull Map<String, String> additionalQueryParameters)
    {
        this.additionalQueryParameters = new LinkedHashMap<>(additionalQueryParameters);
    }

    /**
     * CMP host with additional query parameters appended. A parameter replaces any same-named one already in the host URL.
     */
    @Nullable
    public String getHostWithAdditionalQuery()
    {
        if (host == null || additionalQueryParameters.isEmpty())
        {
            return host;
        }

        final Uri hostUri = Uri.parse(host);
        final Uri.Builder builder = hostUri.buildUpon().clearQuery();
        for (String name : hostUri.getQueryParameterNames())
        {
            if (!additionalQueryParameters.containsKey(name))
            {
                for (String value : hostUri.getQueryParameters(name))
                {
                    builder.appendQueryParameter(name, value);
                }
            }
        }
        for (Map.Entry<String, String> entry : additionalQueryParameters.entrySet())
        {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().toString();
    }

    public void setGdprApplies(boolean gdprApplies)
    {
        this.gdprApplies = gdprApplies;
    }

    public void setHost(@Nullable String host)
    {
        this.host = host;
    }

    @Nullable
    public String getHost()
    {
        return host;
    }

    public boolean isGdprApplies()
    {
        return gdprApplies;
    }

}
