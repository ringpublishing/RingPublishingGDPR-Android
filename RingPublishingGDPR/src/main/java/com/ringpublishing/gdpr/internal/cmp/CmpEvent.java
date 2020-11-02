package com.ringpublishing.gdpr.internal.cmp;

import androidx.annotation.NonNull;

public enum CmpEvent
{
    tcloaded("tcloaded"),
    cmpuishown("cmpuishown"),
    useractioncomplete("useractioncomplete");

    private String name;

    CmpEvent(@NonNull String name)
    {
        this.name = name;
    }

    public static CmpEvent fromString(String text)
    {
        for (CmpEvent cmpEvent : CmpEvent.values())
        {
            if (cmpEvent.name.equalsIgnoreCase(text))
            {
                return cmpEvent;
            }
        }
        return null;
    }

    @Override public String toString()
    {
        return name;
    }
}
