package com.domotrix.android.listeners;

public interface SubscriptionListener {
  public void onMessage(String wampEvent, String jsonMessage);
  public void onFault(String message);
}
