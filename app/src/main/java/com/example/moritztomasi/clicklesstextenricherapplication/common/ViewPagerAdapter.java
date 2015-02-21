package com.example.moritztomasi.clicklesstextenricherapplication.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.moritztomasi.clicklesstextenricherapplication.CameraTab;
import com.example.moritztomasi.clicklesstextenricherapplication.GalleryTab;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence[] titles;
    private int numTabs;

    public ViewPagerAdapter(FragmentManager fm, CharSequence[] titles, int numTabs) {
        super(fm);

        this.titles = titles;
        this.numTabs = numTabs;
    }

    @Override
    public Fragment getItem(int i) {

        switch(i) {
            case 0:
                return new CameraTab();
            case 1:
                return new GalleryTab();
        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return this.titles[position];
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
