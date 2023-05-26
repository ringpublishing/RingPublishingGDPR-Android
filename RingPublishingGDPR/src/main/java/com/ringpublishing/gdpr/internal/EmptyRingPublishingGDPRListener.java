package com.ringpublishing.gdpr.internal;

import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.log.Logger;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

@RestrictTo(Scope.LIBRARY)
public class EmptyRingPublishingGDPRListener implements RingPublishingGDPRListener
{

    @Override
    public void onConsentsUpdated()
    {
        Logger.get().debug("Empty implementation method onConsentsUpdated() called");
    }

}
