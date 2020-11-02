package com.ringpublishing.gdpr.internal.cmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JavaScriptResource
{

    public static String read(final InputStream is) throws IOException
    {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8.name()));
        final StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            stringBuilder.append(line);
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
