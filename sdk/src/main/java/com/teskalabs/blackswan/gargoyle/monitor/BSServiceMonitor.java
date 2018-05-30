package com.teskalabs.blackswan.gargoyle.monitor;

import android.content.Context;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;

import com.teskalabs.blackswan.gargoyle.BSGargoyleService;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleMessage;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A class that monitor's the state of BSGargoyleService.
 * @author Premysl Cerny
 */
public class BSServiceMonitor {
	public static final int STATE_UNKNOWN = 0;
	public static final int STATE_STOPPED = 1;
	public static final int STATE_SENDING = 2;
	public static final int STATE_IDLE = 3;
	public static final int STATE_DEVICE_IDLE = 4;
	public static final int STATE_STOPPED_PROCESS_ON = 5;

	private Context mContext;
	private BSServiceMonitorListener mListener;
	private int mState;
	private Timer mTimer;

	/**
	 * The constructor receives a context necessary to get basic information about the service.
	 * @param context Context
	 * @param listener BSServiceMonitorListener
	 */
	public BSServiceMonitor(Context context, BSServiceMonitorListener listener) {
		mContext = context;
		mListener = listener;
		mState = STATE_UNKNOWN;
	}

	/**
	 * Starts the timer which checks the current state.
	 * @param periodInMillis int
	 */
	public void startTimer(int periodInMillis) {
		if (mTimer == null) {
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					updateWithTimeOrEvent();
				}
			}, 0, periodInMillis);
		}
	}

	/**
	 * Stops the previously started timer.
	 */
	public void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	/**
	 * Updates the current state according to basic flags.
	 */
	public void updateWithTimeOrEvent() {
		boolean isRunning = BSGargoyleService.isRunning(mContext);
		boolean isProcess = BSGargoyleService.isProcess(mContext);
		if (isRunning && !isProcess) {
			updateState(STATE_IDLE);
		} else if (!isRunning && isInSleepMode()) {
			updateState(STATE_DEVICE_IDLE);
		} else if (!isRunning && isProcess) {
			updateState(STATE_STOPPED_PROCESS_ON);
		} else if (!isRunning && !isProcess) {
			updateState(STATE_STOPPED);
		}
	}

	/**
	 * Updates the current state according to the message and basic flags.
	 * @param msg Message
	 */
	public void updateWithMessage(Message msg) {
		boolean isRunning = BSGargoyleService.isRunning(mContext);
		boolean isProcess = BSGargoyleService.isProcess(mContext);
		switch (msg.what) {
			case BSGargoyleMessage.MSG_JSON_EVENT:
			case BSGargoyleMessage.MSG_CONNECTED:
				if (isRunning && isProcess) {
					updateState(STATE_SENDING);
				}
				break;
		}
	}

	/**
	 * Gets the current state.
	 * @return int
	 */
	public int getState() {
		return mState;
	}

	/**
	 * Updates the state only if not the same as the previous one.
	 * @param state int
	 */
	protected void updateState(int state) {
		if (state != mState) {
			mState = state;
			mListener.onReceiveServiceState(state);
		}
	}

	/**
	 * Checks if the device is in idle mode.
	 * @return boolean
	 */
	protected boolean isInSleepMode() {
		PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		if (pm != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				return pm.isDeviceIdleMode();
			} else {
				return !pm.isInteractive();
			}
		} else {
			return false;
		}
	}
}
