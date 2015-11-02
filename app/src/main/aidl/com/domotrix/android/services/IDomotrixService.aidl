// IRemoteService.aidl
package com.domotrix.android.services;

import com.domotrix.android.services.IDomotrixServiceListener;

interface IDomotrixService {
	String getVersion();
	boolean isConnected();
	void publish(String wampEvent, String jsonParams);
	void addListener(String wampEvent, IDomotrixServiceListener listener);
	void removeListener(String wampEvent);
	void remoteLog(String source, String message);
}