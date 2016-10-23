package com.jlubecki.lucent.ui;

import android.app.Application;
import android.support.compat.BuildConfig;

import timber.log.Timber;

/**
 * Created by Jacob on 10/23/16.
 */

public class LucentApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
