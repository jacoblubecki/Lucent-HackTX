package com.jlubecki.lucent.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.GsonBuilder;
import com.jlubecki.lucent.network.spotify.SpotifyReceiver;
import com.jlubecki.lucent.network.spotify.models.PlaybackMeta;

import timber.log.Timber;

/**
 * Created by Jacob on 10/23/16.
 */

public class EEGReceiver extends BroadcastReceiver {

    public static final String ACTION_EEG = "com.jlubecki.lucent.sensor.ACTION_EEG";
    public static final String EXTRA_SENSOR_JSON = "com.jlubecki.lucent.sensor.EXTRA_JSON";

    private EEGData lastData;

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is sent with all broadcasts, regardless of type. The value is taken from
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.
        long timeSentInMs = intent.getLongExtra("timeSent", 0L);

        String action = intent.getAction();

        if (action.equals(ACTION_EEG)) {
            lastData = new GsonBuilder().create().fromJson(intent.getStringExtra(EXTRA_SENSOR_JSON), EEGData.class);

            if(lastData == null) {
                Timber.e("Null EEG data.");
            }
        }
    }

    public EEGData pollData() {
        return lastData;
    }
}
