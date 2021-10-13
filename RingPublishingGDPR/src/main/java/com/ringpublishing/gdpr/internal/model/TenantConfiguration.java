package com.ringpublishing.gdpr.internal.model;

import androidx.annotation.Nullable;

public class TenantConfiguration
{

    private boolean gdprApplies;

    @Nullable
    private String host;

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
