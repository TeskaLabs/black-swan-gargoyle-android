package com.teskalabs.blackswan.gargoyle;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.teskalabs.blackswan.gargoyle.cell.CellData;
import com.teskalabs.blackswan.gargoyle.connector.Connector;
import com.teskalabs.blackswan.gargoyle.events.BasicEvent;
import com.teskalabs.blackswan.gargoyle.events.CellEvent;
import com.teskalabs.blackswan.gargoyle.events.ConnectionEvent;
import com.teskalabs.blackswan.gargoyle.events.JsonEvent;
import com.teskalabs.blackswan.gargoyle.events.PhoneEvent;
import com.teskalabs.blackswan.gargoyle.location.LocationHelper;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleListener;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleServiceConnection;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleClientHandler;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleServerHandler;
import com.teskalabs.blackswan.gargoyle.phonestate.PhoneListener;
import com.teskalabs.blackswan.gargoyle.phonestate.PhoneListenerCallback;
import com.teskalabs.blackswan.gargoyle.phonestate.PhoneResponse;
import com.teskalabs.seacat.android.client.SeaCatClient;

/**
 * This class gets information about the phone and its behavior and sends them to the server when necessary.
 * @author Stepan Hruska, Premysl Cerny
 */
public class BSGargoyleService extends Service implements PhoneListenerCallback, LocationListener {
	public static final String LOG_TAG = "BSGargoyleService";

	// Event constants
	public static final int BASIC_EVENT_INDEX = 0;
	public static final int CONNECTION_EVENT_INDEX = 1;
	public static final int PHONE_EVENT_INDEX = 2;
	public static final int CELL_EVENT_INDEX = 3;

	// Telephony manager
	private TelephonyManager TMgr;
	// Sending data
	private Connector mConnector;
	private String clientTag;
	private BroadcastReceiver mSeaCatReceiver;
	// Listeners
	private PhoneListener PhoneStateListener;
	// Connection with activities
	private Messenger mMessenger;
	private BSGargoyleServerHandler mMessengerServer;
	// List of JSON events
	ArrayList<JsonEvent> mEvents;
	// The current location
	private Location mLocation;
	// Allow closing
	private boolean allowClose;
	// GPS enabled
	private boolean mGPSEnabled;
	// Timer
	private Timer mTimer;
	// Intent
	private Intent mIntent;

	/**
	 * A basic constructor.
	 */
	public BSGargoyleService() {
		// Messaging
		mMessengerServer = new BSGargoyleServerHandler(this);
		mMessenger = new Messenger(mMessengerServer);
		// Events
		mEvents = new ArrayList<>();
		// Other
		clientTag = "";
		mTimer = null;
		mIntent = null;
	}

	/**
	 * Allows closing of the service;
	 */
	public void allowClose() {
		allowClose = true;
	}

	/**
	 * Makes sure that all listeners and necessary objects are removed after shutting down the service.
	 */
	@Override
	public void onDestroy() {
		// Location listener
		try {
			LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
			if (locationManager != null) {
				locationManager.removeUpdates(this);
			}
			// Timer
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
			// Phone listener
			if (TMgr != null && PhoneStateListener != null)
				TMgr.listen(PhoneStateListener, android.telephony.PhoneStateListener.LISTEN_NONE);
			// Connector
			if (mConnector != null) {
				mConnector.delete();
				mConnector = null;
			}
			if (mSeaCatReceiver != null) {
				unregisterReceiver(mSeaCatReceiver);
				mSeaCatReceiver = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Wakelock
		if (mIntent != null) {
			BSWakefulReceiver.completeWakefulIntent(mIntent);
		}
		// Log
		if (!allowClose) {
			Log.i(LOG_TAG, getResources().getString(R.string.log_closed));
		}
		// This
		super.onDestroy();
	}

	/**
	 * Runs the service which obtains phone-related data and sends them to a server.
	 * @param context Context
	 */
	@RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE})
	public static void run(Context context) {
		// Starting the service
		Intent broadcastIntent = new Intent(context, BSWakefulReceiver.class);
		context.sendBroadcast(broadcastIntent);
	}

	/**
	 * Creates a connection between a service and an activity to communicate through messages.
	 * @param context Context
	 * @param listener BSGargoyleListener
	 * @return BSGargoyleServiceConnection
	 */
	public static BSGargoyleServiceConnection startConnection(Context context, BSGargoyleListener listener) {
		 // Binding the service
		Intent intent = new Intent(context, BSGargoyleService.class);
		try {
			Messenger receiveMessenger = new Messenger(new BSGargoyleClientHandler(context, listener));
			BSGargoyleServiceConnection connection = new BSGargoyleServiceConnection(receiveMessenger);
			context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
			return connection;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Shuts down a connection between an activity and a service.
	 * @param context Context
	 * @param connection BSGargoyleServiceConnection
	 */
	public static void stopConnection(Context context, BSGargoyleServiceConnection connection) {
		// Unbinding
		if (connection != null) {
			connection.requestClose(); // request closing of the service
			context.unbindService(connection);
		}
	}

	/**
	 * Stops the service.
	 * @param context Context
	 */
	public static void stop(Context context) {
		Intent intent = new Intent(context, BSGargoyleService.class);
		// Stopping
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
			if (manager != null) {
				for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
					if ("com.teskalabs.blackswan.gargoyle.BSGargoyleService".equals(service.service.getClassName())) {
						return true;
					}
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Checks if the process of the service is running.
	 * @param context Context
	 * @return boolean
	 */
	public static boolean isProcess(Context context) {
		ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		try {
			if (manager != null) {
				List<ActivityManager.RunningAppProcessInfo> RAP = manager.getRunningAppProcesses();
				if (RAP == null)
					return false;
				for (ActivityManager.RunningAppProcessInfo processInfo : RAP) {
					if ("com.teskalabs.blackswan.gargoyle.app:bsgargoyle".equals(processInfo.processName)) {
						return true;
					}
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Checks and returns the events.
	 * @return ArrayList<JsonEvent>
	 */
	public ArrayList<JsonEvent> getEvents() {
		// Checking that the events are ready to be read
		if (mLocation == null)
			return new ArrayList<>();
		// Returning the events
		return mEvents;
	}

	/**
	 * Gets information about allowed sending to the server via SeaCat.
	 * @return boolean
	 */
	public boolean isSendingAllowed() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			return bundle.getBoolean("com.teskalabs.blackswan.gargoyle.use_seacat", false);
		} catch (PackageManager.NameNotFoundException|NullPointerException e) {
			Log.e(LOG_TAG, "Unable to load application meta-data: " + e.getMessage());
			return false;
		}
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
		mIntent = intent;
		if (!BSGargoyleHelper.isFineLocationPermissionGranted(this)
				|| !BSGargoyleHelper.isPhoneStatePermissionGranted(this)) {
			Log.e(LOG_TAG, getResources().getString(R.string.log_permissions));
			stopSelf();
		} else {
			// Checking if the are allowed to send data to the server
			if (isSendingAllowed()) {
				final BSGlobalClass globalVariable = (BSGlobalClass)getApplicationContext();
				if (!globalVariable.isWasInitialized()) {
					SeaCatClient.initialize(this);
					globalVariable.setWasInitialized(true);
				}
				// Initializing the sending object
				mConnector = new Connector(this, getResources().getString(R.string.connector_url));
				// Registering the SeaCat receiver
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(SeaCatClient.ACTION_SEACAT_STATE_CHANGED);
				intentFilter.addAction(SeaCatClient.ACTION_SEACAT_CSR_NEEDED);
				intentFilter.addAction(SeaCatClient.ACTION_SEACAT_CLIENTID_CHANGED);
				intentFilter.addCategory(SeaCatClient.CATEGORY_SEACAT);
				if (isSeaCatReady(SeaCatClient.getState())) {
					mConnector.setReady(); // we are ready to send data!
				}
				mSeaCatReceiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (intent.hasCategory(SeaCatClient.CATEGORY_SEACAT)) {
							// Listening for Client Tag changes
							String client_tag = intent.getStringExtra(SeaCatClient.EXTRA_CLIENT_TAG);
							if (client_tag != null && !client_tag.equals(clientTag)) {
								clientTag = client_tag;
								mMessengerServer.sendClientTag(client_tag);
							}
							// Action
							String action = intent.getAction();
							// Listening for state changes
							if (action.equals(SeaCatClient.ACTION_SEACAT_STATE_CHANGED)) {
								String state = intent.getStringExtra(SeaCatClient.EXTRA_STATE);
								if (isSeaCatReady(state)) {
									mConnector.setReady(); // we are ready to send data!
								} else {
									mConnector.unsetReady();
								}
							}
						}
					}
				};
				registerReceiver(mSeaCatReceiver, intentFilter);
			} else {
				mConnector = null;
			}
			mGPSEnabled = false;
			initialize(); // initialize
		}
		allowClose = false;
		return Service.START_STICKY;
	}

	/**
	 * Checks if the SeaCat is ready.
	 * @param state String
	 * @return boolean
	 */
	private boolean isSeaCatReady(String state) {
		return ((state.charAt(3) == 'Y') && (state.charAt(4) == 'N') && (state.charAt(0) != 'f'));
	}

	/**
	 * Gets the connector's client tag.
	 * @return String
	 */
	public String getClientTag() {
		if (mConnector == null) {
			return "";
		} else {
			return SeaCatClient.getClientTag();
		}
	}

	/**
	 * Resets the connector's identity.
	 */
	public void resetIdentity() {
		if (mConnector != null) {
			try {
				SeaCatClient.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Takes care of binding the activity and service together.
	 * @param intent Intent
	 * @return IBinder
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	/**
	 * Reacts to phone state change events.
	 * @param phoneResponse PhoneResponse
	 */
	@Override
	public void onPhoneResponseChange(PhoneResponse phoneResponse) {
		// Related to the phone response
		if (phoneResponse != null) {
			long txBytes = TrafficStats.getMobileTxBytes();
			long rxBytes = TrafficStats.getMobileRxBytes();
			if (txBytes > 0) phoneResponse.setTX(txBytes);
			if (rxBytes > 0) phoneResponse.setRX(rxBytes);
			// Saving the phone response
			PhoneEvent phoneEvent = (PhoneEvent)mEvents.get(PHONE_EVENT_INDEX);
			phoneEvent.changePhoneResponse(phoneResponse);
		}
		// Refreshing variables
		refreshAllInfo();
		// Sending data if necessary
		sendDataIfNeeded();
	}

	/**
	 * Initializes the service's objects and loads data about the phone.
	 */
	public void initialize() {
		// Initializing necessary variables
		// dataNetStr = "";
		// Adding events to the list
		mEvents.add(BASIC_EVENT_INDEX, new BasicEvent(this));
		mEvents.add(CONNECTION_EVENT_INDEX, new ConnectionEvent());
		mEvents.add(PHONE_EVENT_INDEX, new PhoneEvent());
		mEvents.add(CELL_EVENT_INDEX, new CellEvent());
		// Getting the objects where we are getting the information from
		TMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		PhoneStateListener = new PhoneListener(this, TMgr);
		// Initializing the location listener
		mLocation = null;
		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			try {
				if (locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null)
					mLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
				if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
					mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
					mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				// Registering the network provider
				if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
				}
				// Registering the GPS provider
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
					mGPSEnabled = true;
				}
				JsonEvent.changeLocationAtAll(mEvents, mLocation); // saving
			} catch (SecurityException e) {
				e.printStackTrace();
				Log.e(LOG_TAG, getResources().getString(R.string.location_permissions));
			}
		}
		// Initializing the phone listener
		TMgr.listen(PhoneStateListener,PhoneListener.LISTEN_SIGNAL_STRENGTHS | PhoneListener.LISTEN_CELL_LOCATION |
				PhoneListener.LISTEN_DATA_CONNECTION_STATE| PhoneListener.LISTEN_DATA_ACTIVITY|
				PhoneListener.LISTEN_CALL_STATE|PhoneListener.LISTEN_CELL_INFO|PhoneListener.LISTEN_SERVICE_STATE);
		// Refreshing variables
		refreshAllInfo();
		// Initializing the timer to send basic events
		restartTimer();
	}

	/**
	 * (Re)starts the timer fo basic events.
	 */
	public void restartTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		// Creates the timer
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				sendBasicEvent();
			}
		}, getResources().getInteger(R.integer.basic_event_period_ms),
				getResources().getInteger(R.integer.basic_event_period_ms));
	}

	/**
	 * Enables the GPS provider if it is not enabled yet.
	 */
	private void enableGPS() {
		if (mGPSEnabled)
			return;
		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			try {
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
					mGPSEnabled = true;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
				Log.e(LOG_TAG, getResources().getString(R.string.location_permissions));
			}
		}
	}

	/**
	 * Refreshes all information that might have changed.
	 */
	private void refreshAllInfo() {
		// Getting the basic phone information
		retrieveBasicPhoneInformation();
		// Getting the advanced phone information
		refreshAdvancedPhoneInformation();
	}

	/**
	 * Gets the basic information about the phone (dimensions).
	 */
	private void retrieveBasicPhoneInformation() {
		// Check
		enableGPS();
		// Phone information
		try {
			JsonEvent.changePhoneInfoAtAll(mEvents,
					BSGargoyleHelper.getPhoneVendorModel(),
					BSGargoyleHelper.getPhoneTypeStr(TMgr),
					TMgr.getSubscriberId(),
					TMgr.getDeviceId(),
					TMgr.getLine1Number(),
					TMgr.getSimSerialNumber());
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Refreshes advanced information about the phone and its current state.
	 */
	private void refreshAdvancedPhoneInformation() {
		// Network
		String MCC_MNC = TMgr.getNetworkOperator();
		String net_name = TMgr.getNetworkOperatorName();
		JsonEvent.changePhoneNetworkAtAll(mEvents, MCC_MNC, net_name);

		// Connection
		// Roaming
		int roaming = 0;
		if (TMgr.isNetworkRoaming()) {
			roaming = 1;
		}
		if (TMgr.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN){
			roaming = -1;
		}
		// Other info
		boolean haveMobileConnection = BSGargoyleHelper.haveMobileConnection(this);
		int dconn = TMgr.getDataState();
		// if (m_phoneResponse != null) {
		// dataNetStr = BSGargoyleHelper.getNetworkType(m_phoneResponse.getData_networkType());
		// }
		// Saving
		ConnectionEvent connectionEvent = (ConnectionEvent)mEvents.get(CONNECTION_EVENT_INDEX);
		connectionEvent.changeNetwork(haveMobileConnection, dconn, roaming);

		// Cell info
		CellEvent cellEvent = (CellEvent)mEvents.get(CELL_EVENT_INDEX);
		CellData cellData = cellEvent.getCellData();
		cellData = BSGargoyleHelper.getCellLocation(cellData, TMgr, cellEvent.getPhoneTypeStr());
		cellData = BSGargoyleHelper.getCellSignal(cellData, TMgr);
		cellEvent.changeCell(cellData);
	}

	/**
	 * Receives a new location update.
	 * @param location Location
	 */
	public void onLocationChanged(Location location) {
		// Checks if the current location is better than the last one
		if (mLocation == null) {
			mLocation = location;
		} else {
			if (LocationHelper.isBetterLocation(location, mLocation)) {
				mLocation = location;
			} else {
				return;
			}
		}
		// Saving the location
		JsonEvent.changeLocationAtAll(mEvents, mLocation);
		// Refreshing variables
		refreshAllInfo();
		// Sending data if necessary
		sendDataIfNeeded();
	}

	/**
	 * Reacts to the location's onStatusChanged event.
	 * @param provider String
	 * @param status int
	 * @param extras Bundle
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	/**
	 * Reacts to the onProviderEnabled event.
	 * @param provider String
	 */
	public void onProviderEnabled(String provider) {}

	/**
	 * Reacts to the onProviderDisabled event.
	 * @param provider String
	 */
	public void onProviderDisabled(String provider) {}


	/**
	 * Checks if it is necessary to send the data, and if so, it performs the sending.
	 */
	public void sendDataIfNeeded() {
		// Checking before sending
		ArrayList<JsonEvent> events = getEvents();
		// Sending the data
		boolean wasOneSent = false;
		for (int i = 0; i < events.size(); i++) {
			JsonEvent event = events.get(i);
			if (event.isReady()) {
				wasOneSent = true;
				sendJSON(event.receiveEvent());
			}
		}
		// Restarts the timer if something has changed
		if (wasOneSent) {
			restartTimer();
		}
	}

	/**
	 * Forces a basic event to be sent.
	 */
	public void sendBasicEvent() {
		// Checking before sending
		ArrayList<JsonEvent> events = getEvents();
		// Sending the data
		if (events != null) {
			BasicEvent basicEvent = (BasicEvent) events.get(BASIC_EVENT_INDEX);
			if (basicEvent != null) {
				basicEvent.forceReady();
				sendJSON(basicEvent.receiveEvent());
			}
		}
	}

	/**
	 * Sends JSON to all connectors.
	 * @param JSON JSONObject
	 */
	private void sendJSON(JSONObject JSON) {
		if (JSON != null) {
			try {
				JSONObject sendingJSON = new JSONObject(JSON.toString());
				// Adding the data to the sender
				if (mConnector != null) {
					mConnector.send(sendingJSON);
				}
				// Passing the data to activities
				mMessengerServer.sendJSON(sendingJSON);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
