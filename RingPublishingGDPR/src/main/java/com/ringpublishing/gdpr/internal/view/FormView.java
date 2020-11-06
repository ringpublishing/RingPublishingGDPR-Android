package com.ringpublishing.gdpr.internal.view;

import android.content.Context;
import android.widget.LinearLayout;

public abstract class FormView extends LinearLayout
{

    public FormView(Context context)
    {
        super(context);
    }

    public abstract void showConsentsWelcomeScreen();

    public abstract void showConsentsSettingsScreen();

    public abstract void onFailure(String errorMessage);
}
