package com.ringpublishing.gdpr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

@RestrictTo(Scope.LIBRARY)
public class RingPublishingGDPRNotifier
{

    RingPublishingGDPRNotifier()
    {
        //Package scope constructor
    }

    private final List<RingPublishingGDPRListener> ringPublishingGDPRListeners = Collections.synchronizedList(new ArrayList<>());

    public void addRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRListeners.add(ringPublishingGDPRListener);
    }

    public void removeRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRListeners.remove(ringPublishingGDPRListener);
    }

    public void notifyConsentsUpdated()
    {
        synchronized (ringPublishingGDPRListeners)
        {
            for (RingPublishingGDPRListener listener : ringPublishingGDPRListeners)
            {
                listener.onConsentsUpdated();
            }
        }
    }

}
