package com.teskalabs.bsmtt.messaging;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple client to receive messages and pass them to a listener.
 * @author Premysl Cerny
 */
public class BSMTTClientHandler extends Handler {
	private BSMTTListener mListener;
	private Context mContext;

	/**
	 * A simple constructor.
	 * @param listener BSMTTListener
	 */
	public BSMTTClientHandler(Context context, BSMTTListener listener) {
		mContext = context;
		mListener = listener;
	}

	/**
	 * Passes the message to the listener and checks its response.
	 * @param msg Message
	 */
	@Override
	public void handleMessage(Message msg) {
		// Checking that the message is from the same package
		String callingApp = mContext.getPackageManager().getNameForUid(msg.sendingUid);
		String myApp = mContext.getApplicationContext().getPackageName();
		if (callingApp == null || !callingApp.equals(myApp))
			return;
		// Handling special cases (JSON)
		Bundle data = msg.getData();
		switch (msg.what) {
			case BSMTTMessage.MSG_JSON_EVENT:
				try {
					msg.obj = new JSONObject(data.getString("JSON"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
		}
		// Sending the message to the registered listener
		if (!mListener.onReceiveMessage(msg))
			super.handleMessage(msg);
	}
}
