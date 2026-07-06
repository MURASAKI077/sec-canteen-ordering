package com.example.sec_android;



import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private String[] titles;
    private ArrayList<ViewPagerFragment> viewPagerFragments;
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    public void setTitles(String[] titles) {
        this.titles = titles;
    }
    public void setFragments(ArrayList<ViewPagerFragment> viewPagerFragments) {
        this.viewPagerFragments = viewPagerFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return viewPagerFragments.get(position);
    }

    @Override
    public int getCount() {
        return viewPagerFragments.size();
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}