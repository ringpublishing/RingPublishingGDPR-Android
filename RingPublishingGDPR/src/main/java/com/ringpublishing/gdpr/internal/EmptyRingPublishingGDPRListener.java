package com.ringpublishing.gdpr.internal;

import android.util.Log;

import com.ringpublishing.gdpr.RingPublishingGDPRListener;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

@RestrictTo(Scope.LIBRARY)
public class EmptyRingPublishingGDPRListener implements RingPublishingGDPRListener
{

    @Override
    public void onConsentsUpdated()
    {
        Log.d(RingPublishingGDPRListener.class.getCanonicalName(), "onConsentsUpdated");
    }

}
