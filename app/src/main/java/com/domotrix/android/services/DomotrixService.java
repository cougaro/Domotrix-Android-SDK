package com.domotrix.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DomotrixService extends Service {
	public final static String TAG = "DomotrixService";

	///////////////////////////////////////////////////////////////////////
	// IDomotrixService AIDL implementation
	///////////////////////////////////////////////////////////////////////

	private IDomotrixService.Stub apiEndpoint = new IDomotrixService.Stub() {
		@Override
		public void remoteLog(String source, String message) throws RemoteException {
			Log.d(TAG,"["+source+"] :"+message);
		}
	};

	///////////////////////////////////////////////////////////////////////
	// Service implementation 
	///////////////////////////////////////////////////////////////////////
	
	@Override
	public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind SERVICE Called");
        Log.d(TAG,"Remote getName() "+IDomotrixService.class.getName());
        Log.d(TAG,"Intent Action() "+intent.getAction());
        if (IDomotrixService.class.getName().equals(intent.getAction())) {
			return apiEndpoint;
		} else {
			return null;
		}
	}

	@Override
	public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate SERVICE CALLED");
    }

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
		Log.d(TAG,"onStartCommand SERVICE CALLED");
        return START_STICKY;
    }	

	@Override
	public void onDestroy() {
		Log.d(TAG,"DESTROY SERVICE");
		super.onDestroy();
	}
}