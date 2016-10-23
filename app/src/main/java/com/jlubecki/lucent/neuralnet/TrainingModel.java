package com.jlubecki.lucent.neuralnet;

import com.jlubecki.lucent.network.spotify.models.TrackAudioFeatures;
import com.jlubecki.lucent.sensor.EEGData;

/**
 * Created by Jacob on 10/23/16.
 */

public class TrainingModel implements NeuralNetwork.Trainable {

    private final EEGData eegData;
    private final TrackAudioFeatures audioFeatures;

    public TrainingModel(EEGData eeg, TrackAudioFeatures audioFeatures) {
        this.eegData = eeg;
        this.audioFeatures = audioFeatures;
    }

    @Override
    public double[] trainingInput() {
        return new double[] {
                eegData.delta,
                eegData.theta,
                eegData.low_alpha,
                eegData.high_alpha,
                eegData.low_beta,
                eegData.high_beta,
                eegData.low_gamma,
                eegData.mid_gamma,
                audioFeatures.danceability,
                audioFeatures.energy,
                audioFeatures.key,
                audioFeatures.loudness,
                audioFeatures.mode,
                audioFeatures.speechiness,
                audioFeatures.acousticness,
                audioFeatures.instrumentalness,
                audioFeatures.liveness,
                audioFeatures.valence,
                audioFeatures.tempo,
                audioFeatures.timeSignature,

        };
    }

    @Override
    public double targetValue() {
        return eegData.meditation;
    }
}
