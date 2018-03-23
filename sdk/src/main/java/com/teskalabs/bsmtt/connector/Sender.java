package com.teskalabs.bsmtt.connector;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Sends data to a server.
 * @author Premysl Cerny
 */
public class Sender extends AsyncTask<JSONObject, String, Boolean> {
	public static final String LOG_TAG = "BSInfoSDK - Sender";

	private Connector mConnector;

	public Sender(Connector connector) {
		mConnector = connector;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mConnector.refreshSending();
	}

	@Override
	protected Boolean doInBackground(JSONObject... params) {
		try {
			JSONObject JSON = params[0];
			if (JSON == null)
				return false;

			// Preparing the connection
			URL url = new URL(mConnector.getUrl());
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
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
			return false;
		}

		return true;
	}
}
