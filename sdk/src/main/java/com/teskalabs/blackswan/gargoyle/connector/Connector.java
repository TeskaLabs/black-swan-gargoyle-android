package com.teskalabs.blackswan.gargoyle.connector;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONObject;
import java.util.ArrayDeque;
import java.util.Deque;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

/**
 * Handles sending JSON data to a server.
 * @author Premysl Cerny
 */
public class Connector implements InternetConnectivityListener {
	private Deque<JSONObject> mQueue;
	private String mUrl;
	private boolean isSending;
	private boolean isReady;
	private Sender mSender;
	private boolean mConnected;

	/**
	 * Constructs the Connector with a specified url.
	 * @param context Context
	 * @param url String
	 */
	public Connector(Context context, String url) {
		mUrl = url;
		mQueue = new ArrayDeque<>(250);
		isSending = false;
		isReady = false;
		mConnected = false;
		mSender = null;
		// Connection
		InternetAvailabilityChecker.init(context);
		InternetAvailabilityChecker iChecker = InternetAvailabilityChecker.getInstance();
		iChecker.addInternetConnectivityListener(this);
	}

	/**
	 * Destructs the object.
	 */
	public void delete() {
		// Removes the connection listener
		InternetAvailabilityChecker iChecker = InternetAvailabilityChecker.getInstance();
		iChecker.removeInternetConnectivityChangeListener(this);
		// Removes the Sender if there is one
		if (mSender != null) {
			mSender.cancel(true);
			mSender = null;
		}
	}

	/**
	 * Notifies the connector that the sending may begin.
	 */
	public void setReady() {
		isReady = true;
		run();
	}

	/**
	 * Notifies the connector that the sending is not ready.
	 */
	public void unsetReady() {
		isReady = false;
	}

	/**
	 * Adds an item to the queue to be sent.
	 * @param JSON JSONObject
	 */
	public void send(JSONObject JSON) {
		mQueue.add(JSON);
		run();
	}

	/**
	 * Gets an item from the queue and sends it if possible.
	 * The data are sent only when there is a connection.
	 */
	private void run() {
		if (!isReady)
			return;
		if (mConnected) {
			if (!isSending) {
				JSONObject JSON = mQueue.pollFirst();
				if (JSON != null) {
					isSending = true;
					// a new instance
					mSender = new Sender(this);
					mSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, JSON);
				}
			}
		}
	}

	/**
	 * The connector is notified when the connection state changes.
	 * @param isConnected boolean
	 */
	@Override
	public void onInternetConnectivityChanged(boolean isConnected) {
		mConnected = isConnected;
		if (isConnected) {
			run();
		}
	}

	/**
	 * Returns the URL the Connector is constructed with.
	 * @return String
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * Refreshes sending after a successful one.
	 */
	public void refreshSending() {
		isSending = false;
		this.run();
	}
}
