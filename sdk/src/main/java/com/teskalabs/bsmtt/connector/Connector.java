package com.teskalabs.bsmtt.connector;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Handles sending JSON data to a server.
 * @author Premysl Cerny
 */
public class Connector {
	private Deque<JSONObject> mQueue;
	private String mUrl;
	private boolean isSending;
	private Sender mSender;

	public Connector(String url) {
		mUrl = url;
		mQueue = new ArrayDeque<>();
		isSending = false;
		mSender = null;
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
	 */
	private void run() {
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

	public String getUrl() {
		return mUrl;
	}

	public void refreshSending() {
		isSending = false;
		this.run();
	}
}
