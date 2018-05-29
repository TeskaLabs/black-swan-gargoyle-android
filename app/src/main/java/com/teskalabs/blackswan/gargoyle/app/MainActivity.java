package com.teskalabs.blackswan.gargoyle.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.teskalabs.blackswan.gargoyle.BSGargoyleService;
import com.teskalabs.blackswan.gargoyle.monitor.BSServiceMonitor;
import com.teskalabs.blackswan.gargoyle.monitor.BSServiceMonitorListener;
import com.teskalabs.blackswan.gargoyle.events.BSGargoyleEvents;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleServiceConnection;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleListener;
import com.teskalabs.blackswan.gargoyle.messaging.BSGargoyleMessage;
import com.teskalabs.blackswan.gargoyle.app.fragments.FragmentInfo;
import com.teskalabs.blackswan.gargoyle.app.fragments.FragmentLog;
import com.teskalabs.blackswan.gargoyle.app.fragments.ViewPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main activity class that calls the BS SDK to gather and send data.
 * @author Premysl Cerny
 */
public class MainActivity extends AppCompatActivity implements BSGargoyleListener, BSServiceMonitorListener {
	// GPS
	public static int GPS_SETTINGS_INTENT = 200;
	private boolean mOnlyWifiLoc;
	// Permissions
	public static int ACCESS_FINE_LOCATION_PERMISSION = 300;
	public static int READ_PHONE_STATE_PERMISSION = 301;
	// Connection with the service
	private BSGargoyleServiceConnection mConnection;
	private boolean isConnected;
	// JSON showing
	private boolean wasFirstJSON;
	// Keeping some important data
	private String clientTag;
	private String lastBasicEvent;
	// BSGargoyleService monitor
	private BSServiceMonitor mMonitor;

	/**
	 * A main function to register basic components.
	 * @param savedInstanceState Bundle
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Default values
		mConnection = null;
		wasFirstJSON = false;
		mOnlyWifiLoc = false;
		isConnected = false;
		clientTag = "";
		lastBasicEvent = null;

		// Preparing the button text
		Button btn = findViewById(R.id.sendButton);
		if (BSGargoyleService.isRunning(this)) {
			btn.setText(getResources().getString(R.string.btn_stop));
			// Register the connection
			mConnection = BSGargoyleService.startConnection(this, this);
		}

		// View pager
		ViewPager viewPager = findViewById(R.id.pager);
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

		// Add Fragments to adapter one by one
		adapter.addFragment(new FragmentInfo(), getResources().getString(R.string.fragment_info));
		adapter.addFragment(new FragmentLog(), getResources().getString(R.string.fragment_log));
		viewPager.setAdapter(adapter);

		// Adding tabs
		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);

		// Preparing the monitor
		mMonitor = new BSServiceMonitor(this, this);
		mMonitor.updateWithTimeOrEvent();

		// Showing the current version
		try {
			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			TextView textVersion = findViewById(R.id.textVersion);
			textVersion.setText(versionName);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creating the main menu.
	 * @param menu Menu
	 * @return boolean
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	/**
	 * Preparing the menu, checking if items should be enabled or not.
	 * @param menu Menu
	 * @return boolean
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menu_reset_identity);
		if (isConnected) {
			item.setEnabled(true);
			findViewById(R.id.sendButton).setEnabled(true);
		} else {
			item.setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Calling actions when a menu item was selected.
	 * @param item MenuItem
	 * @return boolean
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_reset_identity:
				resetIdentity(null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Processes the button click and starts/stops the sending service.
	 * @param view View
	 */
	public void onButtonClick(View view) {
		if (isFineLocationPermissionGranted() && isPhoneStatePermissionGranted()) {
			// GPS enabled
			if (!checkGPSEnabled() && !BSGargoyleService.isRunning(MainActivity.this)) {
				showGPSDisabledAlertToUserAndContinue();
				return;
			}
			// Starting or stopping the service
			final Button btn = findViewById(R.id.sendButton);
			if (BSGargoyleService.isRunning(MainActivity.this)) {
				// Stopping the service
				isConnected = false;
				try {
					if (mConnection != null) {
						BSGargoyleService.stopConnection(this, mConnection);
					}
					BSGargoyleService.stop(MainActivity.this);
					btn.setEnabled(false);
					// Enable the button after a delay
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							MainActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									btn.setEnabled(true);
									mMonitor.updateWithTimeOrEvent();
								}
							});
						}
					}, 1000);
				} catch (IllegalArgumentException e) {
					e.fillInStackTrace();
				}
				// Button text
				btn.setText(getResources().getString(R.string.btn_start));
				invalidateOptionsMenu(); // menu
			} else {
				// Starting the service
				try {
					BSGargoyleService.run(MainActivity.this);
					mConnection = BSGargoyleService.startConnection(this, this);
					// Button text
					btn.setText(getResources().getString(R.string.btn_stop));
					btn.setEnabled(false);
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Cleans the log panel.
	 * @param view View
	 */
	public void clearLog(View view) {
		TextView logView = findViewById(R.id.logView);
		logView.setText("");
	}

	/**
	 * Resets the connector's identity.
	 * @param view View
	 */
	public void resetIdentity(View view) {
		if (mConnection != null) {
			mConnection.requestResetIdentity();
		}
	}

	/**
	 * Reacts to messages sent by the service.
	 * @param msg Message
	 * @return boolean (true if the event was processed)
	 */
	@Override
	public boolean onReceiveMessage(Message msg) {
		// Notifying the monitor
		if (mMonitor != null)
			mMonitor.updateWithMessage(msg);
		// Processing the event
		switch (msg.what) {
			case BSGargoyleMessage.MSG_JSON_EVENT:
				JSONObject JSON = (JSONObject)msg.obj;
				showDataFromJSON(JSON);
				return true;
			case BSGargoyleMessage.MSG_CLIENT_TAG:
				clientTag = (String)msg.obj;
				// Refresh in the info log
				if (lastBasicEvent != null) {
					try {
						showDataFromJSON(new JSONObject(lastBasicEvent));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				return true;
			case BSGargoyleMessage.MSG_CONNECTED:
				if (mConnection != null) {
					mConnection.requestCurrentData();
					mConnection.requestClientTag();
					BSGargoyleService.isProcess(this);
					isConnected = true;
					invalidateOptionsMenu(); // menu
				}
				return true;
		}
		return false;
	}

	@Override
	public void onReceiveServiceState(int state) {
		final int finalState = state;
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView textState = findViewById(R.id.textState);
				switch (finalState) {
					case BSServiceMonitor.STATE_UNKNOWN:
						textState.setText(getResources().getString(R.string.service_state_unknown));
						break;
					case BSServiceMonitor.STATE_SENDING:
						textState.setText(getResources().getString(R.string.service_state_sending));
						break;
					case BSServiceMonitor.STATE_STOPPED:
						textState.setText(getResources().getString(R.string.service_state_stopped));
						break;
					case BSServiceMonitor.STATE_STOPPED_PROCESS_ON:
						textState.setText(getResources().getString(R.string.service_state_stopped_process_on));
						break;
					case BSServiceMonitor.STATE_DEVICE_IDLE:
						textState.setText(getResources().getString(R.string.service_state_device_idle));
						break;
					case BSServiceMonitor.STATE_IDLE:
						textState.setText(getResources().getString(R.string.service_state_idle));
						break;
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mMonitor != null) {
			mMonitor.startTimer(getResources().getInteger(R.integer.monitor_timer_period_ms));
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mMonitor != null) {
			mMonitor.stopTimer();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMonitor != null) {
			mMonitor.updateWithTimeOrEvent();
		}
	}

	/**
	 * Shows data to the user from a JSON event.
	 * @param JSON JSONObject
	 */
	private void showDataFromJSON(JSONObject JSON) {
		try {
			if (JSON.has("event_type")) {
				if (JSON.getInt("event_type") == BSGargoyleEvents.BASIC_EVENT) {
					lastBasicEvent = JSON.toString();
					JSON.put("event_type", null); // no need of this info
					JSON.put("@timestamp", null); // no need of this info
					showDataInInfo(JSON);
				} else {
					// Removing unnecessary data
					JSON.put("vendor_model", null);
					JSON.put("phone_type", null);
					JSON.put("IMSI", null);
					JSON.put("IMEI", null);
					JSON.put("MSISDN", null);
					JSON.put("iccid", null);
					JSON.put("MCC_MNC", null);
					JSON.put("net_name", null);
					// Get lookups
					JSON = BSGargoyleEvents.lookupFormatter(JSON);
					// Showing
					showDataInLog(JSON);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows an event's data in the info panel.
	 * @param JSON JSONObject
	 */
	private void showDataInInfo(JSONObject JSON) {
		// Getting the view
		TextView infoView = findViewById(R.id.infoView);
		// Preparing variables
		if (infoView == null || infoView.getText() == null)
			return;
		StringBuilder onlyText = new StringBuilder();
		// Adding the client tag (SeaCat)
		if (!clientTag.equals("")) {
			onlyText.append("<b>Client Tag</b>: ");
			onlyText.append(clientTag);
			onlyText.append("<br />");
		}
		// Iterating through the JSON
		Iterator<String> iterator = JSON.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			try {
				Object value = JSON.get(key);
				onlyText.append("<b>");
				onlyText.append(key);
				onlyText.append("</b>");
				onlyText.append(": ");
				onlyText.append(value.toString());
				if (iterator.hasNext())
					onlyText.append("<br />");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// Showing the text to the user
		infoView.setText(Html.fromHtml(onlyText.toString()));
	}

	/**
	 * Shows an event's data in the log panel.
	 * @param JSON JSONObject
	 */
	private void showDataInLog(JSONObject JSON) {
		// Getting the view
		TextView logView = findViewById(R.id.logView);
		// Preparing variables
		if (logView == null || logView.getText() == null)
			return;
		String oldText = Html.toHtml((Spanned)logView.getText());
		StringBuilder newText = new StringBuilder();
		// Iterating through the JSON
		Iterator<String> iterator = JSON.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			try {
				Object value = JSON.get(key);
				newText.append("<b>");
				newText.append(key);
				newText.append("</b>");
				newText.append(": ");
				newText.append(value.toString());
				if (wasFirstJSON || iterator.hasNext())
					newText.append("<br />");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// Appending the old text
		if (!oldText.equals("") && oldText.length() > 1) {
			newText.append("<br />");
			newText.append(oldText);
		}
		// Showing the text to the user
		wasFirstJSON = true;
		logView.setText(Html.fromHtml(newText.toString()));
	}

	// GPS location --------------------------------------------------------------------------------

	/**
	 * Checks whether the GPS is enabled.
	 * @return boolean
	 */
	private boolean checkGPSEnabled() {
		LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		if (mOnlyWifiLoc) {
			return (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
		} else {
			return (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
		}
	}

	/**
	 * Shows a dialog if the GPS is not enabled, and then continues to start the service.
	 */
	private void showGPSDisabledAlertToUserAndContinue(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));
		alertDialogBuilder.setMessage(getResources().getString(R.string.gps_not_allowed))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.gps_enable_gps),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								mOnlyWifiLoc = false;
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(callGPSSettingIntent, GPS_SETTINGS_INTENT);
								dialogInterface.cancel();
							}
						})
				.setNeutralButton(getResources().getString(R.string.gps_enable_wifi), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						mOnlyWifiLoc = true;
						Intent callGPSSettingIntent = new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivityForResult(callGPSSettingIntent, GPS_SETTINGS_INTENT);
						dialogInterface.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

	/**
	 * Reacts to intent results.
	 * @param requestCode int
	 * @param resultCode int
	 * @param data Intent
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GPS_SETTINGS_INTENT) {
			// Continue here
			onButtonClick(null);
		}
	}

	// Permissions ---------------------------------------------------------------------------------

	/**
	 * Checks if it is allowed to use the access location.
	 * @return boolean
	 */
	public  boolean isFineLocationPermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Checks if it is allowed to access the phone state.
	 * @return boolean
	 */
	public  boolean isPhoneStatePermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_PERMISSION);
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Continues after the permission is obtained.
	 * @param requestCode int
	 * @param permissions @NonNull String[]
	 * @param grantResults @NonNull int[]
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			if (requestCode == ACCESS_FINE_LOCATION_PERMISSION) {
				if (isPhoneStatePermissionGranted())
					onButtonClick(null);
			} else if (requestCode == READ_PHONE_STATE_PERMISSION) {
				onButtonClick(null);
			}
		}
	}
}
