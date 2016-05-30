package com.domotrix.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class NetworkDiscovery {
    private final String TAG = NetworkDiscovery.class.getName();
    private final String TYPE = "_workstation._tcp.local.";
    private final String SERVICE_NAME = "DomotrixDiscoveryService";

    private Context mContext;
    private JmDNS mJmDNS;
    private ServiceInfo mServiceInfo;
    private ServiceListener mServiceListener;
    private WifiManager.MulticastLock mMulticastLock;

    public NetworkDiscovery(Context context) {
        mContext = context;
        try {
            mJmDNS = JmDNS.create();
        } catch (IOException e) {
            Log.d(TAG, "Error in JmDNS creation: " + e);
        }
    }

    public void findServers(final OnFoundListener listener) {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            mJmDNS.addServiceListener(TYPE, mServiceListener = new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent serviceEvent) {
                    ServiceInfo info = mJmDNS.getServiceInfo(serviceEvent.getType(), serviceEvent.getName());
                    if (listener != null) listener.onServiceAdded(info);
                }

                @Override
                public void serviceRemoved(ServiceEvent serviceEvent) {
                    if (listener != null) listener.onServiceRemoved(serviceEvent.getInfo());
                }

                @Override
                public void serviceResolved(ServiceEvent serviceEvent) {
                    mJmDNS.requestServiceInfo(serviceEvent.getType(), serviceEvent.getName(), 1);
                }
            });
        }
    }

    public void reset() {
        if (mJmDNS != null) {
            if (mServiceListener != null) {
                mJmDNS.removeServiceListener(TYPE, mServiceListener);
                mServiceListener = null;
            }
            mJmDNS.unregisterAllServices();
        }
        if (mMulticastLock != null && mMulticastLock.isHeld()) {
            mMulticastLock.release();
        }
    }

    public interface OnFoundListener {
        void onServiceAdded(ServiceInfo info);

        void onServiceRemoved(ServiceInfo info);
    }
}
