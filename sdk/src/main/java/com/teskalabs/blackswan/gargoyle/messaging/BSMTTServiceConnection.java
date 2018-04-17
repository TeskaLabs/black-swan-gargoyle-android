package com.teskalabs.blackswan.gargoyle.messaging;

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
	private Messenger mServiceMessenger;

	/**
	 * A basic constructor.
	 * @param receiveMessenger Messenger
	 */
	public BSMTTServiceConnection(Messenger receiveMessenger) {
		mReceiveMessenger = receiveMessenger;
		mServiceMessenger = null;
	}

	/**
	 * Requests current data (events).
	 * @return boolean
	 */
	public boolean requestCurrentData() {
		if (mServiceMessenger == null)
			return false;
		try {
			Message msg = Message.obtain(null, BSMTTMessage.MSG_GET_EVENT_LIST);
			msg.replyTo = mReceiveMessenger;
			mServiceMessenger.send(msg);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Requests the connector's client tag.
	 * @return boolean
	 */
	public boolean requestClientTag() {
		if (mServiceMessenger == null)
			return false;
		try {
			Message msg = Message.obtain(null, BSMTTMessage.MSG_GET_CLIENT_TAG);
			msg.replyTo = mReceiveMessenger;
			mServiceMessenger.send(msg);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Requests reset of the connector's identity.
	 * There is no reply to this message.
	 * @return boolean
	 */
	public boolean requestResetIdentity() {
		if (mServiceMessenger == null)
			return false;
		try {
			Message msg = Message.obtain(null, BSMTTMessage.MSG_RESET_IDENTITY);
			mServiceMessenger.send(msg);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Notifies the service to register a listener (activity) to send messages to.
	 * @param className ComponentName
	 * @param service IBinder
	 */
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		try {
			// Notify the service
			mServiceMessenger = new Messenger(service);
			Message msg = Message.obtain(null, BSMTTMessage.MSG_ADD_ACTIVITY);
			msg.replyTo = mReceiveMessenger;
			mServiceMessenger.send(msg);
			// Notify the app
			mReceiveMessenger.send(Message.obtain(null, BSMTTMessage.MSG_CONNECTED));
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
