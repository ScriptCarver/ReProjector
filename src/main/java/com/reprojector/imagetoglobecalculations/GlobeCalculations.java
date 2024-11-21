package com.reprojector.imagetoglobecalculations;

import java.awt.image.BufferedImage;

public abstract class GlobeCalculations implements InputToGlobeCalculations{
    int width;
    int height;
    double latitudeStretch;
    double centralLongitude;
    double centralLatitude;
    double rotation;

    public GlobeCalculations(BufferedImage image, double latitudeStretch, double centralLatitude, double centralLongitude, double rotation) {
        width = image.getWidth();
        height = image.getHeight();
        this.latitudeStretch = latitudeStretch;
        this.centralLatitude = centralLatitude;
        this.centralLongitude = centralLongitude;
        this.rotation = rotation;
    }

    public double[][][] angleCoordinates() {
        return angleCoordinates(0, height);
    }

    public abstract double[][][] angleCoordinates(int startJ, int step);

    protected double rotateLongitude(double longitude) {
        longitude += centralLongitude;
        if (Math.abs(longitude) > Math.PI) {
            if (longitude < -Math.PI) {
                longitude += 2 * Math.PI;
            } else {
                longitude -= 2 * Math.PI;
            }
        }
        return longitude;
    }

    protected void rotateLatitude(double x, double z){
        double xszs = x * x + z * z;
        double d = Math.sqrt(xszs);
        double angle = Math.atan2(z, x);
        angle -= centralLatitude;
        x = d * Math.cos(angle);
        z = d * Math.sin(angle);
    }
}
