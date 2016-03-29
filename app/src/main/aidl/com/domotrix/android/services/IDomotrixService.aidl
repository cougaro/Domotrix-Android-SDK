// IRemoteService.aidl
package com.domotrix.android.services;

import com.domotrix.android.services.IDomotrixServiceListener;

interface IDomotrixService {
	String getVersion();
	boolean isConnected();
	void publish(String wampEvent, String jsonParams);
	void subscribe(String wampEvent, IDomotrixServiceListener listener);
	void unsubscribe(String wampEvent, IDomotrixServiceListener listener);
	void remoteLog(String source, String message);
	void speech(String message);

	void registerDomotrixIP(String ip, int port); // reserved
}