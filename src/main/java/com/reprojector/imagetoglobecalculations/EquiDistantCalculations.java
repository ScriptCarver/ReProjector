package com.reprojector.imagetoglobecalculations;

import com.reprojector.helpers.ThreadsHolder;

import java.awt.image.BufferedImage;

public class EquiDistantCalculations extends GlobeCalculations {

    public EquiDistantCalculations(BufferedImage image, double latitudeStretch, double centralLatitude, double centralLongitude, double rotation) {
        super(image, latitudeStretch, centralLatitude, centralLongitude, rotation);
    }

    public double[][][] angleCoordinates(int startJ, int step) {
        double oC = 2 * Math.PI / latitudeStretch * (double) height;
        double globeR = oC / 2 / Math.PI;

        double middleX = (double) width / 2;
        middleX = middleX - 0.5;
        double middleY = (double) height / 2;
        middleY = middleY - 0.5;

        double centerY = 0;
        double centerZ = 0;

        double[][][] imageAngles = new double[Math.min(step, height)][width][2];

        for (double j = startJ; j < Math.min((startJ + step), height); j++) {
            double finalMiddleY = middleY;
            double finalMiddleX = middleX;
            double finalJ = j;
            double[][] row = new double[width][2];
            ThreadsHolder.addTask(() -> {
                for (double i = 0; i < width; i++) {
                    double imageY = i - finalMiddleX;
                    double imageZ = finalJ - finalMiddleY;
                    double imageDist = Math.sqrt(Math.pow((imageY - centerY), 2)
                            + Math.pow((imageZ - centerZ), 2));
                    double horizontalAngle = 2 * Math.PI * imageDist / oC;
                    horizontalAngle = imageY < centerY ?
                            -horizontalAngle :
                            horizontalAngle;

                    double imageAngle = Math.atan2(imageZ - centerZ, imageY - centerY) + rotation;

                    double x = Math.cos(horizontalAngle) * globeR;
                    double initY = Math.sin(horizontalAngle) * globeR;

                    double dist2 = Math.abs(initY);
                    double y = dist2 * Math.cos(imageAngle);
                    double z = dist2 * Math.sin(imageAngle);

                    //Rotate Latitude
                    rotateLatitude(x, z);

                    double longitude = rotateLongitude(Math.atan2(y, x));
                    double latitude = Math.atan(z / Math.sqrt(x * x + y * y));

                    row[(int) i][0] = longitude;
                    row[(int) i][1] = latitude;
                }
            });
            imageAngles[(int) j - startJ] = row;
        }
        ThreadsHolder.waitForTasksToFinish();

        return imageAngles;
    }
}
