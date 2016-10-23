package com.jlubecki.lucent.neuralnet;

import timber.log.Timber;

/**
 * Created by Jacob on 10/22/16.
 */

public class NeuralNetwork {

    private static final double LEARNING_RATE = 0.01;

    private static final int INPUT_LAYER_SIZE = 24;
    private static final int HIDDEN_LAYER_SIZE = 42;
    private static final int OUTPUT_LAYER_SIZE = 1;

    private int cycleIndex = 0;
    private final int maxIterationsPerCycle = 25;

    private final Neuron[] inputLayer;
    private final Neuron[] hiddenLayer;
    private final Neuron[] outputLayer;

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
    }

    public NeuralNetwork(Neuron[] in, Neuron[] hid, Neuron[] out) {
        this.inputLayer = in;
        this.hiddenLayer = hid;
        this.outputLayer = out;
    }

    public void train(Trainable input) {
        double[] rawInput = input.trainingInput();
        double[][] trainingData = new double[rawInput.length][1];

        for (int i = 0; i < rawInput.length; i++) {
            trainingData[i][1] = rawInput[i];
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

        double[] errorFinal = CostFunction.evaluate(outLayerFinal);
        this.currentErr = errorFinal[0];
        for(int i = 0; i < hiddenLayer.length; i++) {
            hiddenLayer[i].adjust(LEARNING_RATE, errorFinal);
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

    public interface Trainable {
        double[] trainingInput();
    }
}
