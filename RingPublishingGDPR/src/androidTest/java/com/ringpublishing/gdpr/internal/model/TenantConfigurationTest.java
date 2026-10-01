package com.ringpublishing.gdpr.internal.model;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TenantConfigurationTest
{

    private TenantConfiguration configuration;

    @Before
    public void setup()
    {
        configuration = new TenantConfiguration();
    }

    @Test
    public void hostIsUnchangedWithoutAdditionalParameters()
    {
        configuration.setHost("https://cmp.example.com/form");

        assertEquals("https://cmp.example.com/form", configuration.getHostWithAdditionalQuery());
    }

    @Test
    public void nullHostStaysNull()
    {
        configuration.setAdditionalQueryParameters(params("app_is", "abc"));

        assertNull(configuration.getHostWithAdditionalQuery());
    }

    @Test
    public void parametersAreAppendedToHostWithoutQuery()
    {
        configuration.setHost("https://cmp.example.com/form");
        configuration.setAdditionalQueryParameters(params("app_is", "abc"));

        assertEquals("https://cmp.example.com/form?app_is=abc", configuration.getHostWithAdditionalQuery());
    }

    @Test
    public void existingQueryParametersArePreserved()
    {
        configuration.setHost("https://cmp.example.com/form?site=onet");
        configuration.setAdditionalQueryParameters(params("app_is", "abc"));

        assertEquals("https://cmp.example.com/form?site=onet&app_is=abc", configuration.getHostWithAdditionalQuery());
    }

    @Test
    public void sameNamedParameterReplacesExistingOne()
    {
        configuration.setHost("https://cmp.example.com/form?app_is=old&site=onet");
        configuration.setAdditionalQueryParameters(params("app_is", "new"));

        assertEquals("https://cmp.example.com/form?site=onet&app_is=new", configuration.getHostWithAdditionalQuery());
    }

    @Test
    public void valuesAreEncodedOnceAndExistingEncodingIsNotDoubled()
    {
        configuration.setHost("https://cmp.example.com/form?q=a%20b");
        configuration.setAdditionalQueryParameters(params("app_is", "a b&c=d/+"));

        assertEquals("https://cmp.example.com/form?q=a%20b&app_is=a%20b%26c%3Dd%2F%2B", configuration.getHostWithAdditionalQuery());
    }

    private static Map<String, String> params(String key, String value)
    {
        final Map<String, String> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }
}
