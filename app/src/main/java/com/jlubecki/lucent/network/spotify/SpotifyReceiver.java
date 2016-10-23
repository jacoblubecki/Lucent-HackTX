package com.jlubecki.lucent.network.spotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

/**
 * Created by Jacob on 10/23/16.
 */

public class SpotifyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.i(intent.getDataString());
    }
}
