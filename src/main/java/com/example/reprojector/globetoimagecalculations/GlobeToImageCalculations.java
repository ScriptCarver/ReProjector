package com.example.reprojector.globetoimagecalculations;

public interface GlobeToImageCalculations {
    double[] getProportions();
    int[][][] imageCoordinates(double[][][] angleCoordinates, int height, int width);
}
