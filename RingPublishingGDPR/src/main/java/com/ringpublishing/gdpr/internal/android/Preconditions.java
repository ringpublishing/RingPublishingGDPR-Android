package com.ringpublishing.gdpr.internal.android;

import androidx.annotation.NonNull;

/**
 * Simple static methods to be called at the start of your own methods to verify
 * correct arguments and state.
 */
public final class Preconditions
{

    /**
     * Ensures that an expression checking an argument is true.
     *
     * @param expression the expression to check
     * @param errorMessage the exception message to use if the check fails
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, @NonNull String errorMessage)
    {
        if (!expression)
        {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private Preconditions()
    {
    }
}
