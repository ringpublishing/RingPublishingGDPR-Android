package com.ringpublishing.gdpr;

import androidx.annotation.NonNull;

public interface RingPublishingGDPROnErrorListener
{

    int ERROR_CODE_1 = 1;
    int ERROR_CODE_2 = 2;
    int ERROR_CODE_3 = 3;
    int ERROR_CODE_4 = 4;
    int ERROR_CODE_5 = 5;
    int ERROR_CODE_6 = 6;

    void onError(int errorCode, @NonNull String errorMessage);
}
