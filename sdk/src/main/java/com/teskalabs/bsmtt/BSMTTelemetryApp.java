package com.teskalabs.bsmtt;

import android.app.Application;
import java.io.IOException;

import com.teskalabs.seacat.android.client.SeaCatClient;

/**
 * Basic application context to do some initializations.
 * @author Premysl Cerny
 */
public class BSMTTelemetryApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		try {
			SeaCatClient.setLogMask(SeaCatClient.LogFlag.ALL_SET);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Enable SeaCat
		SeaCatClient.initialize(getApplicationContext());
	}
}
