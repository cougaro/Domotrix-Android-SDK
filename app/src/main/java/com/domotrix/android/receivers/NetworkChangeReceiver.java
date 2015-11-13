package com.domotrix.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.domotrix.android.services.NetworkService;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG,"******************** CHANGE NETWORK RECEIVER");
        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isWifiAvailable()) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String ssid_name = settings.getString("security_ssd", "Domotrix");
            if (serviceManager.networkNameEquals(ssid_name)) {
                Log.d(TAG,"You are connected to "+ssid_name);
                Intent serviceIntent = new Intent(context, NetworkService.class);
                context.startService(serviceIntent);
            } else {
                Log.e(TAG, "You are not connected to " + ssid_name);
                Intent serviceIntent = new Intent(context, NetworkService.class);
                context.stopService(intent);
            }
        }
    }
}
