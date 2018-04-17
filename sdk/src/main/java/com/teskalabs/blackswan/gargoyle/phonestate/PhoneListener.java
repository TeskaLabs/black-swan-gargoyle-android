package com.teskalabs.blackswan.gargoyle.phonestate;

import android.net.TrafficStats;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Listens to a phone state changes and notify the callback.
 * @author Stepan Hruska, Premysl Cerny
 */
public class PhoneListener extends PhoneStateListener {
	private PhoneListenerCallback m_phoneListenerCallback;
	private TelephonyManager m_telephonyManager;
	private PhoneResponse m_phoneResponse;

	/**
	 * Saves the callback and the telephony manager.
	 * @param phoneListenerCallback PhoneListenerCallback
	 * @param telephonyManager TelephonyManager
	 */
	public PhoneListener(PhoneListenerCallback phoneListenerCallback, TelephonyManager telephonyManager) {
		m_phoneListenerCallback = phoneListenerCallback;
		m_telephonyManager = telephonyManager;
		m_phoneResponse = new PhoneResponse();
	}

	/**
	 * Reactions to the change of data connection.
	 * @param state int
	 * @param networkType int
	 */
	@Override
	public void onDataConnectionStateChanged(int state, int networkType) {
		m_phoneResponse.setData_state(state);
		m_phoneResponse.setData_networkType(networkType);
		long txBytes = TrafficStats.getMobileTxBytes();
		long rxBytes = TrafficStats.getMobileRxBytes();

		if (txBytes > 0) m_phoneResponse.setTX(txBytes);
		if (rxBytes > 0) m_phoneResponse.setRX(rxBytes);

		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		m_phoneResponse.setmFirstDataState(false);

		super.onDataConnectionStateChanged(state, networkType);
	}

	/**
	 * Reactions about changes to cell identity.
	 * @param cellInfo List<CellInfo>
	 */
	@Override
	public void onCellInfoChanged(List<CellInfo> cellInfo) {
		super.onCellInfoChanged(cellInfo);
		//note: Cell Info is not available for all devices. Therefore TelephonyManager is used in UpdateScreen()
		m_phoneResponse.setCellInfo(cellInfo);
		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
	}

	/**
	 * Reactions to the change of signal strength.
	 * @param signalStrength SignalStrength
	 */
	@Override
	public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
		// get the signal strength (a value between 0 and 31)
		int nType = m_telephonyManager.getNetworkType();

		String ssignal = signalStrength.toString();
		String[] parts = ssignal.split(" ");
		if (nType == TelephonyManager.NETWORK_TYPE_LTE){
			m_phoneResponse.setSig_ASU(Integer.parseInt(parts[9]));
			m_phoneResponse.setSig_dbm(Integer.parseInt(parts[9])*2-113);
		} else {
			m_phoneResponse.setSig_ASU(signalStrength.getGsmSignalStrength());
			m_phoneResponse.setSig_dbm(2*m_phoneResponse.getSig_ASU()-113);
		}

		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		super.onSignalStrengthsChanged(signalStrength);
	}

	/**
	 * Reactions to cell location changes.
	 * @param cellLocation CellLocation
	 */
	@Override
	public void onCellLocationChanged(android.telephony.CellLocation cellLocation){
		//cell_loc = (GsmCellLocation)cellLocation;
		m_phoneResponse.setCellLocation(cellLocation);
		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		super.onCellLocationChanged(cellLocation);
	}

	/**
	 * Reactions to data activity.
	 * @param direction int
	 */
	@Override
	public void onDataActivity(int direction){
		m_phoneResponse.setDataActivity_dir(direction);
		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		super.onDataActivity(direction);
	}

	/**
	 * Reactions to the change of call state.
	 * @param state int
	 * @param incomingNumber String
	 */
	@Override
	public void onCallStateChanged(int state, String incomingNumber){
		super.onCallStateChanged(state, incomingNumber);

		m_phoneResponse.setCallState(state);
		if (!incomingNumber.equals("")){
			m_phoneResponse.setClg(incomingNumber);
		}

		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		m_phoneResponse.setmFirstCallState(false);
	}
}