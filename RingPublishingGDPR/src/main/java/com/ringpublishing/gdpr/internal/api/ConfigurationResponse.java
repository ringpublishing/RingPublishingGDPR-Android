package com.ringpublishing.gdpr.internal.api;

class ConfigurationResponse
{

    private static final String OK = "OK";

    @SuppressWarnings("WeakerAccess")
    String status;

    public boolean isValid()
    {
        return OK.equals(status);
    }

}
