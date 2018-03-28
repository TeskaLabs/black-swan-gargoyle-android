package com.teskalabs.bsmtt.events;

import com.teskalabs.bsmtt.BSMTTelemetryHelper;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Holds event identifiers and a few helper methods.
 * @author Premysl Cerny
 */
public class BSMTTEvents {
	public static final int BASIC_EVENT = 0;
	public static final int CONNECTION_EVENT = 1;
	public static final int PHONE_EVENT = 2;
	public static final int CELL_EVENT = 3;

	/**
	 * Transform integer values into strings.
	 * @param JSON
	 * @return
	 */
	public static JSONObject lookupFormatter(JSONObject JSON) {
		try {
			Iterator<?> keys = JSON.keys();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				switch (key) {
					case "call_state":
						JSON.put(key, String.valueOf(JSON.getInt(key)) + " (" + BSMTTelemetryHelper.getCallState(JSON.getInt(key)) + ")");
						break;
					case "data_state":
						JSON.put(key, String.valueOf(JSON.getInt(key)) + " (" + BSMTTelemetryHelper.getDataState(JSON.getInt(key)) + ")");
						break;
					case "dconn":
						JSON.put(key, String.valueOf(JSON.getInt(key)) + " (" + BSMTTelemetryHelper.getDataState(JSON.getInt(key)) + ")");
						break;
					case "data_network_type":
						JSON.put(key, String.valueOf(JSON.getInt(key)) + " (" + BSMTTelemetryHelper.getNetworkType(JSON.getInt(key)) + ")");
						break;
					case "roaming":
						JSON.put(key, String.valueOf(JSON.getInt(key)) + " (" + BSMTTelemetryHelper.getRoaming(JSON.getInt(key)) + ")");
						break;
					case "event_type":
						JSON.put(key, String.valueOf(JSON.getInt(key)) + " (" + getEventTypeLabel(JSON.getInt(key)) + ")");
					default:
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSON;
	}

	/**
	 * Returns an event's label.
	 * @param eventType int
	 */
	public static String getEventTypeLabel(int eventType) {
		switch (eventType) {
			case BASIC_EVENT:
				return "Basic event";
			case CONNECTION_EVENT:
				return "Connection event";
			case PHONE_EVENT:
				return "Phone event";
			case CELL_EVENT:
				return "Cell event";
			default:
				return "Unknown";
		}
	}
}
