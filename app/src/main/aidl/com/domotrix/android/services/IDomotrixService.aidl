// IRemoteService.aidl
package com.domotrix.android.services;

interface IDomotrixService {
	String getVersion();
	boolean isConnected();
	void publish(String wampEvent, String jsonParams);
	void remoteLog(String source, String message);
}