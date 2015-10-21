package com.domotrix.android.sensors; 

public class OnOffData extends SensorData {
	boolean value = false;

	public OnOffData(boolean value) { this.value = value; }

	public boolean getValue() { return this.value; }
	public void setValue(boolean value) { this.value = value; }
}