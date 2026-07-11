package com.example.sec_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String[] TAB_TITLES = new String[]{
            "\u9996\u9875",
            "\u4e2a\u4eba\u4e2d\u5fc3"
    };

    private final ArrayList<Fragment> fragments = new ArrayList<>();

    private TabLayout tabLayout;
    public static ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        fragments.clear();
        fragments.add(new HomeFragment());
        fragments.add(new PersonalFragment());

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.setTitles(TAB_TITLES);
        viewPagerAdapter.setFragments(fragments);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Log.d(TAG, "home");
                    return;
                }

                Log.d(TAG, "personal");
                if (!Constant.landing) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    viewPager.setCurrentItem(0, false);
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
