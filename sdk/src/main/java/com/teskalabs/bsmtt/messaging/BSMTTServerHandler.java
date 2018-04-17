package com.teskalabs.bsmtt.messaging;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.json.JSONObject;
import java.util.ArrayList;

import com.teskalabs.bsmtt.BSMTTelemetryService;
import com.teskalabs.bsmtt.events.JsonEvent;

/**
 * A simple server handler to keep clients to send messages to.
 * @author Premysl Cerny
 */
public class BSMTTServerHandler extends Handler {
	private ArrayList<Messenger> clients;
	private BSMTTelemetryService mService;

	/**
	 * A basic constructor.
	 * @param service BSMTTelemetryService
	 */
	public BSMTTServerHandler(BSMTTelemetryService service) {
		clients = new ArrayList<>();
		mService = service;
	}

	/**
	 * Handles messages to register clients.
	 * @param msg Message
	 */
	@Override
	public void handleMessage(Message msg) {
		// Checking that the message is from the same package
		String callingApp = mService.getPackageManager().getNameForUid(msg.sendingUid);
		String myApp = mService.getApplicationContext().getPackageName();
		if (callingApp == null || !callingApp.equals(myApp))
			return;
		// Handling the message
		switch (msg.what) {
			case BSMTTMessage.MSG_ADD_ACTIVITY:
				clients.add(msg.replyTo);
				break;
			case BSMTTMessage.MSG_GET_EVENT_LIST:
				sendJSONArray(msg.replyTo, mService.getEvents());
				break;
			case BSMTTMessage.MSG_GET_CLIENT_TAG:
				sendClientTag(msg.replyTo, mService.getClientTag());
			default:
				super.handleMessage(msg);
				break;
		}
	}

	/**
	 * A method to send a JSON.
	 * @param JSON JSONObject
	 */
	public void sendJSON(JSONObject JSON) {
		for (int i = 0; i < clients.size(); i++) {
			try {
				Message msg = Message.obtain(null, BSMTTMessage.MSG_JSON_EVENT);
				// Adding the data to be sent with the message
				Bundle data = new Bundle();
				data.putString("JSON", JSON.toString());
				msg.setData(data);
				// Sending
				clients.get(i).send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
				// The client is not available anymore
				clients.remove(i);
			}
		}
	}

	/**
	 * A method to send a JSON array.
	 * @param receiver Messenger
	 * @param JSONs ArrayList<JSONObject>
	 */
	public void sendJSONArray(Messenger receiver, ArrayList<JsonEvent> JSONs) {
		try {
			for (int i = 0; i < JSONs.size(); i++) {
				Message msg = Message.obtain(null, BSMTTMessage.MSG_JSON_EVENT);
				// Adding the data to be sent with the message
				Bundle data = new Bundle();
				JSONObject json = JSONs.get(i).simpleGet();
				data.putString("JSON", json.toString());
				msg.setData(data);
				// Sending
				receiver.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A method to send the connector's client tag.
	 * @param receiver Messenger
	 */
	public void sendClientTag(Messenger receiver, String ClientTag) {
		try {
			Message msg = Message.obtain(null, BSMTTMessage.MSG_CLIENT_TAG);
			// Adding the data to be sent with the message
			Bundle data = new Bundle();
			data.putString("ClientTag", ClientTag);
			msg.setData(data);
			// Sending
			receiver.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A method to send the connector's client tag.
	 * @param ClientTag String
	 */
	public void sendClientTag(String ClientTag) {
		for (int i = 0; i < clients.size(); i++) {
			sendClientTag(clients.get(i), ClientTag);
		}
	}
}
