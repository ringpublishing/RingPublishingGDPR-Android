package com.ringpublishing.gdpr.internal.network;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.test.core.app.ApplicationProvider;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class NetworkTest
{

    private static final int TIMEOUT_SECONDS = 1;

    private static final long DRIBBLE_PERIOD_MS = 300;

    private static final long DRIBBLE_TOTAL_MS = 6000;

    private ServerSocket serverSocket;

    private final AtomicBoolean serverStopped = new AtomicBoolean(false);

    @Before
    public void setUp() throws IOException
    {
        serverSocket = new ServerSocket(0);
    }

    @After
    public void tearDown() throws IOException
    {
        serverStopped.set(true);
        serverSocket.close();
    }

    private Call newCall()
    {
        final Context context = ApplicationProvider.getApplicationContext();
        final OkHttpClient client = (OkHttpClient) new Network(context, TIMEOUT_SECONDS)
                .createRetrofit("tenant")
                .callFactory();
        final Request request = new Request.Builder().url("http://127.0.0.1:" + serverSocket.getLocalPort() + "/").build();
        return client.newCall(request);
    }

    private void startServer(final boolean dribble)
    {
        final Thread thread = new Thread(() ->
        {
            try (Socket socket = serverSocket.accept())
            {
                final InputStream in = socket.getInputStream();
                final byte[] buffer = new byte[4096];
                in.read(buffer);
                final OutputStream out = socket.getOutputStream();
                if (dribble)
                {
                    out.write("HTTP/1.1 200 OK\r\nContent-Length: 100000\r\n\r\n".getBytes());
                    out.flush();
                    final long end = System.currentTimeMillis() + DRIBBLE_TOTAL_MS;
                    while (!serverStopped.get() && System.currentTimeMillis() < end)
                    {
                        out.write('x');
                        out.flush();
                        Thread.sleep(DRIBBLE_PERIOD_MS);
                    }
                }
                else
                {
                    while (!serverStopped.get())
                    {
                        Thread.sleep(DRIBBLE_PERIOD_MS);
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private long elapsedUntilFailure(final Call call)
    {
        final long start = System.nanoTime();
        try (Response response = call.execute())
        {
            response.body().string();
            fail("Expected the call to fail");
        }
        catch (IOException expected)
        {
            return (System.nanoTime() - start) / 1_000_000;
        }
        return -1;
    }

    @Test
    public void testStalledServer_WhenNeverAnswers_ThenFailsWithinTimeout()
    {
        startServer(false);

        final long elapsedMs = elapsedUntilFailure(newCall());

        assertTrue("Failed after " + elapsedMs + " ms", elapsedMs < 3000);
    }

    @Test
    public void testSlowServer_WhenEachReadSucceedsButWholeCallTooLong_ThenFailsWithinTimeout()
    {
        startServer(true);

        final long elapsedMs = elapsedUntilFailure(newCall());

        assertTrue("Failed after " + elapsedMs + " ms", elapsedMs < 3000);
    }
}
