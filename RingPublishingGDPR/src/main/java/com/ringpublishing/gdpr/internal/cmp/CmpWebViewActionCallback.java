package com.ringpublishing.gdpr.internal.cmp;

public interface CmpWebViewActionCallback
{
    void onActionLoaded();

    void onActionComplete();

    void onActionError(String error);

    void onActionInAppTCData(String tcData, boolean success);

    void getCompleteConsentData(String error, String dlData);
}
