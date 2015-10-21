package com.domotrix.android.sensors; 

public class RecognitionData extends SensorData {
	int command = 0;
	int controller = 0;
	String location = "";
	String mode = "";
	String show = ""; 
	long duration = 0;
	int quantity = 0;

	public RecognitionData(int command, int controller, String location, String mode, String show, long duration, int quantity) { 
		this.command = command;
		this.controller = controller;
		this.location = location;
		this.mode = mode;
		this.show = show;
		this.duration = duration;
		this.quantity = quantity;
		this.sender = "com.domotrix.recognitiondata";
	}

	public int getCommand() {return this.command; }
	public int getController() {return this.controller; }
	public String getLocation() {return this.location; }
	public String getMode() {return this.mode; }
	public String getShow() {return this.show; }
	public long getDuration() {return this.duration; }
	public int getQuantity() {return this.quantity; }
}