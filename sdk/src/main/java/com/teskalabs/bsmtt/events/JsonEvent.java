package com.teskalabs.bsmtt.events;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * An abstract class to provide methods for a BSMTT JSON event.
 * @author Premysl Cerny
 */
public abstract class JsonEvent {
	private boolean mReady;
	private boolean mDimensionsChanged;
	private JSONObject mJSONEvent;

	// Temporary variables
	private String mVendorModel;
	private String PhoneTypeStr;
	private String IMSI;
	private String IMEI;
	private String MSISDN; // added by Premysl
	private String iccid;
	private String MCC_MNC;
	private String net_name;
	private Location mLocation;

	/**
	 * Performs location changing at more events at once.
	 * @param events ArrayList<JsonEvent>
	 * @param location Location
	 */
	public static void changeLocationAtAll(ArrayList<JsonEvent> events, Location location) {
		for (int i = 0; i < events.size(); i++) {
			JsonEvent event = events.get(i);
			event.changeLocation(location);
		}
	}

	/**
	 * Performs phone info changing at more events at once.
	 * @param events ArrayList<JsonEvent>
	 * @param vendorModel String
	 * @param phoneType String
	 * @param imsi String
	 * @param imei String
	 * @param msisdn String
	 * @param iccID String
	 */
	public static void changePhoneInfoAtAll(ArrayList<JsonEvent> events, String vendorModel, String phoneType,
											String imsi, String imei, String msisdn, String iccID) {
		for (int i = 0; i < events.size(); i++) {
			JsonEvent event = events.get(i);
			event.changePhoneInfo(vendorModel, phoneType, imsi, imei, msisdn, iccID);
		}
	}

	/**
	 * Performs network info changing at more events at once.
	 * @param events ArrayList<JsonEvent>
	 * @param mccMnc String
	 * @param netName String
	 */
	public static void changePhoneNetworkAtAll(ArrayList<JsonEvent> events, String mccMnc, String netName) {
		for (int i = 0; i < events.size(); i++) {
			JsonEvent event = events.get(i);
			event.changePhoneNetwork(mccMnc, netName);
		}
	}

	/**
	 * A basic constructor.
	 */
	protected JsonEvent(int type) {
		mReady = false;
		mDimensionsChanged = false;
		mJSONEvent = new JSONObject();
		// Dimensions first
		try {
			// Gravitational field (or spacetime for losers)
			mJSONEvent.put("@timestamp", System.currentTimeMillis());
			mJSONEvent.put("L", new JSONObject());
			mJSONEvent.put("event_type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// Temporary variables
		mLocation = null;
		mVendorModel = "";
		PhoneTypeStr = "";
		IMSI = "";
		IMEI = "";
		MSISDN = "";
		iccid = "";
		MCC_MNC = "";
		net_name = "";
	}

	/**
	 * The event is received by the listener, if it is ready.
	 * @return JSONObject
	 */
	public JSONObject receiveEvent() {
		if (mReady) {
			// Refreshes the timestamp
			try {
				mJSONEvent.put("@timestamp", System.currentTimeMillis());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// Return
			mReady = false;
			return mJSONEvent;
		} else {
			return null;
		}
	}

	/**
	 * Checks if the event is ready.
	 * @return boolean
	 */
	public boolean isReady() {
		return mReady;
	}

	/**
	 * Forces the event to be ready.
	 * @return boolean
	 */
	public void forceReady() {
		mReady = true;
	}

	/**
	 * Returns true if dimensions changed.
	 * @return boolean
	 */
	public boolean ismDimensionsChanged() {
		return mDimensionsChanged;
	}

	/**
	 * Sets the value of dimensions changed.
	 * @param mDimensionsChanged boolean
	 */
	public void setmDimensionsChanged(boolean mDimensionsChanged) {
		this.mDimensionsChanged = mDimensionsChanged;
	}

	/**
	 * Gets the event data.
	 * @return JSONObject
	 */
	protected JSONObject getEventData() {
		return mJSONEvent;
	}

	/**
	 * Sets the event data.
	 * @param jsonEvent JSONObject
	 */
	protected void saveEventData(JSONObject jsonEvent) {
		mJSONEvent = jsonEvent;
	}

	/**
	 * Notifies that the event has changed.
	 */
	protected void dataReceived() {
		mReady = true;
	}

	// Dimensions ----------------------------------------------------------------------------------

	/**
	 * Changes the location of the event.
	 * @param location Location
	 */
	public void changeLocation(Location location) {
		// Check
		if (mLocation != null && location != null) {
			if (mLocation.getLatitude() == location.getLatitude() &&
					mLocation.getLongitude() == location.getLongitude())
				return;
		}
		// Save
		mLocation = location;
		try {
			if (mLocation != null) {
				JSONObject Lattr = new JSONObject();
				Lattr.put("lat", mLocation.getLatitude());
				Lattr.put("lon", mLocation.getLongitude());
				mJSONEvent.put("L", Lattr);
				mDimensionsChanged = true; // notify
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Changes information about the phone of the event.
	 * @param vendorModel String
	 * @param phoneType String
	 * @param imsi String
	 * @param imei String
	 * @param msisdn String
	 * @param iccID String
	 */
	public void changePhoneInfo(String vendorModel, String phoneType, String imsi,
								String imei, String msisdn, String iccID) {
		// Check
		if (mVendorModel.equals(vendorModel) && PhoneTypeStr.equals(phoneType) && IMSI.equals(imsi)
				&& IMEI.equals(imei) && MSISDN.equals(msisdn) && iccid.equals(iccID))
			return;
		// Save
		mVendorModel = vendorModel;
		PhoneTypeStr = phoneType;
		IMSI = imsi;
		IMEI = imei;
		MSISDN = msisdn;
		iccid = iccID;
		// Phone information
		try {
			mJSONEvent.put("vendor_model", mVendorModel);
			mJSONEvent.put("phone_type", PhoneTypeStr);
			mJSONEvent.put("IMSI", IMSI);
			mJSONEvent.put("IMEI", IMEI);
			if (!MSISDN.equals(""))
				mJSONEvent.put("MSISDN", MSISDN);
			mJSONEvent.put("iccid", iccid);
			mDimensionsChanged = true; // notify
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Changes information about the phone network of the event.
	 * @param mccMnc String
	 * @param netName String
	 */
	public void changePhoneNetwork(String mccMnc, String netName) {
		// Check
		if (MCC_MNC.equals(mccMnc) && net_name.equals(netName))
			return;
		// Save
		MCC_MNC = mccMnc;
		net_name = netName;
		// Phone network
		try {
			mJSONEvent.put("MCC_MNC", MCC_MNC);
			mJSONEvent.put("net_name", net_name);
			mDimensionsChanged = true; // notify
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the phone type as String.
	 * @return String
	 */
	public String getPhoneTypeStr() {
		return PhoneTypeStr;
	}
}
