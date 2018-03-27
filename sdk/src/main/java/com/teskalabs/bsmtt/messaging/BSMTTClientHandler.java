package com.teskalabs.bsmtt.messaging;

import android.os.Handler;
import android.os.Message;

/**
 * A simple client to receive messages and pass them to a listener.
 * @author Premysl Cerny
 */
public class BSMTTClientHandler extends Handler {
	private BSMTTListener mListener;

	/**
	 * A simple constructor.
	 * @param listener BSMTTListener
	 */
	public BSMTTClientHandler(BSMTTListener listener) {
		mListener = listener;
	}

	/**
	 * Passes the message to the listener and checks its response.
	 * @param msg Message
	 */
	@Override
	public void handleMessage(Message msg) {
		if (!mListener.onReceiveMessage(msg))
			super.handleMessage(msg);
	}
}
