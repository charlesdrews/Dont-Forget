package com.charlesdrews.dontforget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by charlie on 3/28/16.
 */
public class TabPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_PAGES = 3;
    private static final String[] PAGE_TITLES = { "Weather", "Tasks", "Birthdays" };

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new WeatherFragment();
            case 1:
                return new TaskFragment();
            case 2:
                return new BirthdaysFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PAGE_TITLES[position];
    }
}
