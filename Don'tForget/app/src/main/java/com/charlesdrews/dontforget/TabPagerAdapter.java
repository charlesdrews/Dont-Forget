package com.charlesdrews.dontforget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.charlesdrews.dontforget.birthdays.BirthdaysFragment;
import com.charlesdrews.dontforget.tasks.TaskFragment;
import com.charlesdrews.dontforget.weather.WeatherFragment;

/**
 * Created by charlie on 3/28/16.
 */
public class TabPagerAdapter extends FragmentPagerAdapter {
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
        return PAGE_TITLES.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PAGE_TITLES[position];
    }
}
