package com.domotrix.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.domotrix.android.services.NetworkService;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isWifiAvailable()) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String ssid_name = settings.getString("security_ssd", "Domotrix");
            if (serviceManager.networkNameEquals(ssid_name)) {
                // We are in the correct SSID Network... start searching domotrix
                Toast.makeText(context, "*** CONNECTED TO NETWORK ***", Toast.LENGTH_SHORT).show();
                Intent serviceIntent = new Intent(context, NetworkService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
