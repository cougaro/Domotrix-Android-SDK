package com.domotrix.android.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.domotrix.android.services.DomotrixService;
import com.domotrix.android.services.IDomotrixService;

public abstract class BasicRemoteReceiver extends BroadcastReceiver {
	protected IDomotrixService mService = null;
	private boolean mIsBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = IDomotrixService.Stub.asInterface(service);
		}
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	protected boolean isConnected() {
		return mIsBound;
	}

	protected boolean connectToRemoteService(Context ctx) {
		Intent intent = new Intent(ctx, DomotrixService.class);
		intent.setAction(IDomotrixService.class.getName());
		ctx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		return mIsBound;
	}

	protected boolean disconnectFromRemoteService(Context ctx) { 
		if (mIsBound) {
			ctx.unbindService(mConnection);
			mIsBound = false;
			return true;
		} else { 
			return false;
		}
	}
}