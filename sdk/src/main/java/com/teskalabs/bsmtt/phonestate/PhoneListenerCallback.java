package com.teskalabs.bsmtt.phonestate;

/**
 * An interface that helps a class react to the phone state change events.
 * @author Premysl Cerny
 */
public interface PhoneListenerCallback {
	void onPhoneResponseChange(PhoneResponse phoneResponse);
}
