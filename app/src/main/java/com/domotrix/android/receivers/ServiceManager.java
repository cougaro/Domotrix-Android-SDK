package com.domotrix.android.receivers;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class ServiceManager extends ContextWrapper {
  private final String TAG = "ServiceManager";

  public ServiceManager(Context base) {
    super(base);
  }

  public boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      return true;
    }
    return false;
  }

  public String getNetworkName() {
    WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
    if (wifiInfo != null) {
      String name = wifiInfo.getSSID();
      return name;
    } else {
      return "";
    }
  }

  public boolean networkNameEquals(String ssid) {
    WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
    if (wifiInfo != null) {
      String name = wifiInfo.getSSID();
      if (name.equalsIgnoreCase(ssid)) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

}
