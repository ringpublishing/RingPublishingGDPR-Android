package com.ringpublishing.gdpr.internal.api;

class VerifyResponse
{

    private static final String OK = "OK";

    @SuppressWarnings("WeakerAccess")
    String status;

    public boolean isValid()
    {
        return OK.equals(status);
    }

}
