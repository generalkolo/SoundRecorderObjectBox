package com.semanientreprise.soundrecorderbox;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyAdapter extends FragmentPagerAdapter {
    private String[] fragment_titles = {"Record","Saved Recordings"};

    public MyAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:{
                return RecordFragment.newInstance(position);
            }
            case 1:{
                return SavedRecordingsFragment.newInstance(position);
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return fragment_titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragment_titles[position];
    }
}
