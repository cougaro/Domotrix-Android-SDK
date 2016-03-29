package com.domotrix.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTSService extends Service implements TextToSpeech.OnInitListener {
    private TextToSpeech mTts;
    private boolean mReady;
    String text_to_speech = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String text_to_speech = intent.getStringExtra("TEXT_TO_SPEECH");
        if (mReady) {
            if (android.os.Build.VERSION.SDK_INT < 21) {
                mTts.speak(text_to_speech, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                mTts.speak(text_to_speech, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } else {
            this.text_to_speech = text_to_speech;
            mTts = new TextToSpeech(this, this);
        }
        return Service.START_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.ITALIAN);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                if (android.os.Build.VERSION.SDK_INT < 21) {
                    mTts.speak(this.text_to_speech, TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    mTts.speak(this.text_to_speech, TextToSpeech.QUEUE_FLUSH, null, null);
                }
                mReady = true;
                return;
            }
        }
        mReady = false;
    }

    @Override
    public void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}