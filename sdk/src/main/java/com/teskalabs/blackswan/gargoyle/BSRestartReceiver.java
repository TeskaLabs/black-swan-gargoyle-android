package com.teskalabs.blackswan.gargoyle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Restarts the service when necessary.
 */
public class BSRestartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			BSGargoyleService.run(context);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}
