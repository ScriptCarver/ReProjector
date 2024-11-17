package com.example.reprojector.globetoimagecalculations;

import com.example.reprojector.helpers.ThreadsHolder;

public class EquirectangularCalculations implements GlobeToImageCalculations {
    private static final double[] proportions = new double[]{2.0, 1.0};

    @Override
    public double[] getProportions() {
        return proportions;
    }

    public int[][][] imageCoordinates(double[][][] angleCoordinates, int height, int width) {
        double widthHelp = width - 1;
        double heightHelp = height - 1;
        int[][][] imageCoordinates = new int[angleCoordinates.length][angleCoordinates[0].length][2];
        for (int i = 0; i < angleCoordinates.length; i++) {
            double[][] rowAngles = angleCoordinates[i];
            int[][] intRow = new int[rowAngles.length][2];
            ThreadsHolder.addTask(() -> {
                for (int j = 0; j < rowAngles.length; j++) {
                    double[] pixelAngles = rowAngles[j];
                    int x = (int) Math.round(((pixelAngles[0] + Math.PI) / (2 * Math.PI)) * widthHelp);
                    int y = (int) Math.round(((pixelAngles[1] + Math.PI / 2) / (Math.PI)) * heightHelp);
                    intRow[j][0] = x;
                    intRow[j][1] = y;
                }
            });
            imageCoordinates[i] = intRow;
        }
        ThreadsHolder.waitForTasksToFinish();
        return imageCoordinates;
    }
}
