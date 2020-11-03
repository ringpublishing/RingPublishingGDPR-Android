package com.ringpublishing.gdpr.internal.model;

public class State
{

    public static final String LOADING = "LOADING";

    public static final String CONTENT = "LOADED";

    public static final String ERROR = "ERROR";

    private String current = LOADING;

    public String getCurrent()
    {
        return current;
    }

    public boolean isLoading()
    {
        return LOADING.equals(current);
    }

    public boolean isLoaded()
    {
        return CONTENT.equals(current);
    }

    public boolean isError()
    {
        return ERROR.equals(current);
    }

    @Override
    public String toString()
    {
        return "Current state is: " + current;
    }

    public void loading()
    {
        current = LOADING;
    }

    public void content()
    {
        current = CONTENT;
    }

    public void error()
    {
        current = ERROR;
    }
}
