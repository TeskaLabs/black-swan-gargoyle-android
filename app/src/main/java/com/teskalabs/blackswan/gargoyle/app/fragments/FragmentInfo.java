package com.teskalabs.blackswan.gargoyle.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teskalabs.blackswan.R;

/**
 * A simple fragment for showing info about the phone.
 * @author Premysl Cerny
 */
public class FragmentInfo extends Fragment {
	/**
	 * Basic constructor.
	 */
	public FragmentInfo() {

	}

	/**
	 * A basic onCreate method.
	 * @param savedInstanceState Bundle
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * Creating a specified view.
	 * @param inflater LayoutInflater
	 * @param container ViewGroup
	 * @param savedInstanceState Bundle
	 * @return View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_info, container, false);
	}
}
