package com.teskalabs.bsmttapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.teskalabs.bsmtt.BSMTTelemetryService;

/**
 * Main activity class that calls the BS SDK to gather and send data.
 * @author Premysl Cerny
 */
public class MainActivity extends AppCompatActivity {
	// GPS
	public static int GPS_SETTINS_INTENT = 200;
	// Permissions
	public static int ACCESS_FINE_LOCATION_PERMISSION = 300;
	public static int READ_PHONE_STATE_PERMISSION = 301;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Preparing the button text
		Button btn = findViewById(R.id.sendButton);
		if (BSMTTelemetryService.isRunning(this))
			btn.setText(getResources().getString(R.string.btn_stop));
	}

	/**
	 * Processes the button click and starts/stops the sending service.
	 * @param view View
	 */
	public void onButtonClick(View view) {
		if (isFineLocationPermissionGranted() && isPhoneStatePermissionGranted()) {
			// GPS enabled
			if (!checkGPSEnabled() && !BSMTTelemetryService.isRunning(this)) {
				showGPSDisabledAlertToUserAndContinue();
				return;
			}
			// Starting or stopping the service
			Button btn = findViewById(R.id.sendButton);
			if (BSMTTelemetryService.isRunning(this)) {
				// Stopping the service
				BSMTTelemetryService.stop(this);
				// Button text
				btn.setText(getResources().getString(R.string.btn_start));
			} else {
				// Starting the service
				try {
					BSMTTelemetryService.run(this);
					// Button text
					btn.setText(getResources().getString(R.string.btn_stop));
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		}
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
