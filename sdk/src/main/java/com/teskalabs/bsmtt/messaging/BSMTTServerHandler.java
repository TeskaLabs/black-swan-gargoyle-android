package com.teskalabs.bsmtt.messaging;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import java.util.ArrayList;

/**
 * A simple server handler to keep clients to send messages to.
 * @author Premysl Cerny
 */
public class BSMTTServerHandler extends Handler {
	private ArrayList<Messenger> clients;

	/**
	 * A basic constructor.
	 */
	public BSMTTServerHandler() {
		clients = new ArrayList<>();
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
