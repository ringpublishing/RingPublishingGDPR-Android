package com.ringpublishing.gdpr.internal.model;

public class RequestsState
{
    private VerifyState verifyState = VerifyState.LOADING;

    private TenantState tenantState = TenantState.LOADING;

    public void setTenantState(TenantState tenantState)
    {
        this.tenantState = tenantState;
    }

    public void setVerifyState(VerifyState verifyState)
    {
        this.verifyState = verifyState;
    }

    public void setIsLoading()
    {
        verifyState = VerifyState.LOADING;
        tenantState = TenantState.LOADING;
    }

    public boolean isLoading()
    {
        return tenantState == TenantState.LOADING || verifyState == VerifyState.LOADING;
    }

    public boolean isFailure()
    {
        return tenantState == TenantState.FAILURE || verifyState == VerifyState.FAILURE;
    }
}
