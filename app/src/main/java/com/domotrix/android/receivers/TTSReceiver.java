package com.domotrix.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.domotrix.android.services.TTSService;

/**
 * Created by cougaro on 29/03/16.
 */
public class TTSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String text_to_speech = intent.getExtras().getString("TEXT_TO_SPEECH");
        Intent mIntent = new Intent(context, TTSService.class);
        Bundle extras = mIntent.getExtras();
        extras.putString("TEXT_TO_SPEECH", text_to_speech);
        context.startService(mIntent);
    }
}
