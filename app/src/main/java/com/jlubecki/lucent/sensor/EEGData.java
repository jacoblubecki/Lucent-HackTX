package com.jlubecki.lucent.sensor;

import com.jlubecki.lucent.neuralnet.NeuralNetwork;

/**
 * Created by Jacob on 10/23/16.
 */

public class EEGData implements NeuralNetwork.Trainable {
    boolean has_power;
    short signal_quality;
    short attention;
    short meditation;
    int delta;
    int theta;
    int low_alpha;
    int high_alpha;
    int low_beta;
    int high_beta;
    int low_gamma;
    int mid_gamma;

    @Override
    public double[] trainingInput() {
        return new double[] { signal_quality, attention, meditation, delta, theta, low_alpha, high_alpha, low_beta, high_beta, low_gamma, mid_gamma };
    }
}
