package com.domotrix.android.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.domotrix.android.Connection;
import com.domotrix.android.NetworkDiscovery;
import com.domotrix.android.listeners.SubscriptionListener;
import com.domotrix.android.utils.RepeatableAsyncTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jmdns.ServiceInfo;

public class DomotrixService extends Service {
	public final static String TAG = "DomotrixService";
    private Connection mConnection;

    private HashMap<String,RemoteCallbackList<IDomotrixServiceListener>> remote_hashmap = new HashMap<String, RemoteCallbackList<IDomotrixServiceListener>>();
    private SubscriptionListener dispatcherListener = new SubscriptionListener() {
        @Override
        public void onMessage(String wampEvent, String jsonMessage) {
            Log.d(TAG,"=============== WAMP MESSAGE RECEIVED");
            Log.d(TAG, wampEvent);
            Log.d(TAG,"=====================================");
            // Start the dispatcher
            /*
            synchronized (listeners) {
                // Broadcast to all clients the new value.
                int N = listeners.beginBroadcast();

                for (int i=0; i<N; i++) {
                    try {
                        listeners.getBroadcastItem(i).handleLocationUpdate();
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                listeners.finishBroadcast();
            }
            */
        }

        @Override
        public void onFault(String message) {
            Log.d(TAG,"================= WAMP FAULT RECEIVED");
            Log.d(TAG, message);
            Log.d(TAG,"=====================================");
        }
    };

    ///////////////////////////////////////////////////////////////////////
	// IDomotrixService AIDL implementation
	///////////////////////////////////////////////////////////////////////

	private IDomotrixService.Stub apiEndpoint = new IDomotrixService.Stub() {

		@Override
		public void remoteLog(String source, String message) throws RemoteException {
            if (!getAppName(getCallingPid()).equals("com.domotrix.domotrixdemo")) {
                throw new RemoteException("Unauthorized app");
            }
			Log.d(TAG,"["+getAppName(getCallingPid())+"]["+source+"] :"+message);
		}

        @Override
        public boolean isConnected() throws RemoteException {
            if (mConnection != null) return mConnection.isConnected();
            return false;
        }

        @Override
        public void publish(String wampEvent, String jsonParams) throws RemoteException {
            assert mConnection != null;
            mConnection.publish(wampEvent, jsonParams);
        }

        @Override
        public void subscribe(String wampEvent, IDomotrixServiceListener listener) throws RemoteException {
            assert mConnection != null;

            String appName = getAppName(getCallingPid());
            // TODO: check authorization

            if (!remote_hashmap.containsKey(wampEvent)) {
                remote_hashmap.put(wampEvent, new RemoteCallbackList<IDomotrixServiceListener>());
            }
            RemoteCallbackList<IDomotrixServiceListener> remote_listeners = remote_hashmap.get(wampEvent);
            if (remote_listeners != null) remote_listeners.register(listener);

            mConnection.subscribe(wampEvent, dispatcherListener);
        }

        @Override
        public void unsubscribe(String wampEvent, IDomotrixServiceListener listener) throws RemoteException {
            assert mConnection != null;

            String appName = getAppName(getCallingPid());
            // TODO: check authorization

            RemoteCallbackList<IDomotrixServiceListener> remote_listeners = remote_hashmap.get(wampEvent);
            if (remote_listeners != null) remote_listeners.unregister(listener);

            //mConnection.unsubscribe(wampEvent, dispatcherListener);
        }

		@Override
		public String getVersion() {
			try {
				String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                Log.d(TAG,"SDK Version "+versionName);
				return versionName;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
            return "";
		}

        private String getAppName(int pID) {
            String processName = "";
            ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            List l = am.getRunningAppProcesses();
            Iterator i = l.iterator();
            PackageManager pm = getPackageManager();
            while(i.hasNext())
            {
                ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
                try
                {
                    if(info.pid == pID)
                    {
                        CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                        processName = info.processName;
                    }
                } catch(Exception e) {
                }
            }
            return processName;
        }
	};

	///////////////////////////////////////////////////////////////////////
	// Service implementation 
	///////////////////////////////////////////////////////////////////////
	
	@Override
	public IBinder onBind(Intent intent) {
        if (IDomotrixService.class.getName().equals(intent.getAction())) {
			return apiEndpoint;
		} else {
			return null;
		}
	}

	@Override
	public void onCreate() {
        super.onCreate();

        // Wamp Client Connection
        mConnection = new Connection(DomotrixService.this);

        // Start Searching Network Task
        //DiscoverNetworkTask task = new DiscoverNetworkTask(DomotrixService.this);
        //task.execute();
    }

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
		Log.d(TAG, "onStartCommand SERVICE CALLED");
        return START_STICKY;
    }	

	@Override
	public void onDestroy() {
		Log.d(TAG,"DESTROY SERVICE");
		super.onDestroy();
	}

    class DiscoverNetworkTask extends RepeatableAsyncTask<Void, Void, Object> {
        Context mContext;

        public DiscoverNetworkTask (Context context){
            mContext = context;
        }

        @Override
        protected Object repeatInBackground(Void... params) {
            final boolean[] isFound = {false};
            NetworkDiscovery discovery = new NetworkDiscovery(DomotrixService.this);
            discovery.findServers(new NetworkDiscovery.OnFoundListener() {
                @Override
                public void onServiceAdded(ServiceInfo info) {
                    isFound[0] = true;
                    Intent i = new Intent("com.domotrix.android.DOMOTRIX_FOUND");
                    sendBroadcast(i);
                    String[] addresses = info.getHostAddresses();
                    mConnection.start(addresses[0], Connection.DOMOTRIX_DEFAULT_PORT, Connection.DOMOTRIX_DEFAULT_REALM);
                }
                @Override
                public void onServiceRemoved(ServiceInfo info) {
                    isFound[0] = false;
                    Log.d(TAG,"SEND BROADCAST com.domotrix.android.DOMOTRIX_NOTFOUND");
                    Intent i = new Intent("com.domotrix.android.DOMOTRIX_NOTFOUND");
                    sendBroadcast(i);
                }
            });
            if (isFound[0] == true) {
                return isFound[0];
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object v, Exception e) {
            super.onPostExecute(v);
        }
    }

}