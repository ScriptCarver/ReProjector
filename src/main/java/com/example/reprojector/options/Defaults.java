package com.example.reprojector.options;

import javafx.scene.paint.Color;

public class Defaults {
    public static final double[] PROJECTION_PROPORTIONS = new double[]{2.0,1.0};
    public static final double CENTRAL_LATITUDE = 0;
    public static final double CENTRAL_LONGITUDE = 0;
    public static final double LATITUDE_STRETCH = Math.toRadians(45);
    public static final double LONGITUDE_STRETCH = Math.toRadians(45);
    public static final double ROTATION = Math.toRadians(0);
    public static final Color FILL_COLOR = Color.WHITE;
    public static final ImageInputMethod IMAGE_INPUT_METHOD = ImageInputMethod.Equidistant;
    public static final MapProjection MAP_PROJECTION = MapProjection.EquiRectangular;
}
