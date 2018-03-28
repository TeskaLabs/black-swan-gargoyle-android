package com.teskalabs.bsmtt.connector;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.teskalabs.seacat.android.client.SeaCatClient;

/**
 * Sends data to a server.
 * @author Premysl Cerny
 */
public class Sender extends AsyncTask<JSONObject, String, Boolean> {
	public static final String LOG_TAG = "BSMTTelemetrySender";

	private Connector mConnector;

	/**
	 * The basic constructor that receives the Connector.
	 * @param connector Connector
	 */
	public Sender(Connector connector) {
		mConnector = connector;
	}

	/**
	 * Actions before the execution of the async task.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	/**
	 * Refreshes sending after the execution of the async task.
	 * @param result Boolean
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		mConnector.refreshSending();
	}

	/**
	 * Performs sending of the JSON data.
	 * @param params JSONObject...
	 * @return Boolean
	 */
	@Override
	protected Boolean doInBackground(JSONObject... params) {
		JSONObject JSON = params[0];
		if (JSON == null)
			return false;

		try {
			// Preparing the connection
			URL url = new URL(mConnector.getUrl());
			// HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			HttpURLConnection conn = SeaCatClient.open(url);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			conn.setRequestProperty("Accept","application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			// Adding the output data
			DataOutputStream os = new DataOutputStream(conn.getOutputStream());
			os.writeBytes(JSON.toString());

			// Sending the data via opened stream
			os.flush();
			os.close();

			// Adding a log line
			Log.i(LOG_TAG, String.valueOf(conn.getResponseCode()) + ": " + conn.getResponseMessage());

			// Closing the connection
			conn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
			// save to the queue again
			mConnector.send(JSON);
			return false;
		}

		return true;
	}
}
