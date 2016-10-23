package com.jlubecki.lucent.neuralnet;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by Jacob on 10/22/16.
 */

public class NeuralNetwork {

    public static NeuralNetwork instance;

    private static final double LEARNING_RATE = 0.001;

    private static final int INPUT_LAYER_SIZE = 20;
    private static final int HIDDEN_LAYER_SIZE = 42;
    private static final int OUTPUT_LAYER_SIZE = 1;

    private int cycleIndex = 0;
    private final int maxIterationsPerCycle = 25;

    public final Neuron[] inputLayer;
    public final Neuron[] hiddenLayer;
    public final Neuron[] outputLayer;

    private double currentErr;

    public NeuralNetwork() {
        inputLayer = new Neuron[INPUT_LAYER_SIZE];
        hiddenLayer = new Neuron[HIDDEN_LAYER_SIZE];
        outputLayer = new Neuron[OUTPUT_LAYER_SIZE];

        for (int i = 0; i < inputLayer.length; i++) {
            inputLayer[i] = new Neuron();
        }

        for (int i = 0; i < hiddenLayer.length; i++) {
            double[] seedHLWeight = Neuron.genSeed(3, INPUT_LAYER_SIZE);
            double[] seedHLBias = Neuron.genSeed(3, INPUT_LAYER_SIZE);

            hiddenLayer[i] = new Neuron(seedHLWeight, seedHLBias);
        }

        for (int i = 0; i < outputLayer.length; i++) {
            double[] seedOutputWeight = Neuron.genSeed(3, HIDDEN_LAYER_SIZE);
            double[] seedOuputBias = Neuron.genSeed(3, HIDDEN_LAYER_SIZE);

            outputLayer[i] = new Neuron(seedOutputWeight, seedOuputBias);
        }

        instance = this;
    }

    public NeuralNetwork(Neuron[] in, Neuron[] hid, Neuron[] out) {
        this.inputLayer = in;
        this.hiddenLayer = hid;
        this.outputLayer = out;

        instance = this;
    }

    public void train(Trainable input) {
        double[] rawInput = input.trainingInput();
        double[][] trainingData = new double[rawInput.length][1];

        for (int i = 0; i < rawInput.length; i++) {
            trainingData[i][0] = rawInput[i];
        }

        double[] outLayer1 = new double[inputLayer.length];
        for (int i = 0; i < inputLayer.length; i++) {
            // Evaluate each training input for the input layer
            outLayer1[i] = inputLayer[i].evaluate(trainingData[i]);
        }

        double[] outLayer2 = new double[hiddenLayer.length];
        for(int i = 0; i < hiddenLayer.length; i++) {
            outLayer2[i] = hiddenLayer[i].evaluate(outLayer1);
        }

        double[] outLayerFinal = new double[outputLayer.length];
        for(int i = 0; i < outputLayer.length; i++) {
            outLayerFinal[i] = outputLayer[i].evaluate(outLayer2);
        }

        double[] errorFinal = CostFunction.evaluate(outLayerFinal, input.targetValue());
        double[] errorFinalForMapping = new double[hiddenLayer.length];

        for(int i = 0; i < errorFinalForMapping.length; i++) {
            errorFinalForMapping[i] = errorFinal[0];
        }

        this.currentErr = errorFinal[0];
        for(int i = 0; i < hiddenLayer.length; i++) {
            hiddenLayer[i].adjust(LEARNING_RATE, errorFinalForMapping);
        }

        for(int i = 0; i < inputLayer.length; i++) {
            for(int j = 0; j < hiddenLayer.length; j++) {
                inputLayer[i].adjust(LEARNING_RATE, hiddenLayer[j].outCalcDelta);
            }
        }

        cycleIndex++;

        if(cycleIndex % maxIterationsPerCycle == 0) {
            Timber.i("Neural net error evaluated to %d.", currentErr);
            cycleIndex = 0;
        }
    }

    public void print(final Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context, "Err: " + currentErr, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface Trainable {
        double[] trainingInput();
        double targetValue();
    }

    @Override
    public String toString() {
        NetworkState state = new NetworkState();
        state.inputLayer = inputLayer;
        state.hiddenLayer = hiddenLayer;
        state.outputLayer = outputLayer;

        return state.toString();
    }
}
