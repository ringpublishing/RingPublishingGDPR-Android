package com.ringpublishing.gdpr.internal.model;

public class TenantConfiguration
{
    public enum TenantState {
        LOADING,
        ERROR,
        CONFIGURATION_FINISHED,
        VERIFY_ERROR,
        VERIFY_FINISHED
    }

    private boolean gdprApplies;

    private String host;

    private TenantState state = TenantState.LOADING;

    public void setGdprApplies(boolean gdprApplies)
    {
        this.gdprApplies = gdprApplies;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setState(TenantState state)
    {
        this.state = state;
    }

    public TenantState getState()
    {
        return state;
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
