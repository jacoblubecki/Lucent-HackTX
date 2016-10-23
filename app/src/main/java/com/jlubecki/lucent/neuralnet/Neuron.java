package com.jlubecki.lucent.neuralnet;

import com.google.gson.GsonBuilder;

import java.util.Arrays;

/**
 * Created by Jacob on 10/23/16.
 */

public class Neuron {

    private double[] weights;
    private double[] biases;

    public double[] outCalc;
    private double[] outCalcDiff;
    public double[] outCalcDelta;
    private double[] lastInput;
    public double output;

    public Neuron() {
        this(genSeed(3.0, 1), genSeed(3.0, 1));
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }

    public Neuron(double[] weights, double[] biases) {
        this.weights = weights;
        this.biases = biases;

        outCalc = new double[weights.length];
        outCalcDiff = new double[weights.length];
        outCalcDelta = new double[weights.length];

        if(weights.length != biases.length) {
            throw new RuntimeException("Sizes must be equal.");
        }
    }

    public double evaluate(double[] inputs) {
        lastInput = inputs;

        double sum = 0;

        for(int i = 0; i < inputs.length; i++) {
            double calcI = calcSigmoid(weights[i], inputs[i], biases[i]);
            sum += calcI;

            outCalc[i] = calcI;
            outCalcDiff[i] = calcSigmoidDerivative(calcI);
        }

        output = sum;

        return output;
    }

    public void adjust(double learningRate, double[] propagatedError) {
        for(int i = 0; i < weights.length; i++) {
            double err = propagatedError[i];
            outCalcDelta[i] = err * outCalcDiff[i];

            weights[i] -= learningRate * outCalcDelta[i] * lastInput[i];
            biases[i] -= learningRate * outCalcDelta[i] * lastInput[i];
        }
    }

    private double calcSigmoid(double w, double x, double b) {
        return 1 / (1 + Math.exp(-(w * x + b)));
    }

    private double calcSigmoidDerivative(double x) {
        return x * (1 - x);
    }

    static double[] genSeed(double seedLimit, int seedCount) {
        double[] seeds = new double[seedCount];

        for(int i = 0; i < seedCount; i++) {
            seeds[i] = seedLimit * (-1 + 2 * Math.random());
        }

        return seeds;
    }
}
