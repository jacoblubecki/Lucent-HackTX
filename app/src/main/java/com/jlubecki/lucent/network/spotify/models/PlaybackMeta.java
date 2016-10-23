package com.jlubecki.lucent.network.spotify.models;

import android.content.Intent;

import com.google.gson.GsonBuilder;

/**
 * Created by Jacob on 10/22/16.
 */

public class PlaybackMeta {

    public String trackId;
    public String artistName;
    public String albumName;
    public String trackName;
    public int trackLengthInSec;

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }

    public PlaybackMeta(Intent dataIntent) {
        trackId = dataIntent.getStringExtra("id");
        artistName = dataIntent.getStringExtra("artist");
        albumName = dataIntent.getStringExtra("album");
        trackName = dataIntent.getStringExtra("track");
        trackLengthInSec = dataIntent.getIntExtra("length", 0);
    }
}
