package com.jlubecki.lucent.network.spotify.models;

import com.jlubecki.lucent.neuralnet.NeuralNetwork;

/**
 * Created by Jacob on 10/23/16.
 */

public class TrackAudioFeatures implements NeuralNetwork.Trainable {
    public double danceability;
    public double energy;
    public int key;
    public double loudness;
    public int mode;
    public double speechiness;
    public double acousticness;
    public double instrumentalness;
    public double liveness;
    public double valence;
    public double tempo;
    public String type;
    public String id;
    public String uri;
    public String trackHref;
    public String analysisUrl;
    public int durationMs;
    public int timeSignature;

    @Override
    public double[] trainingInput() {
        return new double[] {danceability, energy, key, loudness, mode, speechiness, acousticness, instrumentalness, liveness, valence, tempo, durationMs, timeSignature};
    }
}
