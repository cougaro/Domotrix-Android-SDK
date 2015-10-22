// IRemoteService.aidl
package com.domotrix.android.services;

interface IDomotrixService {
	String getVersion();
	void remoteLog(String source, String message);
}