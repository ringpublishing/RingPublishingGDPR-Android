package com.ringpublishing.gdpr;

/**
 * Listener for consents changes
 */
public interface RingPublishingGDPRListener
{
    /**
     * Called after finish display consents screen, when updated consents has been saved
     */
    void onConsentsUpdated();
}
