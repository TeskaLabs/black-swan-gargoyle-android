package com.teskalabs.blackswan.gargoyle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * A wakeful receiver that runs the BSGargoyleService while keeping the CPU on.
 * @author Premysl Cerny
 */
public class BSWakefulReceiver extends WakefulBroadcastReceiver {

	/**
	 * Starts the BSGargoyleService as a wakeful service (keep a WakeLock for it).
	 * @param context Context
	 * @param intent Intent
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, BSGargoyleService.class);
		startWakefulService(context, service);
	}
}
