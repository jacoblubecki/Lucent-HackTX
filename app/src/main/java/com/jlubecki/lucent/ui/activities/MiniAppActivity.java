package com.jlubecki.lucent.ui.activities;

import android.os.Bundle;

import com.jlubecki.lucent.R;
import com.tooleap.sdk.TooleapActivities;

/**
 * Created by Ali on 10/23/2016.
 */

public class MiniAppActivity extends TooleapActivities.Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini);
    }
}
