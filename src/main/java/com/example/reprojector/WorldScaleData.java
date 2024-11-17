package com.example.reprojector;

import com.example.reprojector.globetoimagecalculations.EquirectangularCalculations;
import com.example.reprojector.globetoimagecalculations.UVMapCalculations;
import com.example.reprojector.helpers.ImageBuilder;
import com.example.reprojector.helpers.Constants;
import com.example.reprojector.options.Defaults;
import com.example.reprojector.options.MapProjection;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class WorldScaleData {
    private static ArrayList<Pair<SingleInputController, SingleInputImage>> inputs;
    private static int projectionImageWidth;
    private static int projectionImageHeight;
    private static int uvSize;
    private static MapProjection mapProjection;
    private static Color fillColor;

    private static boolean listChanged;
    private static boolean mapProjectionChanged;
    private static boolean projectionSizeChanged;
    private static boolean fillColorChanged;
    private static boolean uvSizeChanged;

    private static BufferedImage uvMap;
    private static BufferedImage projectionImage;

    public static void init() {
        inputs = new ArrayList<>();
        listChanged = false;

        projectionImageWidth = 0;
        projectionImageHeight = 0;
        projectionSizeChanged = false;

        uvSize = 0;
        uvSizeChanged = false;

        mapProjection = Defaults.MAP_PROJECTION;
        mapProjectionChanged = false;

        fillColor = Defaults.FILL_COLOR;
        fillColorChanged = false;

        uvMap = null;
        projectionImage = null;
    }

    public static void recalculate() {
        if (!inputs.isEmpty()) {
            inputs.forEach(pair -> pair.getKey().mapAngleFieldsToParameters());
            recalculateUVSize();
            recalculateProjectionImageSizes();
        }
    }

    public static void setMapProjection(MapProjection mapProjection) {
        if (WorldScaleData.mapProjection != mapProjection) {
            WorldScaleData.mapProjection = mapProjection;
            mapProjectionChanged = true;
        }
    }

    public static SingleInputImage addNewInput(SingleInputController controller) {
        SingleInputImage singleInputImage = new SingleInputImage();
        inputs.add(new Pair<>(controller, singleInputImage));
        listChanged = true;
        return singleInputImage;
    }

    public static void setFillColor(Color fillColor){
        if(WorldScaleData.fillColor != fillColor){
            WorldScaleData.fillColor = fillColor;
            fillColorChanged = true;
        }
    }

    public static void removeInput(SingleInputController controller) {
        inputs.removeIf(pair -> pair.getKey() == controller);
        listChanged = true;
    }

    private static void recalculateUVSize() {
        OptionalInt newUVSizeHelper = inputs.stream().filter(pair -> pair.getValue().getSourceImage() != null).map(pair -> {
            BufferedImage image = pair.getValue().getSourceImage();
            return Math.min(image.getWidth(), image.getHeight()) * Constants.uvSizeMultiplier;
        }).mapToInt(Double::intValue).min();
        int newUVSize = newUVSizeHelper.isPresent() ? newUVSizeHelper.getAsInt() : 0;
        if (uvSize != newUVSize) {
            uvSize = newUVSize;
            uvSizeChanged = true;
        }
    }

    public static void recalculateProjectionImageSizes() {
        if (projectionImageWidth == 0 || projectionImageHeight == 0) {
            double[] proportions;
            switch (mapProjection) {
                case UV -> proportions = new UVMapCalculations().getProportions();
                case EquiRectangular -> proportions = new EquirectangularCalculations().getProportions();
                default -> proportions = Defaults.PROJECTION_PROPORTIONS;
            }
            if (projectionImageWidth == 0 && projectionImageHeight == 0) {
                projectionImageWidth = (int) (proportions[0] * (double) uvSize);
                projectionImageHeight = (int) (proportions[1] * (double) uvSize);
            } else if (projectionImageWidth == 0) {
                projectionImageWidth = (int) ((proportions[0] / proportions[1]) * (double) projectionImageHeight);
            } else {
                projectionImageHeight = (int) ((proportions[1] / proportions[0]) * (double) projectionImageWidth);
            }
            projectionSizeChanged = true;
        }
    }

    public static void setProjectionImageWidth(int projectionImageWidth) {
        if (WorldScaleData.projectionImageWidth != projectionImageWidth) {
            WorldScaleData.projectionImageWidth = projectionImageWidth;
            projectionSizeChanged = true;
        }
    }

    public static void setProjectionImageHeight(int projectionImageHeight) {
        if (WorldScaleData.projectionImageHeight != projectionImageHeight) {
            WorldScaleData.projectionImageHeight = projectionImageHeight;
            projectionSizeChanged = true;
        }
    }

    public static BufferedImage getUvMap() {
        if ((listChanged || uvSizeChanged || fillColorChanged) && !inputs.isEmpty()) {
            uvMap = ImageBuilder.uvImageFromList(inputs.stream().map(Pair::getValue).collect(Collectors.toCollection(ArrayList::new)), uvSize, uvSize, fillColor);
            uvSizeChanged = false;
            return uvMap;
        }
        return null;
    }

    public static BufferedImage getProjectionImage() {
        if ((listChanged || mapProjectionChanged || projectionSizeChanged || fillColorChanged) && !inputs.isEmpty()) {
            projectionImage = ImageBuilder.imageFromList(inputs.stream().map(Pair::getValue).collect(Collectors.toCollection(ArrayList::new)), projectionImageWidth, projectionImageHeight, mapProjection, fillColor);
            mapProjectionChanged = false;
            projectionSizeChanged = false;
            return projectionImage;
        }
        return projectionImage;
    }

    public static void setListChangedToTrue() {
        WorldScaleData.listChanged = true;
    }

    public static void makeListChangedFalse() {
        listChanged = false;
    }

    public static void makeFillColorChangedFalse() {
        fillColorChanged = false;
    }
}
