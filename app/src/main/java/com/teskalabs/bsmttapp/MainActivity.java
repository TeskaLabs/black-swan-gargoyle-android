package com.teskalabs.bsmttapp;

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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.teskalabs.bsmtt.BSMTTelemetryService;
import com.teskalabs.bsmtt.events.BSMTTEvents;
import com.teskalabs.bsmtt.messaging.BSMTTListener;
import com.teskalabs.bsmtt.messaging.BSMTTMessage;
import com.teskalabs.bsmtt.messaging.BSMTTServiceConnection;
import com.teskalabs.bsmttapp.fragments.FragmentInfo;
import com.teskalabs.bsmttapp.fragments.FragmentLog;
import com.teskalabs.bsmttapp.fragments.ViewPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

/**
 * Main activity class that calls the BS SDK to gather and send data.
 * @author Premysl Cerny
 */
public class MainActivity extends AppCompatActivity implements BSMTTListener {
	// GPS
	public static int GPS_SETTINS_INTENT = 200;
	// Permissions
	public static int ACCESS_FINE_LOCATION_PERMISSION = 300;
	public static int READ_PHONE_STATE_PERMISSION = 301;
	// Connection with the service
	private BSMTTServiceConnection mConnection;
	// JSON showing
	private boolean wasFirstJSON;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Default values
		mConnection = null;
		wasFirstJSON = false;

		// Preparing the button text
		Button btn = findViewById(R.id.sendButton);
		if (BSMTTelemetryService.isRunning(this)) {
			btn.setText(getResources().getString(R.string.btn_stop));
			// Register the connection
			mConnection = BSMTTelemetryService.startConnection(this, this);
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
	}

	/**
	 * Processes the button click and starts/stops the sending service.
	 * @param view View
	 */
	public void onButtonClick(View view) {
		if (isFineLocationPermissionGranted() && isPhoneStatePermissionGranted()) {
			// GPS enabled
			if (!checkGPSEnabled() && !BSMTTelemetryService.isRunning(MainActivity.this)) {
				showGPSDisabledAlertToUserAndContinue();
				return;
			}
			// Starting or stopping the service
			Button btn = findViewById(R.id.sendButton);
			if (BSMTTelemetryService.isRunning(MainActivity.this)) {
				// Stopping the service
				if (mConnection != null) {
					BSMTTelemetryService.stopConnection(this, mConnection);
				}
				BSMTTelemetryService.stop(MainActivity.this);
				// Button text
				btn.setText(getResources().getString(R.string.btn_start));
			} else {
				// Starting the service
				try {
					BSMTTelemetryService.run(MainActivity.this);
					mConnection = BSMTTelemetryService.startConnection(this, this);
					// Button text
					btn.setText(getResources().getString(R.string.btn_stop));
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
	 * Reacts to messages sent by the service.
	 * @param msg Message
	 * @return boolean
	 */
	@Override
	public boolean onReceiveMessage(Message msg) {
		// Processing the event
		switch (msg.what) {
			case BSMTTMessage.MSG_JSON_EVENT:
				JSONObject JSON = (JSONObject)msg.obj;
				showDataFromJSON(JSON);
				return true;
			case BSMTTMessage.MSG_CONNECTED:
				if (mConnection != null) {
					mConnection.requestCurrentData();
				}
				return true;
		}
		return false;
	}

	/**
	 * Shows data to the user from a JSON event.
	 * @param JSON JSONObject
	 */
	private void showDataFromJSON(JSONObject JSON) {
		try {
			if (JSON.has("event_type")) {
				if (JSON.getInt("event_type") == BSMTTEvents.BASIC_EVENT) {
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
					JSON = BSMTTEvents.lookupFormatter(JSON);
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
		StringBuilder onlyText = new StringBuilder();
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
		return (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
	}

	/**
	 * Shows a dialog if the GPS is not enabled, and then continues to start the service.
	 */
	private void showGPSDisabledAlertToUserAndContinue(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));
		alertDialogBuilder.setMessage(getResources().getString(R.string.gps_not_allowed))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.gps_enable_ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(callGPSSettingIntent, GPS_SETTINS_INTENT);
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
		if (requestCode == GPS_SETTINS_INTENT) {
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
