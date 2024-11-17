package com.example.reprojector.globetoimagecalculations;

import com.example.reprojector.helpers.ThreadsHolder;

public class UVMapCalculations implements GlobeToImageCalculations {
    private static final double[] proportions = new double[]{1,1};

    public double[] getProportions() {
        return proportions;
    }

    public int[][][] imageCoordinates(double[][][] angleCoordinates, int height, int width) {
        int[][][] intCoordinates = new int[angleCoordinates.length][angleCoordinates[0].length][2];
        for (int i = 0; i < angleCoordinates.length; i++) {
            double[][] rowOfAngles = angleCoordinates[i];
            int[][] row = new int[rowOfAngles.length][rowOfAngles[0].length];
            ThreadsHolder.addTask(() -> {
                for (int j = 0; j < rowOfAngles.length; j++) {
                    double[] pixelAngleCoordinates = rowOfAngles[j];
                    double latitude = pixelAngleCoordinates[1] - Math.PI / 2; //Offset latitude from "equator"
                    double longitude = pixelAngleCoordinates[0] + Math.PI;  //Offset longitude from "prime meridian"

                    double sphericalX = Math.sin(latitude) * Math.cos(longitude);
                    double sphericalY = Math.sin(latitude) * Math.sin(longitude);
                    double sphericalZ = Math.cos(latitude);

                    double u = (Math.atan2(sphericalY, sphericalX) / (2 * Math.PI)) + 0.5;
                    double v = sphericalZ * 0.5 + 0.5;
                    u = Math.round(u * ((double) height - 1));
                    v = Math.round(v * ((double) width - 1));
                    row[j][0] = (int) u;
                    row[j][1] = (int) v;
                }
            });
            intCoordinates[i] = row;
        }
        ThreadsHolder.waitForTasksToFinish();
        return intCoordinates;
    }


}
