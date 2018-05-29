package com.teskalabs.blackswan.gargoyle;

/**
 * A listener to receive notifications about the service's state changes.
 * @author Premysl Cerny
 */
public interface BSServiceMonitorListener {

	/**
	 * Receives information about a service's state.
	 * @param state int
	 * @return boolean
	 */
	void onReceiveServiceState(int state);
}
