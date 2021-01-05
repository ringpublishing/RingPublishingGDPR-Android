package com.ringpublishing.gdpr.internal.task;


import android.os.Handler;

import java.util.concurrent.TimeUnit;

public class TimeoutTask
{

    private final int timeout;

    private TimeoutCallback timeoutCallback;

    private Handler handler;

    public TimeoutTask(TimeoutCallback timeoutCallback, final int timeout)
    {
        this.timeoutCallback = timeoutCallback;
        this.timeout = timeout;
    }

    public void start()
    {
        handler = new Handler();
        handler.postDelayed(() ->
        {
            if (timeoutCallback != null)
            {
                timeoutCallback.onTimeout();
            }
        }, TimeUnit.SECONDS.toMillis(timeout));
    }

    public void cancel()
    {
        timeoutCallback = null;
        if (handler != null)
        {
            handler.removeCallbacksAndMessages(null);
        }
        handler = null;
    }

    public interface TimeoutCallback
    {

        void onTimeout();
    }

}
