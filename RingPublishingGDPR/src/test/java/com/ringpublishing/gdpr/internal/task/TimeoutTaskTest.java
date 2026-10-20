package com.ringpublishing.gdpr.internal.task;

import android.os.Handler;

import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TimeoutTaskTest
{

    @Test
    public void testStart_WhenCalledTwice_ThenFirstTimerCancelledAndOnlySecondPending()
    {
        try (MockedConstruction<Handler> handlers = Mockito.mockConstruction(Handler.class))
        {
            final TimeoutTask task = new TimeoutTask();

            task.start(() -> { });
            task.start(() -> { });

            assertEquals(2, handlers.constructed().size());
            final Handler first = handlers.constructed().get(0);
            final Handler second = handlers.constructed().get(1);
            verify(first).postDelayed(any(Runnable.class), anyLong());
            verify(first).removeCallbacksAndMessages(null);
            verify(second).postDelayed(any(Runnable.class), anyLong());
            verify(second, never()).removeCallbacksAndMessages(null);

            task.cancel();

            verify(second).removeCallbacksAndMessages(null);
        }
    }
}
