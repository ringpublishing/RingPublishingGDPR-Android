package com.ringpublishing.gdpr.internal.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ringpublishing.gdpr.R;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ErrorView extends RelativeLayout
{

    @NonNull
    private final TextView messageTextView;

    @NonNull
    private final ButtonView retryButton;

    @Nullable
    private RetryCallback retryCallback;

    public ErrorView(Context context)
    {
        this(context, null);
    }

    public ErrorView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.ring_publishing_gdpr_view_error, this);

        messageTextView = findViewById(R.id.message_text);
        retryButton = findViewById(R.id.retry_button);

        retryButton.setOnClickListener(view ->
        {
            if (retryCallback != null)
            {
                retryCallback.onRetryClicked();
            }
        });
    }

    public void setRetryCallback(@NonNull RetryCallback retryCallback)
    {
        this.retryCallback = retryCallback;
    }

    public void setTypeface(@Nullable Typeface typeface)
    {
        messageTextView.setTypeface(typeface);
        retryButton.setTypeface(typeface);
    }

    public void setThemeColor(@ColorInt int themeColor)
    {
        retryButton.setThemeColor(themeColor);
    }

    public interface RetryCallback
    {

        void onRetryClicked();

    }

}
