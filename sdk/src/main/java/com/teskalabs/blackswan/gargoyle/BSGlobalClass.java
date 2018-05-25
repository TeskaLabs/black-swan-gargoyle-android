package com.teskalabs.blackswan.gargoyle;

import android.app.Application;

/**
 * Stores app's necessary global values.
 * @author Premysl Cerny
 */
public class BSGlobalClass extends Application {
	private boolean wasInitialized;

	public BSGlobalClass() {
		super();
		wasInitialized = false;
	}

	public boolean isWasInitialized() {
		return wasInitialized;
	}

	public void setWasInitialized(boolean wasInitialized) {
		this.wasInitialized = wasInitialized;
	}
}
