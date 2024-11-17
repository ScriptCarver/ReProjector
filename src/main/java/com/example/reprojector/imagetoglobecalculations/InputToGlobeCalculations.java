package com.example.reprojector.imagetoglobecalculations;

public interface InputToGlobeCalculations {

    double[][][] angleCoordinates();
    double[][][] angleCoordinates(int start, int step);
}
