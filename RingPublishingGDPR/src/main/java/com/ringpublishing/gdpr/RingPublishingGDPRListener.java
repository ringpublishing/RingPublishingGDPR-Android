package com.ringpublishing.gdpr;

import com.ringpublishing.gdpr.internal.log.Logger;

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
    default void onError(RingPublishingGDPRError error, String detailMessage)
    {
        Logger.get().warn(String.format("RingPublishingGDPRListener Error: %s %s", error.name(), detailMessage));
    }

}
