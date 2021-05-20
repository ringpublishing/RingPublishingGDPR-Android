package com.ringpublishing.gdpr.internal.model;

public class TenantConfiguration
{

    private boolean gdprApplies;

    private String host;

    public void setGdprApplies(boolean gdprApplies)
    {
        this.gdprApplies = gdprApplies;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getHost()
    {
        return host;
    }

    public boolean isGdprApplies()
    {
        return gdprApplies;
    }

}
