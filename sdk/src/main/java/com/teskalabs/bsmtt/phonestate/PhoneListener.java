package com.teskalabs.bsmtt.phonestate;

import android.net.TrafficStats;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Listens to a phone state changes and notify the callback.
 * @author Stepan Hruska, Premysl Cerny
 */
public class PhoneListener extends PhoneStateListener
{
	private PhoneListenerCallback m_phoneListenerCallback;
	private TelephonyManager m_telephonyManager;
	private PhoneResponse m_phoneResponse;

	public PhoneListener(PhoneListenerCallback phoneListenerCallback, TelephonyManager telephonyManager) {
		m_phoneListenerCallback = phoneListenerCallback;
		m_telephonyManager = telephonyManager;
		m_phoneResponse = new PhoneResponse();
	}

	@Override
	public void onDataConnectionStateChanged(int state,int networkType) {
		m_phoneResponse.setData_state(state);
		m_phoneResponse.setData_networkType(networkType);
		long txBytes = TrafficStats.getMobileTxBytes();
		long rxBytes = TrafficStats.getMobileRxBytes();

		if (txBytes > 0) m_phoneResponse.setmTX(txBytes);
		if (rxBytes > 0) m_phoneResponse.setmRX(rxBytes);

		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		m_phoneResponse.setmFirstDataState(false);

		super.onDataConnectionStateChanged(state, networkType);
	}


	@Override
	public void onCellInfoChanged(List< CellInfo > cellInfo) {
		super.onCellInfoChanged(cellInfo);
		//note: Cell Info is not available for all devices. Therefore TelephonyManager is used in UpdateScreen()
		m_phoneResponse.setCellInfo(cellInfo);
		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
	}

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

	@Override
	public void onCellLocationChanged(android.telephony.CellLocation cellLocation){
		//cell_loc = (GsmCellLocation)cellLocation;
		m_phoneResponse.setCellLocation(cellLocation);
		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		super.onCellLocationChanged(cellLocation);
	}

	@Override
	public void onDataActivity(int direction){
		m_phoneResponse.setDataActivity_dir(direction);
		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		super.onDataActivity(direction);
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber){
		super.onCallStateChanged(state, incomingNumber);

		m_phoneResponse.setCallState(state);
		if (!incomingNumber.equals("")){
			m_phoneResponse.setInNum(incomingNumber);
		}

		m_phoneListenerCallback.onPhoneResponseChange(m_phoneResponse);
		m_phoneResponse.setmFirstCallState(false);
	}
}