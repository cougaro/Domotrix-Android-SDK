package com.domotrix.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DomotrixService extends Service {
	public final static String TAG = "DomotrixService";

	///////////////////////////////////////////////////////////////////////
	// IRemoteService AIDL implementation
	///////////////////////////////////////////////////////////////////////

	private IRemoteService.Stub apiEndpoint = new IRemoteService.Stub() {
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
		if (IRemoteService.class.getName().equals(intent.getAction())) {
			return apiEndpoint;
		} else {
			return null;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }	

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}