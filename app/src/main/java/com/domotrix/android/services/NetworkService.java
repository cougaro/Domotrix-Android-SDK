package com.domotrix.android.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.domotrix.android.NetworkDiscovery;
import com.domotrix.android.utils.RepeatableAsyncTask;

import java.util.List;

import javax.jmdns.ServiceInfo;

/**
 * Created by cougaro on 10/11/15.
 */
public class NetworkService extends Service {

    private final static String TAG = "NetworkService";
    private final static int REPEAT_TIME = 5;

    private IDomotrixService mService = null;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IDomotrixService.Stub.asInterface(service);
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    protected void connectToRemoteService(Context ctx) {
        if (!mIsBound) {
            Intent serviceIntent = new Intent(IDomotrixService.class.getName());
            boolean bindResult = bindService(createExplicitFromImplicitIntent(getApplicationContext(), serviceIntent), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = bindResult;
        }
    }

    private Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    protected void disconnectFromRemoteService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void handleStart() {
        Log.d(TAG,"*********************************");
        Log.d(TAG,"START NETWORK SERVICE");
        Log.d(TAG, "*********************************");

        // Start Searching Network Task
        DiscoverNetworkTask task = new DiscoverNetworkTask(NetworkService.this);
        task.execute();
        connectToRemoteService(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handleStart();
        return  Service.START_NOT_STICKY;

        /*
        if(startId == Service.START_STICKY) {
            handleStart();
            return super.onStartCommand(intent, flags, startId);
        }else{
            Log.d(TAG,"*********************************");
            Log.d(TAG,"SORRY NOT STICKY...");
            Log.d(TAG, "*********************************");
            return  Service.START_NOT_STICKY;
        }
        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectFromRemoteService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class DiscoverNetworkTask extends RepeatableAsyncTask<Void, Void, Object> {
        Context mContext;

        public DiscoverNetworkTask(Context context) {
            super(REPEAT_TIME);
            mContext = context;
        }

        @Override
        protected Object repeatInBackground(Void... params) {
            final int[] isFound = {0};

            NetworkDiscovery discovery = new NetworkDiscovery(NetworkService.this);
            discovery.findServers(new NetworkDiscovery.OnFoundListener() {
                @Override
                public void onServiceAdded(ServiceInfo info) {
                    isFound[0] = 1;
                    String[] addresses = info.getHostAddresses();
                    if (mService != null) {
                        try {
                            mService.remoteLog(TAG,"****************************************");
                            mService.remoteLog(TAG,"****************************************");
                            mService.remoteLog(TAG,"****************************************");
                            mService.remoteLog(TAG,"DOMOTRIX IP FOUND AT:"+addresses[0]);
                            mService.remoteLog(TAG,"****************************************");
                            mService.remoteLog(TAG,"****************************************");
                            mService.remoteLog(TAG,"****************************************");
                            mService.registerDomotrixIP(addresses[0], 0);
                        } catch (RemoteException e) {
                        }
                    }
                }

                @Override
                public void onServiceRemoved(ServiceInfo info) { isFound[0] = 2; }
            });

            if (isFound[0] == 0) {
                return null;
            }

            return isFound[0];
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
            /*
            if (v == null) {
                Intent intent = new Intent(getApplicationContext(), NetworkService.class);
                PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);
                // Build notification
                // Actions are just fake
                Notification noti = new Notification.Builder(mContext)
                        .setContentTitle("Not Found")
                        .setContentText("Subject").setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pIntent)
                        .build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                noti.flags |= Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(0, noti);
            } else {
                Intent intent = new Intent(getApplicationContext(), NetworkService.class);
                PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);
                // Build notification
                // Actions are just fake
                Notification noti = new Notification.Builder(mContext)
                        .setContentTitle("Found")
                        .setContentText("connected into Domotrix Realm").setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pIntent)
                        .build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                noti.flags |= Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(0, noti);
            }
            */
        }
    }

}
