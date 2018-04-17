package com.teskalabs.blackswan.gargoyle.messaging;

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
public class BSGargoyleClientHandler extends Handler {
	private BSGargoyleListener mListener;
	private Context mContext;

	/**
	 * A simple constructor.
	 * @param listener BSGargoyleListener
	 */
	public BSGargoyleClientHandler(Context context, BSGargoyleListener listener) {
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
			case BSGargoyleMessage.MSG_JSON_EVENT:
				try {
					msg.obj = new JSONObject(data.getString("JSON"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case BSGargoyleMessage.MSG_CLIENT_TAG:
				msg.obj = data.getString("ClientTag");
				break;
		}
		// Sending the message to the registered listener
		if (!mListener.onReceiveMessage(msg))
			super.handleMessage(msg);
	}
}
