package com.jlubecki.lucent.sensor;

import com.jlubecki.lucent.neuralnet.NeuralNetwork;

/**
 * Created by Jacob on 10/23/16.
 */

public class EEGData {
    public boolean has_power;
    public short signal_quality;
    public short attention;
    public short meditation;
    public int delta;
    public int theta;
    public int low_alpha;
    public int high_alpha;
    public int low_beta;
    public int high_beta;
    public int low_gamma;
    public int mid_gamma;
}
