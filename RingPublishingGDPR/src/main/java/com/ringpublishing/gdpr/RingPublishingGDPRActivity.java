package com.ringpublishing.gdpr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.ringpublishing.gdpr.internal.callback.RingPublishingGDPRActivityCallback;
import com.ringpublishing.gdpr.internal.view.FormView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity will display consent view to accept by user.
 * When is displayed by sdk on application start, will be start in new task.
 * When you want to display it manually, create Intent and start activity.
 *
 * You have two intents to choose. Default Welcome Screen and optional Settings screen
 * when you want display to user advanced view on start.
 */
public class RingPublishingGDPRActivity extends AppCompatActivity implements RingPublishingGDPRActivityCallback
{

    private static final String SCREEN_SETTINGS_BUNDLE_KEY = "SCREEN_SETTINGS";

    private FormView formView;

    /**
     * Create intent to open default consent screen
     * @param context to create intent
     * @return Intent that can be used to startActivity
     */
    static Intent createShowWelcomeScreenIntent(Context context)
    {
        return createStartIntent(context, false);
    }

    /**
     * Create intent to open settings consent screen
     * @param context to create intent
     * @return Intent that can be used to startActivity
     */
    static Intent createShowSettingsScreenIntent(Context context)
    {
        return createStartIntent(context, true);
    }

    private static Intent createStartIntent(Context context, boolean openSettingsScreen)
    {
        final Intent intent = new Intent(context, RingPublishingGDPRActivity.class);
        if (openSettingsScreen)
        {
            intent.putExtra(SCREEN_SETTINGS_BUNDLE_KEY, true);
        }
        return intent;
    }

    private ViewGroup layout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_publishing_gdpr);

        layout = findViewById(R.id.content);
        addViewToLayout();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        handleIntent();
    }

    @Override
    protected void onRestart()
    {
        if(layout.getChildCount() < 1)
        {
            clearLayout();
            addViewToLayout();
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy()
    {
        clearLayout();
        super.onDestroy();
    }

    private void addViewToLayout()
    {
        clearLayout();
        formView = RingPublishingGDPR.getInstance().getFormView(this.getApplicationContext());

        if(formView == null)
        {
            return;
        }

        RingPublishingGDPR.getInstance().setActivityCallback(this);

        formView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.addView(formView);

        handleIntent();
    }

    private void handleIntent()
    {
        if (getIntent() != null && getIntent().hasExtra(SCREEN_SETTINGS_BUNDLE_KEY))
        {
            formView.showConsentsSettingsScreen();
        }
        else
        {
            formView.showConsentsWelcomeScreen();
        }
    }

    private void clearLayout()
    {
        if(formView == null)
        {
            formView = RingPublishingGDPR.getInstance().getFormView(this.getApplicationContext());
        }

        if(formView != null)
        {
            if(formView.getParent() instanceof ViewGroup)
            {
                ((ViewGroup)formView.getParent()).removeView(formView);
            }
        }

        layout.removeAllViews();

        RingPublishingGDPR.getInstance().setActivityCallback(null);
    }

    @Override
    public void hide(FormView formView)
    {
        RingPublishingGDPR.getInstance().notifyConsentsUpdated();
        setResult(RESULT_OK);
        finish();
    }
}
