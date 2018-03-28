package com.teskalabs.bsmtt.messaging;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.teskalabs.bsmtt.BSMTTelemetryService;

import java.util.ArrayList;

/**
 * A simple server handler to keep clients to send messages to.
 * @author Premysl Cerny
 */
public class BSMTTServerHandler extends Handler {
	private ArrayList<Messenger> clients;
	private BSMTTelemetryService mService;
	private boolean wasInitialized;

	/**
	 * A basic constructor.
	 */
	public BSMTTServerHandler(BSMTTelemetryService service) {
		clients = new ArrayList<>();
		mService = service;
		wasInitialized = false;
	}

	/**
	 * Handles messages to register clients.
	 * @param msg Message
	 */
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case BSMTTMessage.MSG_ADD_ACTIVITY:
				clients.add(msg.replyTo);
				// Initializing the service
				if (!wasInitialized) {
					mService.initialize();
					wasInitialized = true;
				}
				break;
			default:
				super.handleMessage(msg);
				break;
		}
	}

	/**
	 * A method to send a message with data.
	 * @param message int
	 * @param data Object
	 */
	public void sendMessage(int message, Object data) {
		for (int i = 0; i < clients.size(); i++) {
			try {
				clients.get(i).send(Message.obtain(null, message, data));
			} catch (RemoteException e) {
				e.printStackTrace();
				// The client is not available anymore
				clients.remove(i);
			}
		}
	}
}
