package com.example.reprojector.imagetoglobecalculations;

import com.example.reprojector.helpers.ThreadsHolder;

import java.awt.image.BufferedImage;

public class CastCalculations extends GlobeCalculations {

    public CastCalculations(BufferedImage image, double latitudeStretch, double centralLatitude, double centralLongitude, double rotation) {
        super(image, latitudeStretch, centralLatitude, centralLongitude, rotation);
    }

    public double[][][] angleCoordinates(int startJ, int step) {
        double oC = 2 * Math.PI / latitudeStretch * (double) height;
        double globeR = oC / 2 / Math.PI;
        double globeRSqr = Math.pow(globeR, 2);

        double halfX = (double) width / 2;
        halfX = halfX - 0.5;
        double halfY = (double) height / 2;
        halfY = halfY - 0.5;

        double[][][] imageAngles = new double[Math.min(step, height)][width][2];
        for (double j = startJ; j < Math.min((startJ + step), height); j++) {
            double finalHalfY = halfY;
            double finalHalfX = halfX;
            double finalJ = j;
            double[][] row = new double[width][2];
            ThreadsHolder.addTask(() -> {
                for (double i = 0; i < width; i++) {
                    double x = 1;
                    double z = finalJ - finalHalfY;
                    double y = i - finalHalfX;

                    //wrap coordinates around sphere
                    while ((y * y) + (z * z) > globeRSqr) {
                        if (z * z > globeRSqr) {
                            z = z < 0 ? -globeR - (z + globeR) :
                                    globeR - (z - globeR);
                            x *= -1;
                        }
                        double r = Math.sqrt(globeRSqr - (z * z));
                        if ((y * y) > (r * r)) {
                            int mult = (int) Math.abs(Math.floor(y / r));
                            double dif = Math.abs(y) - mult * r;
                            int help = mult % 4;
                            switch (help) {
                                case 0 -> y = y < 0 ? -dif : dif;
                                case 1 -> {
                                    y = y < 0 ? -1 * r - (y + r) :
                                            r - (y - r);
                                    x *= -1;
                                }
                                case 2 -> {
                                    y = y < 0 ? dif : -dif;
                                    x *= -1;
                                }
                                case 3 -> y = y < 0 ? r - dif : -r + dif;
                            }
                        }
                    }

                    x = x * Math.sqrt(globeRSqr - ((y * y) + (z * z)));

                    double imageAngle = Math.atan2(z, x);
                    imageAngle += rotation;
                    double dist = Math.sqrt(x * x + z * z);
                    x = Math.sin(imageAngle) * dist;
                    z = Math.cos(imageAngle) * dist;

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
