package com.teskalabs.bsmttapp.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps and returns fragments for a ViewPager.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
	private final List<Fragment> mFragmentList = new ArrayList<>();
	private final List<String> mFragmentTitleList = new ArrayList<>();

	/**
	 * A simple constructor.
	 * @param manager FragmentManager
	 */
	public ViewPagerAdapter(FragmentManager manager) {
		super(manager);
	}

	/**
	 * Gets an item at a specified position.
	 * @param position int
	 * @return Fragment
	 */
	@Override
	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}

	/**
	 * Gets count of items/fragments.
	 * @return int
	 */
	@Override
	public int getCount() {
		return mFragmentList.size();
	}

	/**
	 * Adds a fragment to the adapter.
	 * @param fragment Fragment
	 * @param title String
	 */
	public void addFragment(Fragment fragment, String title) {
		mFragmentList.add(fragment);
		mFragmentTitleList.add(title);
	}

	/**
	 * Returns the page title.
	 * @param position int
	 * @return CharSequence
	 */
	@Override
	public CharSequence getPageTitle(int position) {
		return mFragmentTitleList.get(position);
	}
}
