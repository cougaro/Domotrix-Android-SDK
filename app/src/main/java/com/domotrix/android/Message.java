package com.domotrix.android;

public class Message {
	public String type;
	public String message;

	String getType() {
		return type;
	}
	void setType(String type) {
		this.type = type;
	}
	String getMessage() {
		return message; 
	}
	void setMessage(String message) {
		this.message = message;
	}

	public String toString() {
		return getType()+","+getMessage();
	}
}