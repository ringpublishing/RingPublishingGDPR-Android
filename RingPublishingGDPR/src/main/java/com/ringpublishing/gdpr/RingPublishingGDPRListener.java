package com.ringpublishing.gdpr;

import android.util.Log;

/**
 * Listener for consents changes
 */
public interface RingPublishingGDPRListener
{

    /**
     * Called after finish display consents screen, when updated consents has been saved
     */
    void onConsentsUpdated();

    /**
     * Called after receive error from initialize library or loading webview form
     *
     * @param error @{@link RingPublishingGDPRError}
     */
    default void onError(RingPublishingGDPRError error)
    {
        Log.w(RingPublishingGDPRListener.class.getCanonicalName(), String.format("Error: %s", error.name()));
    }

}
