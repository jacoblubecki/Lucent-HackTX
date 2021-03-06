package com.jlubecki.lucent.network.spotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jlubecki.lucent.network.spotify.models.PlaybackMeta;

import timber.log.Timber;

/**
 * Created by Jacob on 10/23/16.
 */

public class SpotifyReceiver extends BroadcastReceiver {
    public static final class BroadcastTypes {
        public static final String SPOTIFY_PACKAGE = "com.spotify.music";
        public static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        public static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        public static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    private PlaybackMeta lastData;

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is sent with all broadcasts, regardless of type. The value is taken from
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.
        long timeSentInMs = intent.getLongExtra("timeSent", 0L);

        String action = intent.getAction();

        if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
            lastData = new PlaybackMeta(intent);

            if(lastData == null) {
                Timber.e("Data error.");
            }
        }
    }

    public PlaybackMeta pollData() {
        return lastData;
    }
}
