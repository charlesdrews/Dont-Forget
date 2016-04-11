package com.charlesdrews.dontforget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.charlesdrews.dontforget.birthdays.BirthdaysFragment;
import com.charlesdrews.dontforget.tasks.TaskFragment;
import com.charlesdrews.dontforget.weather.WeatherFragment;

/**
 * Bind weather, task, and birthday fragments to the ViewPager in MainActivity
 * Created by charlie on 3/28/16.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    public static final int WEATHER = 0;
    public static final int TASKS = 1;
    public static final int BIRTHDAYS = 2;
    private static final String[] PAGE_TITLES = { "Weather", "Tasks", "Birthdays" };

    SparseArray<Fragment> mActiveFragments = new SparseArray<>(3);

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mActiveFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mActiveFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case WEATHER:
                return new WeatherFragment();
            case TASKS:
                return new TaskFragment();
            case BIRTHDAYS:
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

    public Fragment getActiveFragment(int position) {
        return mActiveFragments.get(position);
    }
}
