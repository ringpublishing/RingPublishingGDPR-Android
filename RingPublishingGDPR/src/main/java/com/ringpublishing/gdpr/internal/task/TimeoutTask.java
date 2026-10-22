package com.ringpublishing.gdpr.internal.task;

import android.os.Handler;
import android.os.Looper;

import com.ringpublishing.gdpr.BuildConfig;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TimeoutTask
{

    private int timeout = BuildConfig.DEFAULT_TIMEOUT;

    @Nullable
    private Handler handler;

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public synchronized void start(@NonNull TimeoutCallback timeoutCallback)
    {
        cancel();
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(timeoutCallback::onTimeout, TimeUnit.SECONDS.toMillis(timeout));
    }

    public synchronized void cancel()
    {
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
