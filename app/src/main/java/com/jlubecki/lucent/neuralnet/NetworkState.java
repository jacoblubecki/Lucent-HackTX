package com.jlubecki.lucent.neuralnet;

import com.google.gson.GsonBuilder;

/**
 * Created by Jacob on 10/23/16.
 */

public class NetworkState {
    public Neuron[] inputLayer;
    public Neuron[] hiddenLayer;
    public Neuron[] outputLayer;

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
