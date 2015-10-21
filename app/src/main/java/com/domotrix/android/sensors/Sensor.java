package com.domotrix.android.sensors;

import java.util.ArrayList;

public class Sensor {
	ArrayList<String> ids = new ArrayList<String>();
	String uri;
	SensorData data;

	public Sensor() {}
	public Sensor(String uri, SensorData data) { this.uri = uri; this.data = data; }

	public String getURI() { return this.uri; }
	public void setURI(String uri) { this.uri = uri; }

	public void addID(String id) { ids.add(id); }
	public ArrayList<String> getIDs() { return this.ids; }

	public SensorData getData() { return this.data; }
	public void setData(SensorData data) { this.data = data; }
}
