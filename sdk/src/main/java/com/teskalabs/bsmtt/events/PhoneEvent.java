package com.teskalabs.bsmtt.events;

import com.teskalabs.bsmtt.phonestate.PhoneResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An event that reacts to phone event changes.
 * @author Premysl Cerny
 */
public class PhoneEvent extends JsonEvent {
	// Information related to the phone response
	private PhoneResponse mPhoneResponse; // added by Premysl

	/**
	 * A basic constructor.
	 */
	public PhoneEvent() {
		super(BSMTTEvents.PHONE_EVENT);
		mPhoneResponse = null;
	}

	/**
	 * Reacts to changes of the phone state of the event.
	 * @param phoneResponse PhoneResponse
	 */
	public void changePhoneResponse(PhoneResponse phoneResponse) {
		// We can check the differences between the new and the old object
		if (mPhoneResponse == null ||
				mPhoneResponse.getCellInfo() != phoneResponse.getCellInfo() ||
				mPhoneResponse.getCellLocation() != phoneResponse.getCellLocation() ||
				!mPhoneResponse.getClg().equals(phoneResponse.getClg()) ||
				mPhoneResponse.getSig_dbm() != phoneResponse.getSig_dbm() ||
				mPhoneResponse.getSig_ASU() != phoneResponse.getSig_ASU() ||
				mPhoneResponse.getCallState() != phoneResponse.getCallState() ||
				mPhoneResponse.getData_networkType() != phoneResponse.getData_networkType() ||
				mPhoneResponse.getData_state() != phoneResponse.getData_state() ||
				mPhoneResponse.getDataActivity_dir() != phoneResponse.getDataActivity_dir() ||
				mPhoneResponse.getRX() != phoneResponse.getRX() ||
				mPhoneResponse.getTX() != phoneResponse.getTX()) {
			// Saving the new response
			try {
				mPhoneResponse = phoneResponse.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			// Saving the response to the JSON
			JSONObject data = getEventData();
			try {
				// From the phone response
				if (mPhoneResponse != null) {
					if (mPhoneResponse.getData_state() != Integer.MIN_VALUE)
						data.put("data_state", mPhoneResponse.getData_state());
					if (mPhoneResponse.getData_networkType() != Integer.MIN_VALUE)
						data.put("data_network_type", mPhoneResponse.getData_networkType());
					if (mPhoneResponse.getSig_ASU() != Integer.MIN_VALUE)
						data.put("sig_ASU", mPhoneResponse.getSig_ASU());
					if (mPhoneResponse.getSig_dbm() != Integer.MIN_VALUE)
						data.put("sig_dbm", mPhoneResponse.getSig_dbm());
					if (mPhoneResponse.getDataActivity_dir() != Integer.MIN_VALUE)
						data.put("data_activity_dir", mPhoneResponse.getDataActivity_dir());
					if (mPhoneResponse.getCallState() != Integer.MIN_VALUE)
						data.put("call_state", mPhoneResponse.getCallState());
					if (mPhoneResponse.getRX() != Long.MIN_VALUE)
						data.put("RX", mPhoneResponse.getRX());
					if (mPhoneResponse.getTX() != Long.MIN_VALUE)
						data.put("TX", mPhoneResponse.getTX());
					if (!mPhoneResponse.getClg().equals(""))
						data.put("Clg", mPhoneResponse.getClg());
					saveEventData(data); // save
					dataReceived(); // notify
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
