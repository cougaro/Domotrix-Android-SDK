package com.domotrix.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.domotrix.android.listeners.SubscriptionListener;
import com.domotrix.android.sensors.Sensor;
import com.domotrix.android.services.ChatHeadService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;
import ws.wamp.jawampa.PubSubData;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

public class Connection {
	private final static String TAG = "Connection";
	public final static String DOMOTRIX_DEFAULT_HOST = "domotrix.local";
	public final static int DOMOTRIX_DEFAULT_PORT = 9000;
	public final static String DOMOTRIX_DEFAULT_REALM = "domotrix_realm";
	private final static int MIN_RECONNECT_INTERVAL = 5; // sec

	private final Context ctx;
	private WampClient client;
	private boolean isConnected = false;

    public Connection(Context ctx) {
        this.ctx = ctx;
    }

	public void start() {
		start(DOMOTRIX_DEFAULT_HOST, DOMOTRIX_DEFAULT_PORT, DOMOTRIX_DEFAULT_REALM);
	}

	public void start(String host, int port, String realm) {
		try {
			final String wsuri = "ws://"+host+":"+port;
			Log.d(TAG,"***********************************************");
			Log.d(TAG,"Trying to connect to " + wsuri);
			Log.d(TAG,"***********************************************");

			// Create a builder and configure the client
			WampClientBuilder builder = new WampClientBuilder();
			builder.withUri(wsuri)
			.withRealm(realm)
			.withInfiniteReconnects()
			.withReconnectInterval(MIN_RECONNECT_INTERVAL, TimeUnit.SECONDS);

			// Create a client through the builder. This will not immediately start
			// a connection attempt
			client = builder.build();

			// subscribe for session status changes
			client.statusChanged().subscribe(new Action1<WampClient.Status>() {
				@Override
				public void call(WampClient.Status t1) {
					if(t1 == WampClient.Status.Connected) {
						isConnected = true;
					} else if (t1 == WampClient.Status.Disconnected){
						isConnected = false;
					}
                    assert ctx != null;
                    Intent i = new Intent("com.domotrix.android."+(isConnected ? "DOMOTRIX_CONNECTED" : "DOMOTRIX_DISCONNECTED"));
                    ctx.sendBroadcast(i);
                }
			});

			// request to open the connection with the server
			client.open();

		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	public void startWithChatHead(Activity activity, String host, int port, String realm) {
		start(host, port, realm);
		activity.startService(new Intent(activity.getApplicationContext(), ChatHeadService.class));
	}

	public boolean isConnected() {
		return isConnected;
	}

    public void publish(String wampEvent, String jsonParams) {
        try {
            assert wampEvent != null;
            assert jsonParams != null;
            assert client != null;
			JsonNode try_to_encode = new ObjectMapper().readTree(jsonParams);
			// TODO: check the presence of SENDER
			client.publish(wampEvent, try_to_encode);
        } catch (Exception e) {
            Log.e(TAG, "publish Exception", e);
        }
    }

	public boolean publish(Sensor sensor) {
		try {
			String params = JSONMapper.encode(sensor.getData());
			Log.d(TAG,"PUBLISH URI "+sensor.getURI());
			Log.d(TAG,"PUBlISH MESSAGE "+params);
			client.publish(sensor.getURI(), params);
			return true;
		} catch (Exception e) {
			Log.e(TAG, "publish Exception", e);
		}
		return false;
	}

	public void subscribe(final String procedure, final SubscriptionListener listener) {
		if (isConnected()) {
			client.makeSubscription(procedure).subscribe(new Action1<PubSubData>() {
				@Override
				public void call(PubSubData arg0) {
					if(arg0 != null) {
						//Log.i(TAG, procedure + " call Json response: " + arg0.toString());
			//			Log.d(TAG, "___________");
			//			Log.d(TAG, ""+arg0.arguments());
			//			Log.d(TAG, "___________");
						//ObjectNode objectNode = arg0.keywordArguments();
						//String str = objectNode.toString();
						//Log.d(TAG, str);
						if (listener != null) listener.onMessage(procedure, arg0.arguments().toString());
					}
				}
			}, new Action1<Throwable>(){
				@Override
				public void call(Throwable arg0) {
					if(arg0 != null) {
						Log.i(TAG, procedure + " call Throwable response: " + arg0.toString());
						if (listener != null) listener.onFault(arg0.toString());
					}
				}
			});
		}
	}

	private void testSendMessage(String message) {
		try {
			client.publish("com.leapmotion.sensor", JSONMapper.encode(message));

			/*
			Observable<Reply> observable = client.call(procedure, arrayNode, node);
			observable.subscribe(new Action1<Reply>(){
			@Override
			public void call(Reply reply) {
			if(reply != null) {
			ArrayNode arguments = reply.arguments();
			String str = arguments.toString();
			try {
			JSONArray jsonArray = new JSONArray(str);
			int count = jsonArray.length();
			Log.i(TAG, "comments.get call Json response: " + str + ", comments count=" + count);
			}
			catch (JSONException e) {}
			}
			}
			}, new Action1<Throwable>(){
			@Override
			public void call(Throwable arg0) {
			if(arg0 != null) {
			Log.i(TAG, "comments.get call Throwable response: " + arg0.toString());
			}
			}

			});
			*/
		}
		catch (Exception e) {
			Log.e(TAG, "requestComments Exception");
		}
	}

	public void stop() {
		client.close();
	}

	/*
	void setCommentsAddListener()
	{
	// comments.[post_id].add
	final String procedure = "comments." + POST_ID + ".add";

	client.makeSubscription(procedure).subscribe(new Action1<PubSubData>(){
	@Override
	public void call(PubSubData arg0) {
	if(arg0 != null) {
	Log.i(LOG_WAMP, procedure + " call Json response: " + arg0.toString());
	ObjectNode objectNode = arg0.keywordArguments();
	String str = objectNode.toString();
	try {
	JSONObject jsonComment = new JSONObject(str);
	}
	catch (JSONException e) {}
	}
	}

	}, new Action1<Throwable>(){
	@Override
	public void call(Throwable arg0) {
	if(arg0 != null) {
	Log.i(LOG_WAMP, procedure + " call Throwable response: " + arg0.toString());
	}
	}

	});
	}
	*/
}
