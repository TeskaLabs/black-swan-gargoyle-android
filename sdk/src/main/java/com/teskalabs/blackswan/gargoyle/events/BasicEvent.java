package com.teskalabs.blackswan.gargoyle.events;

import com.teskalabs.blackswan.gargoyle.BSGargoyleService;
import com.teskalabs.blackswan.gargoyle.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An event that reacts to certain dimension changes.
 * @author Premysl Cerny
 */
public class BasicEvent extends JsonEvent {
	private boolean ticked;
	private long mLastTime;
	private BSGargoyleService mService;

	/**
	 * A basic constructor.
	 * @param service BSGargoyleService
	 */
	public BasicEvent(BSGargoyleService service) {
		super(BSGargoyleEvents.BASIC_EVENT);
		ticked = false;
		mLastTime = 0;
		mService = service;
	}

	/**
	 * Readiness depends on changed dimensions.
	 * @return boolean
	 */
	@Override
	public boolean isReady() {
		if (ismDimensionsChanged()) {
			setmDimensionsChanged(false);
			forceReady();
			return true;
		} else if (ismLocationChanged()) {
			long currentTimeSec = System.currentTimeMillis() / 1000;
			int difference = mService.getResources().getInteger(R.integer.gps_interval_sec);
			// Send location related data only after some interval
			if (currentTimeSec - mLastTime >= difference) {
				mLastTime = currentTimeSec; // saving the time
				setmLocationChanged(false);
				forceReady();
				return true;
			} else if (!ticked) {
				ticked = true;
				// Again notifying the sender
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						ticked = false;
						mService.sendDataIfNeeded();
					}
				}, ((difference - (currentTimeSec - mLastTime)) * 1000) + 1);
			}
		}
		return false;
	}
}
