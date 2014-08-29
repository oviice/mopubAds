package com.mopub.simpleadsdemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class MoPubSampleActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            return;
        }

        if (findViewById(R.id.fragment_container) != null) {
            final MoPubListFragment listFragment = new MoPubListFragment();
            listFragment.setArguments(getIntent().getExtras());
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, listFragment)
                    .commit();
        }
    }
}
