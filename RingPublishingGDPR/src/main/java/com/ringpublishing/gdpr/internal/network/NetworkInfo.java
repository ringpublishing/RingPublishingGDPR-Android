package com.ringpublishing.gdpr.internal.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

public class NetworkInfo
{

    @NonNull
    private final ConnectivityManager cm;

    public NetworkInfo(Context appContext)
    {
        cm = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isOnline()
    {
        boolean isConnected = false;
        android.net.Network[] allNetworks = cm.getAllNetworks();

        for (android.net.Network network : allNetworks)
        {
            NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(network);
            if (networkCapabilities != null)
            {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                {
                    isConnected = true;
                }
            }
        }
        return isConnected;
    }

}
