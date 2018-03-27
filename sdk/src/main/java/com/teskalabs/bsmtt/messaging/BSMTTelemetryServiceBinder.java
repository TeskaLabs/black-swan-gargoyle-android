package com.teskalabs.bsmtt.messaging;

import android.os.Binder;
import android.os.Messenger;

/**
 * A binder to the service to get its messenger;
 * @author Premysl Cerny
 */
public class BSMTTelemetryServiceBinder extends Binder {
	private Messenger mMessenger;

	/**
	 * A simple constructor.
	 * @param messenger Messenger
	 */
	public BSMTTelemetryServiceBinder(Messenger messenger) {
		mMessenger = messenger;
	}

	/**
	 * Returns the messenger.
	 * @return Messenger
	 */
	public Messenger getMessenger() {
		return mMessenger;
	}
}