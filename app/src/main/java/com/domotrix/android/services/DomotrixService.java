package com.domotrix.android.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.domotrix.android.Connection;
import com.domotrix.android.JSONMapper;
import com.domotrix.android.listeners.SubscriptionListener;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DomotrixService extends Service {
    public final static String TAG = "DomotrixService";
    private Connection mConnection;
    private Pubnub pubnub;

    private HashMap<String, RemoteCallbackList<IDomotrixServiceListener>> remote_hashmap = new HashMap<String, RemoteCallbackList<IDomotrixServiceListener>>();
    private SubscriptionListener dispatcherListener = new SubscriptionListener() {
        @Override
        public void onMessage(String wampEvent, String jsonMessage) {
            Log.d(TAG, "=============== WAMP MESSAGE RECEIVED");
            Log.d(TAG, wampEvent);
            Log.d(TAG, "=====================================");
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
            Log.d(TAG, "================= WAMP FAULT RECEIVED");
            Log.d(TAG, message);
            Log.d(TAG, "=====================================");
        }
    };

    ///////////////////////////////////////////////////////////////////////
    // IDomotrixService AIDL implementation
    ///////////////////////////////////////////////////////////////////////

    private IDomotrixService.Stub apiEndpoint = new IDomotrixService.Stub() {

        @Override
        public void remoteLog(String source, String message) throws RemoteException {
            /*
            if (!getAppName(getCallingPid()).equals("com.domotrix.domotrixdemo") ||
                    !getAppName(getCallingPid()).equals(getApplicationContext().getPackageName())) {
                throw new RemoteException("Unauthorized app");
            }
            */
            Log.d(TAG, "[" + getAppName(getCallingPid()) + "][" + source + "] :" + message);
        }

        @Override
        public void registerDomotrixIP(String ip, int port) throws RemoteException {
            //if (!getAppName(getCallingPid()).equals(getApplicationContext().getPackageName())) {
            //    throw new RemoteException("Unauthorized app");
            //}
            if (mConnection == null) {
                throw new RemoteException("No Connection");
            }
            if (mConnection.isConnected()) {
                mConnection.stop();
            }
            mConnection.start(ip,Connection.DOMOTRIX_DEFAULT_PORT, Connection.DOMOTRIX_DEFAULT_REALM);
        }

        @Override
        public boolean isConnected() throws RemoteException {
            if (mConnection != null) return mConnection.isConnected();
            return false;
        }

        @Override
        public void publish(String wampEvent, String jsonParams) throws RemoteException {
            assert mConnection != null;
            Log.d(TAG, "[" + getAppName(getCallingPid()) + "][PUBLISH]: " + wampEvent + ":" + jsonParams);
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
                Log.d(TAG, "SDK Version " + versionName);
                return versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return "";
        }

        private String getAppName(int pID) {
            String processName = "";
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List l = am.getRunningAppProcesses();
            Iterator i = l.iterator();
            PackageManager pm = getPackageManager();
            while (i.hasNext()) {
                ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
                try {
                    if (info.pid == pID) {
                        CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                        processName = info.processName;
                    }
                } catch (Exception e) {
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

        Log.d(TAG,"ONCREATE SERVICE");

        // Wamp Client Connection
        mConnection = new Connection(DomotrixService.this);

        // Start PubNub
        pubnub = new Pubnub("pub-c-51297165-eee3-4138-bcc9-ba56b34889c5", "sub-c-79773956-83a1-11e5-9e96-02ee2ddab7fe");
        try {
            pubnub.subscribe("domotrix", new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {
                            Log.d(TAG,"PUBNUB CONNECTED....");
                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            Log.d(TAG,"SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            Log.d(TAG,"SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            Log.d(TAG,"SUBSCRIBE : " + channel + " : "
                                    + message.getClass() + " : " + message.toString());
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            Log.d(TAG,"SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        } catch (PubnubException e) {
            System.out.println(e.toString());
        }

        // Start NetworkService
        Intent serviceIntent = new Intent(getApplicationContext(), NetworkService.class);
        startService(serviceIntent);
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
        Log.d(TAG, "DESTROY SERVICE");
        super.onDestroy();
    }
}