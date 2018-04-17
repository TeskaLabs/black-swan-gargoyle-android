package com.teskalabs.blackswan.gargoyle.phonestate;

/**
 * An interface that helps a class react to the phone state change events.
 * @author Premysl Cerny
 */
public interface PhoneListenerCallback {
	/**
	 * Receives the phone response with appropriate information.
	 * @param phoneResponse PhoneResponse
	 */
	void onPhoneResponseChange(PhoneResponse phoneResponse);
}
