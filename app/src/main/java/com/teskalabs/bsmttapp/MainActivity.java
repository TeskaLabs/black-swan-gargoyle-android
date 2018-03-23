package com.teskalabs.bsmttapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.teskalabs.bsmtt.BSMTTelemetryService;

public class MainActivity extends AppCompatActivity {

	// Permissions
	public static int ACCESS_COARSE_LOCATION_PERMISSION = 300;
	public static int READ_PHONE_STATE_PERMISSION = 301;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Preparing the button text
		Button btn = (Button)findViewById(R.id.sendButton);
		if (BSMTTelemetryService.isRunning(this))
			btn.setText(getResources().getString(R.string.btn_stop));
	}

	/**
	 * Processes the button click and starts/stops the sending service.
	 * @param view View
	 */
	public void onButtonClick(View view) {
		if (isCoarseLocationPermissionGranted() && isPhoneStatePermissionGranted()) {
			Button btn = (Button)findViewById(R.id.sendButton);
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

	/**
	 * Checks if it is allowed to use the access location.
	 * @return boolean
	 */
	public  boolean isCoarseLocationPermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_PERMISSION);
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
			if (requestCode == ACCESS_COARSE_LOCATION_PERMISSION) {
				if (isPhoneStatePermissionGranted())
					onButtonClick(null);
			} else if (requestCode == READ_PHONE_STATE_PERMISSION) {
				onButtonClick(null);
			}
		}
	}
}
