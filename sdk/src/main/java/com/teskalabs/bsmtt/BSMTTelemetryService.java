package com.teskalabs.bsmtt;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.net.TrafficStats;
import android.os.IBinder;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.teskalabs.bsmtt.cell.CellData;
import com.teskalabs.bsmtt.connector.Connector;
import com.teskalabs.bsmtt.phonestate.PhoneListener;
import com.teskalabs.bsmtt.phonestate.PhoneListenerCallback;
import com.teskalabs.bsmtt.phonestate.PhoneResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class gets information about the phone and its behavior and sends them to the server when necessary.
 * @author Stepan Hruska, Premysl Cerny
 */
public class BSMTTelemetryService extends Service implements PhoneListenerCallback {
	public static final String LOG_TAG = "BSMTTelemetryService";

	public TelephonyManager TMgr;
	public Connector mConnector;

	// Information related to the phone itself
	// Basic (dimensions)
	private String mVendorModel;
	private String PhoneTypeStr;
	private String NetOp;
	private String net_name;
	private String IMSI;
	private String IMEI;
	private String MSISDN; // added by Premysl
	private String iccid;
	private Long timestamp;
	// Advanced
	private boolean haveMobileConnection;
	private String dconn;
	private String dataNetStr;
	private String mRoaming; // added by Premysl
	// Information related to the cell
	private CellData m_cellData; // added by Premysl
	// Information related to the phone response
	private PhoneResponse m_phoneResponse; // added by Premysl

	// Listeners
	PhoneListener PhoneStateListener;

	public BSMTTelemetryService() {

	}

	@Override
	public void onDestroy() {
		TMgr.listen(PhoneStateListener, android.telephony.PhoneStateListener.LISTEN_NONE);
		mConnector.delete();
		super.onDestroy();
	}

	/**
	 * Runs the service which obtains phone-related data and sends them to a server.
	 * @param context Context
	 */
	@RequiresPermission(allOf = {Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE})
	public static void run(Context context) {
		Intent intent = new Intent(context, BSMTTelemetryService.class);
		context.startService(intent);
	}

	/**
	 * Stops the service.
	 * @param context Context
	 */
	public static void stop(Context context) {
		Intent intent = new Intent(context, BSMTTelemetryService.class);
		context.stopService(intent);
	}

	/**
	 * Checks if the service is already running.
	 * @param context Context
	 * @return boolean
	 */
	public static boolean isRunning(Context context) {
		ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		try {
			for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if ("com.teskalabs.bsmtt.BSMTTelemetryService".equals(service.service.getClassName())) {
					return true;
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Checks permissions, initializes the service and obtains the data.
	 * @param intent Intent
	 * @param flags int
	 * @param startId int
	 * @return int
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (BSMTTelemetryHelper.isCoarseLocationPermissionGranted(this)
					&& BSMTTelemetryHelper.isPhoneStatePermissionGranted(this)) {
				initialize(); // start
				sendDataIfNeeded();
			} else {
				Log.e(LOG_TAG, getResources().getString(R.string.log_permissions));
				stopSelf();
			}
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Reacts to phone state change events.
	 * @param phoneResponse PhoneResponse
	 */
	@Override
	public void onPhoneResponseChange(PhoneResponse phoneResponse) {
		// We can check the differences between the new and the old object
		if (m_phoneResponse == null ||
				m_phoneResponse.getCellInfo() != phoneResponse.getCellInfo() ||
				m_phoneResponse.getCellLocation() != phoneResponse.getCellLocation() ||
				!m_phoneResponse.getInNum().equals(phoneResponse.getInNum()) ||
				m_phoneResponse.getSig_dbm() != phoneResponse.getSig_dbm() ||
				m_phoneResponse.getSig_ASU() != phoneResponse.getSig_ASU() ||
				m_phoneResponse.getCallState() != phoneResponse.getCallState() ||
				m_phoneResponse.getData_networkType() != phoneResponse.getData_networkType() ||
				m_phoneResponse.getData_state() != phoneResponse.getData_state() ||
				m_phoneResponse.getDataActivity_dir() != phoneResponse.getDataActivity_dir() ||
				m_phoneResponse.getmRX() != phoneResponse.getmRX() ||
				m_phoneResponse.getmTX() != phoneResponse.getmTX()) {
			// Saving the new response
			try {
				m_phoneResponse = phoneResponse.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			// Refreshing variables
			retrieveBasicPhoneInformation();
			refreshAdvancedPhoneInformation();
			// Sending data if necessary
			sendDataIfNeeded();
		}
//		m_phoneResponse = phoneResponse;
//		// Refreshing variables
//		refreshAdvancedPhoneInformation();
//		// Sending data if necessary
//		sendDataIfNeeded();
	}

	/**
	 * Initializes the service's objects and loads data about the phone.
	 */
	private void initialize() {
		// Initializing necessary variables
		dataNetStr = "";
		m_cellData = new CellData();
		// Getting the objects where we are getting the information from
		TMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		PhoneStateListener = new PhoneListener(this, TMgr);
		// Initializing the sending object
		mConnector = new Connector(this, getResources().getString(R.string.connector_url));
		// Initializing the phone listener
		TMgr.listen(PhoneStateListener,PhoneListener.LISTEN_SIGNAL_STRENGTHS | PhoneListener.LISTEN_CELL_LOCATION |
				PhoneListener.LISTEN_DATA_CONNECTION_STATE| PhoneListener.LISTEN_DATA_ACTIVITY|
				PhoneListener.LISTEN_CALL_STATE|PhoneListener.LISTEN_CELL_INFO|PhoneListener.LISTEN_SERVICE_STATE);
		// Getting the basic phone information
		retrieveBasicPhoneInformation();
		// Getting the advanced phone information
		refreshAdvancedPhoneInformation();
	}

	/**
	 * Gets the basic information about the phone (dimensions).
	 */
	private void retrieveBasicPhoneInformation() {
		// Phone information
		try {
			mVendorModel = BSMTTelemetryHelper.getPhoneVendorModel();
			PhoneTypeStr = BSMTTelemetryHelper.getPhoneTypeStr(TMgr);
			IMSI = TMgr.getSubscriberId();
			IMEI = TMgr.getDeviceId();
			iccid = TMgr.getSimSerialNumber();
			MSISDN = TMgr.getLine1Number();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		// Timestamp
		timestamp = System.currentTimeMillis();
	}

	/**
	 * Refreshes advanced information about the phone and its current state.
	 */
	private void refreshAdvancedPhoneInformation() {
		// Roaming
		mRoaming = "Home network";
		if (TMgr.isNetworkRoaming()) {
			mRoaming = "Roaming";
		}
		if (TMgr.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN){
			mRoaming = "";
		}

		// Cell info
		m_cellData = BSMTTelemetryHelper.getCellLocation(m_cellData, TMgr, PhoneTypeStr);
		m_cellData = BSMTTelemetryHelper.getCellSignal(m_cellData, TMgr);

		// Connection
		haveMobileConnection = BSMTTelemetryHelper.haveMobileConnection(this);
		dconn = BSMTTelemetryHelper.getDataState(TMgr);
		NetOp = TMgr.getNetworkOperator();
		net_name = TMgr.getNetworkOperatorName();
		if (m_phoneResponse != null) {
			dataNetStr = BSMTTelemetryHelper.getNetworkType(m_phoneResponse.getData_networkType());
		}

		// Related to the phone response
		if (m_phoneResponse != null) {
			long txBytes = TrafficStats.getMobileTxBytes();
			long rxBytes = TrafficStats.getMobileRxBytes();
			if (txBytes > 0) m_phoneResponse.setmTX(txBytes);
			if (rxBytes > 0) m_phoneResponse.setmRX(rxBytes);
		}
	}

	/**
	 * Checks if it is necessary to send the data, and if so, it performs the sending.
	 */
	private void sendDataIfNeeded() {
		// Checking before sending
		if (m_phoneResponse == null)
			return;
		// Getting the data
		JSONObject JSON = prepareJSONForSending();
		// Sending the data
		if (JSON != null) {
			mConnector.send(JSON);
			// Printing the data
			Log.i(LOG_TAG, JSON.toString());
		}
	}

	/**
	 * Prepares/maps data in the JSON format to be sent.
	 * @return JSONObject
	 */
	private JSONObject prepareJSONForSending() {
		JSONObject JSON = new JSONObject();
		try {
			// From the service's variables
			// Basic
			JSON.put("@timestamp", timestamp);
			JSON.put("vendor_model", mVendorModel);
			JSON.put("phone_type", PhoneTypeStr);
			JSON.put("net_op", NetOp);
			JSON.put("net_name", net_name);
			if (IMSI != null)
				JSON.put("IMSI", IMSI);
			if (IMEI != null)
				JSON.put("IMEI", IMEI);
			if (MSISDN != null && !MSISDN.equals(""))
				JSON.put("MSISDN", MSISDN);
			if (iccid != null)
				JSON.put("iccid", iccid);

			// Advanced
			JSON.put("have_mobile_conn", haveMobileConnection);
			JSON.put("dconn", dconn);
			if (!dataNetStr.equals(""))
				JSON.put("data_net", dataNetStr);
			JSON.put("roaming", mRoaming);
			// From the cell
			if (m_cellData.getASU() != Integer.MIN_VALUE)
				JSON.put("ASU", m_cellData.getASU());
			if (m_cellData.getBSID() != Integer.MIN_VALUE)
				JSON.put("BSID", m_cellData.getBSID());
			if (m_cellData.getBSILat() != Integer.MIN_VALUE)
				JSON.put("BSILat", m_cellData.getBSILat());
			if (m_cellData.getBSILon() != Integer.MIN_VALUE)
				JSON.put("BSILon", m_cellData.getBSILon());
			if (m_cellData.getCi() != Integer.MIN_VALUE)
				JSON.put("ci", m_cellData.getCi());
			if (m_cellData.getCID() != Integer.MIN_VALUE)
				JSON.put("cid", m_cellData.getCID());
			if (m_cellData.getDbm() != Integer.MIN_VALUE)
				JSON.put("Dbm", m_cellData.getDbm());
			if (m_cellData.geteNodeB() != Integer.MIN_VALUE)
				JSON.put("eNodeB", m_cellData.geteNodeB());
			if (m_cellData.getLAC() != Integer.MIN_VALUE)
				JSON.put("lac", m_cellData.getLAC());
			if (m_cellData.getNetID() != Integer.MIN_VALUE)
				JSON.put("NetID", m_cellData.getNetID());
			if (m_cellData.getPci() != Integer.MIN_VALUE)
				JSON.put("pci", m_cellData.getPci());
			if (m_cellData.getPSC() != Integer.MIN_VALUE)
				JSON.put("PSC", m_cellData.getPSC());
			if (m_cellData.getRNC() != Integer.MIN_VALUE)
				JSON.put("RNC", m_cellData.getRNC());
			if (m_cellData.getSysID() != Integer.MIN_VALUE)
				JSON.put("SysID", m_cellData.getSysID());
			if (m_cellData.getTac() != Integer.MIN_VALUE)
				JSON.put("tac", m_cellData.getTac());
			if (m_cellData.getTimAdv() != Integer.MIN_VALUE)
				JSON.put("TimAdv", m_cellData.getTimAdv());
			// From the phone response
			if (m_phoneResponse != null) {
				if (m_phoneResponse.getData_state() != Integer.MIN_VALUE)
					JSON.put("data_state", m_phoneResponse.getData_state());
				if (m_phoneResponse.getData_networkType() != Integer.MIN_VALUE)
					JSON.put("data_network_type", m_phoneResponse.getData_networkType());
				if (m_phoneResponse.getSig_ASU() != Integer.MIN_VALUE)
					JSON.put("sig_ASU", m_phoneResponse.getSig_ASU());
				if (m_phoneResponse.getSig_dbm() != Integer.MIN_VALUE)
					JSON.put("sig_dbm", m_phoneResponse.getSig_dbm());
				if (m_phoneResponse.getDataActivity_dir() != Integer.MIN_VALUE)
					JSON.put("data_activity_dir", m_phoneResponse.getDataActivity_dir());
				if (m_phoneResponse.getCallState() != Integer.MIN_VALUE)
					JSON.put("call_state", m_phoneResponse.getCallState());
				if (m_phoneResponse.getmRX() != Long.MIN_VALUE)
					JSON.put("RX", m_phoneResponse.getmRX());
				if (m_phoneResponse.getmTX() != Long.MIN_VALUE)
					JSON.put("TX", m_phoneResponse.getmTX());
				if (!m_phoneResponse.getInNum().equals(""))
					JSON.put("inNum", m_phoneResponse.getInNum());
			}
			// return
			return JSON;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
