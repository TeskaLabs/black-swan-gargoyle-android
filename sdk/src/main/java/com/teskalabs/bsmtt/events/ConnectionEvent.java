package com.teskalabs.bsmtt.events;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An event that reacts to internet connection changes.
 * @author Premysl Cerny
 */
public class ConnectionEvent extends JsonEvent {
	private boolean haveMobileConnection;
	private int dconn;
	private int mRoaming; // added by Premysl

	/**
	 * A basic constructor.
	 */
	public ConnectionEvent() {
		super(BSMTTEvents.CONNECTION_EVENT);
		haveMobileConnection = false;
		dconn = Integer.MIN_VALUE;
		mRoaming = Integer.MIN_VALUE;
	}

	/**
	 * Reacts to changes of the internet network of the event.
	 * @param hMobileConnection boolean
	 * @param dConn int
	 * @param roaming int
	 */
	public void changeNetwork(boolean hMobileConnection, int dConn, int roaming) {
		// Check
		if (haveMobileConnection == hMobileConnection && dconn == dConn && mRoaming == roaming)
			return;
		// Save
		haveMobileConnection = hMobileConnection;
		dconn = dConn;
		mRoaming = roaming;
		JSONObject data = getEventData();
		try {
			data.put("have_mobile_conn", haveMobileConnection ? 1 : 0);
			data.put("dconn", dconn);
			data.put("roaming", mRoaming);
			saveEventData(data); // save
			dataReceived(); // notify
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
