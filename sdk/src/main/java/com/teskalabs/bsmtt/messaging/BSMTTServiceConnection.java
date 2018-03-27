package com.teskalabs.bsmtt.messaging;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * A connector between an activity and the service to register the activity as a client.
 * @author Premysl Cerny
 */
public class BSMTTServiceConnection implements ServiceConnection {
	private Messenger mReceiveMessenger;

	/**
	 * A basic constructor.
	 * @param receiveMessenger Messenger
	 */
	public BSMTTServiceConnection(Messenger receiveMessenger) {
		mReceiveMessenger = receiveMessenger;
	}

	/**
	 * Notifies the service to register a listener (activity) to send messages to.
	 * @param className ComponentName
	 * @param binder IBinder
	 */
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		BSMTTelemetryServiceBinder bsmttBinder = (BSMTTelemetryServiceBinder)binder;
		Messenger messenger = bsmttBinder.getMessenger();
		try {
			Message msg = Message.obtain(null, BSMTTMessage.MSG_ADD_ACTIVITY);
			msg.replyTo = mReceiveMessenger;
			messenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reacts to disconnection of the service (empty now).
	 * @param arg0 ComponentName
	 */
	@Override
	public void onServiceDisconnected(ComponentName arg0) { }
}
