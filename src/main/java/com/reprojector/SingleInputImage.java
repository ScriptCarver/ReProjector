package com.reprojector;

import com.reprojector.helpers.Constants;
import com.reprojector.imagetoglobecalculations.CastCalculations;
import com.reprojector.imagetoglobecalculations.EquiDistantCalculations;
import com.reprojector.imagetoglobecalculations.InputToGlobeCalculations;
import com.reprojector.options.Defaults;
import com.reprojector.options.ImageInputMethod;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SingleInputImage {
    //Position
    private double centralLatitude;
    private double centralLongitude;
    //Rotation
    private double rotation;
    //Size
    private double latitudeStretch;
    private double longitudeStretch;
    //Input
    private ImageInputMethod imageInputMethod;
    private BufferedImage sourceImage;
    //Modified image
    private BufferedImage resizedImage;

    public SingleInputImage() {
        centralLatitude = Defaults.CENTRAL_LATITUDE;
        centralLongitude = Defaults.CENTRAL_LONGITUDE;
        latitudeStretch = Defaults.LATITUDE_STRETCH;
        longitudeStretch = Defaults.LONGITUDE_STRETCH;
        imageInputMethod = Defaults.IMAGE_INPUT_METHOD;
        rotation = Defaults.ROTATION;
        sourceImage = null;
        resizedImage = null;
    }

    public void recalculate() {
        resizeInputImage();
    }

    public void dropUnneededVariables() {
        resizedImage = null;
        Runtime.getRuntime().gc();
    }

    public void recalculateStretches(double latitudeStretch, double longitudeStretch) {
        if (latitudeStretch == 0 || longitudeStretch == 0) {
            if (latitudeStretch == 0 && longitudeStretch == 0) {
                latitudeStretch = Defaults.LATITUDE_STRETCH;
                longitudeStretch = Defaults.LONGITUDE_STRETCH;
            } else if (longitudeStretch == 0) {
                longitudeStretch = latitudeStretch / sourceImage.getHeight() * sourceImage.getWidth();
            } else {
                latitudeStretch = longitudeStretch / sourceImage.getWidth() * sourceImage.getHeight();
            }
        }
        if (this.latitudeStretch != latitudeStretch
                || this.longitudeStretch != longitudeStretch) {
            this.latitudeStretch = latitudeStretch;
            this.longitudeStretch = longitudeStretch;
            WorldScaleData.setListChangedToTrue();
        }
    }

    public void setCentralLatitude(double centralLatitude) {
        if (this.centralLatitude != centralLatitude) {
            this.centralLatitude = centralLatitude;
            WorldScaleData.setListChangedToTrue();
        }
    }

    public void setCentralLongitude(double centralLongitude) {
        if (this.centralLongitude != centralLongitude) {
            this.centralLongitude = centralLongitude;
            WorldScaleData.setListChangedToTrue();
        }
    }

    public void setAngleStretch(double latitudeStretch, double longitudeStretch) {
        recalculateStretches(latitudeStretch, longitudeStretch);
    }

    public void setRotation(double rotation){
        if(this.rotation != rotation){
            this.rotation = rotation;
            WorldScaleData.setListChangedToTrue();
        }
    }

    public void setImageInputMethod(ImageInputMethod imageInputMethod) {
        if (this.imageInputMethod != imageInputMethod) {
            this.imageInputMethod = imageInputMethod;
        }
    }

    public BufferedImage getSourceImage() {
        return sourceImage;
    }

    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    private void resizeInputImage() {
        double latProp = (double) sourceImage.getHeight() / latitudeStretch;
        double longProp = (double) sourceImage.getWidth() / longitudeStretch;
        int finalHeight;
        int finalWidth;
        if (latProp < longProp) {
            finalHeight = sourceImage.getHeight();
            finalWidth = (int) (latProp * longitudeStretch);
        } else {
            finalWidth = sourceImage.getWidth();
            finalHeight = (int) (longProp * latitudeStretch);
        }

        double latMultiplayer = getLatitudeMultiplier();
        double multiplayerLimit = Math.sqrt((double) (Integer.MAX_VALUE / (finalHeight * finalWidth * 4)));
        latMultiplayer = Math.min(latMultiplayer, multiplayerLimit);

        finalWidth *= latMultiplayer;
        finalHeight *= latMultiplayer;

        Image temp = sourceImage.getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(finalWidth, finalHeight, sourceImage.getType());

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();

        this.resizedImage = resizedImage;
    }

    public BufferedImage getResizedImage() {
        return resizedImage;
    }

    private double getLatitudeMultiplier() {
        double sourceLat;
        double maxLat = centralLatitude + latitudeStretch / 2;
        double minLat = centralLatitude - latitudeStretch / 2;
        boolean poleBetween = (minLat <= Math.PI / 2 && Math.PI / 2 < maxLat)
                || (minLat <= -Math.PI / 2 && -Math.PI / 2 < maxLat);
        if (poleBetween) {
            sourceLat = Math.PI / 2;
        } else {
            sourceLat = Math.PI / 2 - Math.min(Math.min((Math.abs(maxLat) - Math.PI / 2), (Math.abs(minLat) - Math.PI / 2)),
                    Math.min(Math.PI / 2 - (Math.abs(maxLat)), Math.PI / 2 - (Math.abs(minLat) - Math.PI / 2)));
        }
        return Math.max(Constants.multiplierOnPole * Math.sin(sourceLat), 1);
    }

    public InputToGlobeCalculations getInputToGlobeCalculations() {
        switch (imageInputMethod) {
            case Cast:
                return new CastCalculations(resizedImage, latitudeStretch, centralLatitude, centralLongitude, rotation);
            default:
                return new EquiDistantCalculations(resizedImage, latitudeStretch, centralLatitude, centralLongitude, rotation);
        }
    }
}
