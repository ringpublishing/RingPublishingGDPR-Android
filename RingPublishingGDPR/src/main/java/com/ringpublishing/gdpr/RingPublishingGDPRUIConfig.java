package com.ringpublishing.gdpr;

import android.graphics.Typeface;

import androidx.annotation.ColorInt;

/**
 * UI configuration for native UI components
 */
public class RingPublishingGDPRUIConfig
{

    final Typeface typeface;

    @ColorInt
    final int themeColor;

    /**
     * UI configuration for initialization sdk
     * @param typeface for native UI components (Error view)
     * @param themeColor for native UI components (Error view)
     */
    public RingPublishingGDPRUIConfig(final Typeface typeface, @ColorInt final int themeColor)
    {
        this.typeface = typeface;
        this.themeColor = themeColor;
    }

    public Typeface getTypeface()
    {
        return typeface;
    }

    public int getThemeColor()
    {
        return themeColor;
    }
}
