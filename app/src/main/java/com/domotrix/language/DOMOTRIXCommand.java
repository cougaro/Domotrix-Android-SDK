package com.domotrix.language;

public class DOMOTRIXCommand {

  public final static int NONE = 0;
  public final static int LIGHTS = 1;
  public final static int AIRCONDITIONER = 2;
  public final static int SMART_TV = 3;
  public final static int LAUNDRY_MACHINE = 4;
  public final static int ROOMBA = 5;
  public final static int POWER_SOCKET = 6;

  public final static int ON = 1;
  public final static int OFF = 2;
  public final static int CHANGE = 3;
  public final static int MODE = 4;
  public final static int SHOW = 5;
  public final static int CANCEL = 6;

  public final static int ONLY = 1;
  public final static int ALL = 2;

  private int controller = NONE;
  private int command = NONE;
  private int quantity = NONE;
  private String speech = "";
  private String location = "";
  private String mode = "";
  private String show = "";

  private long startAtMillis = 0;

  public DOMOTRIXCommand() {}

  public void setCommand(int command) { this.command = command; }
  public int getCommand() { return this.command; }

  public void setController(int controller) { this.controller = controller; }
  public int getController() { return this.controller; }

  public void setLocation(String location) { this.location = location; }
  public String getLocation() { return this.location; }

  public void setMode(String mode) { this.mode = mode; }
  public String getMode() { return this.mode; }

  public void setShow(String show) { this.show = show; }
  public String getShow() { return this.show; }

  public void setStart(long millis) { this.startAtMillis = millis; }
  public long getStart() { return this.startAtMillis; }

  public void setQuantity(int quantity) { this.quantity = quantity; }
  public int getQuantity() { return this.quantity; }

  //public void setSpeech(String speech) { this.speech = speech; }
  //public String getSpeech() { return speech; }
}
