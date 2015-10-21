package com.domotrix.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.domotrix.android.services.DomotrixService;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(DomotrixService.class.getName());
        context.startService(serviceIntent); 
	}
}
