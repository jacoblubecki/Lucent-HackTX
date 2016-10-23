package com.jlubecki.lucent.neuralnet;

/**
 * Created by Jacob on 10/23/16.
 */

public final class CostFunction {

    /**
     * EEG measures focused state from 0-100. Targeting 85 for now.
     */
    private static final double DESIRED_OUTPUT = 85;

    public static final double[] evaluate(double[] output) {
        double[] errorOut = new double[output.length];

        for(int i = 0; i < output.length; i++) {
            errorOut[i] = Math.pow(output[i] - DESIRED_OUTPUT, 2);
        }

        return errorOut;
    }
}
