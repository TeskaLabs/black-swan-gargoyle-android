package com.teskalabs.blackswan.gargoyle.messaging;

/**
 * Contains definitions of messages.
 * @author Premysl Cerny
 */
public class BSGargoyleMessage {
	// Requests
	public static final int MSG_ADD_ACTIVITY = 1;
	public static final int MSG_GET_EVENT_LIST = 2;
	public static final int MSG_GET_CLIENT_TAG = 3;
	public static final int MSG_RESET_IDENTITY = 4;
	// Events
	public static final int MSG_CONNECTED = 5;
	public static final int MSG_JSON_EVENT = 6;
	public static final int MSG_CLIENT_TAG = 7;
}
