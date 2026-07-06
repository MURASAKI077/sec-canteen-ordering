package com.example.sec_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private String[] mTitles = new String[]{"菜单首页", "个人中心"};
    //把viewpager放入到集合中
    private ArrayList<ViewPagerFragment> mViewPagerFragments = new ArrayList<>();



    private TabLayout tabLayout;
    public static ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart(){
        super.onStart();
        initView();
    }




    private void initData(){

    }

    private void initView(){
        mViewPagerFragments.clear();
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.view_pager);
        for (int i = 0; i < mTitles.length; i++) {
            mViewPagerFragments.add(ViewPagerFragment.newInstance(mTitles[i]));
        }
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.setTitles(mTitles);
        viewPagerAdapter.setFragments(mViewPagerFragments);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        Log.d(TAG, "菜单首页");
                        break;
                    case 1:
                        Log.d(TAG, "个人中心");
                        if(!Constant.landing){
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }

                        break;
                    default:
                        break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}