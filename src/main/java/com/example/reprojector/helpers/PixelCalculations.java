package com.example.reprojector.helpers;

import javafx.scene.paint.Color;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PixelCalculations {

    public static int[] averagePixels(int targetWidth, int targetHeight,
                                      ArrayList<int[][][]> intCoordinates,
                                      ArrayList<int[]> originalPixels, ArrayList<Boolean> hasAlphaChanel) {

        return averagePixels(targetWidth, targetHeight,
                intCoordinates, originalPixels,
                hasAlphaChanel, Color.TRANSPARENT);
    }

    public static int[] averagePixels(int targetWidth, int targetHeight,
                                      int[][][] intCoordinates,
                                      int[] originalPixels, boolean hasAlphaChanel,
                                      Color fillColor) {
        System.out.println("Averaging:");
        Instant start = Instant.now();
        AtomicLong[] newPixelsAtomic = new AtomicLong[targetWidth * targetHeight * 4]; // Sum of color and alpha channels from original pixels
        AtomicInteger[] counts = new AtomicInteger[targetWidth * targetHeight * 4]; // Number of original pixels being part of new pixel
        for (int i = 0; i < newPixelsAtomic.length; i++) {
            newPixelsAtomic[i] = new AtomicLong(0);
            counts[i] = new AtomicInteger(0);
        }

        for (int i = 0; i < intCoordinates.length; i++) {
            int finalI = i;
            int[][] helpRow = intCoordinates[i];
            ThreadsHolder.addTask(() -> {
                for (int j = 0; j < intCoordinates[finalI].length; j++) {
                    int[] coordinates = helpRow[j];
                    int newPixelRedIndex = (coordinates[1] * targetWidth + coordinates[0]) * 4;
                    int newPixelGreenIndex = newPixelRedIndex + 1;
                    int newPixelBlueIndex = newPixelGreenIndex + 1;
                    int newPixelAlphaIndex = newPixelBlueIndex + 1;

                    int originalPixelRedIndex = hasAlphaChanel ? 4 * (helpRow.length * finalI + j) :
                            3 * (helpRow.length * finalI + j);
                    int originalPixelGreenIndex = originalPixelRedIndex + 1;
                    int originalPixelBlueIndex = originalPixelGreenIndex + 1;
                    int originalPixelAlphaIndex = originalPixelBlueIndex + 1;

                    newPixelsAtomic[newPixelRedIndex].addAndGet(originalPixels[originalPixelRedIndex]);
                    newPixelsAtomic[newPixelGreenIndex].addAndGet(originalPixels[originalPixelGreenIndex]);
                    newPixelsAtomic[newPixelBlueIndex].addAndGet(originalPixels[originalPixelBlueIndex]);

                    counts[newPixelRedIndex].addAndGet(1);
                    counts[newPixelGreenIndex].addAndGet(1);
                    counts[newPixelBlueIndex].addAndGet(1);

                    if (hasAlphaChanel) {
                        newPixelsAtomic[newPixelAlphaIndex].addAndGet(originalPixels[originalPixelAlphaIndex]);
                        counts[newPixelAlphaIndex].addAndGet(1);
                    } else if (newPixelsAtomic[newPixelAlphaIndex].get() == 0) {
                        newPixelsAtomic[newPixelAlphaIndex].set(255);
                        counts[newPixelAlphaIndex].set(1);
                    }
                }
            });
        }
        ThreadsHolder.waitForTasksToFinish();

        //Divide color values by number pixels being part of new pixel
        int[] newPixels = new int[newPixelsAtomic.length];
        for (int i = 0; i < newPixelsAtomic.length; i++) {
            int count = counts[i].get();
            newPixels[i] = count != 0 ?
                    (int) (newPixelsAtomic[i].get() / count) :
                    pixelValue(i, fillColor);
        }
        Instant end = Instant.now();
        System.out.println("Took: " + Duration.between(start, end).toMillis());
        return newPixels;
    }


    public static int[] averagePixels(int targetWidth, int targetHeight,
                                         ArrayList<int[][][]> intCoordinates,
                                         ArrayList<int[]> originalPixels, ArrayList<Boolean> hasAlphaChanel,
                                         Color fillColor) {
        System.out.println("Averaging:");
        Instant start = Instant.now();
        AtomicLong[] newPixelsAtomic = new AtomicLong[targetWidth * targetHeight * 4]; // Sum of color and alpha channels from original pixels
        AtomicInteger[] counts = new AtomicInteger[targetWidth * targetHeight * 4]; // Number of original pixels being part of new pixel
        for (int i = 0; i < newPixelsAtomic.length; i++) {
            newPixelsAtomic[i] = new AtomicLong(0);
            counts[i] = new AtomicInteger(0);
        }

        for (int k = 0; k < intCoordinates.size(); k++) {
            int[][][] helperList = intCoordinates.get(k);
            boolean imgHasAlphaChannel = hasAlphaChanel.get(k);
            int[] imgOriginalPixels = originalPixels.get(k);
            for (int i = 0; i < helperList.length; i++) {
                int finalI = i;
                int[][] helpRow = helperList[i];
                ThreadsHolder.addTask(() -> {
                    for (int j = 0; j < helpRow.length; j++) {
                        int[] coordinates = helpRow[j];
                        int newPixelRedIndex = (coordinates[1] * targetWidth + coordinates[0]) * 4;
                        int newPixelGreenIndex = newPixelRedIndex + 1;
                        int newPixelBlueIndex = newPixelGreenIndex + 1;
                        int newPixelAlphaIndex = newPixelBlueIndex + 1;

                        int originalPixelRedIndex = imgHasAlphaChannel ? 4 * (helpRow.length * finalI + j) :
                                3 * (helpRow.length * finalI + j);
                        int originalPixelGreenIndex = originalPixelRedIndex + 1;
                        int originalPixelBlueIndex = originalPixelGreenIndex + 1;
                        int originalPixelAlphaIndex = originalPixelBlueIndex + 1;

                        newPixelsAtomic[newPixelRedIndex].addAndGet(imgOriginalPixels[originalPixelRedIndex]);
                        newPixelsAtomic[newPixelGreenIndex].addAndGet(imgOriginalPixels[originalPixelGreenIndex]);
                        newPixelsAtomic[newPixelBlueIndex].addAndGet(imgOriginalPixels[originalPixelBlueIndex]);

                        counts[newPixelRedIndex].addAndGet(1);
                        counts[newPixelGreenIndex].addAndGet(1);
                        counts[newPixelBlueIndex].addAndGet(1);

                        if (imgHasAlphaChannel) {
                            newPixelsAtomic[newPixelAlphaIndex].addAndGet(imgOriginalPixels[originalPixelAlphaIndex]);
                            counts[newPixelAlphaIndex].addAndGet(1);
                        } else if (newPixelsAtomic[newPixelAlphaIndex].get() == 0) {
                            newPixelsAtomic[newPixelAlphaIndex].set(255);
                            counts[newPixelAlphaIndex].set(1);
                        }
                    }
                });
            }
        }
        ThreadsHolder.waitForTasksToFinish();

        //Divide color values by number pixels being part of new pixel
        int[] newPixels = new int[newPixelsAtomic.length];
        for (int i = 0; i < newPixelsAtomic.length; i++) {
            int count = counts[i].get();
            newPixels[i] = count != 0 ?
                    (int) (newPixelsAtomic[i].get() / count) :
                    pixelValue(i, fillColor);
        }
        Instant end = Instant.now();
        System.out.println("Took: " + Duration.between(start, end).toMillis());
        return newPixels;
    }


    private static int[] averageMultiplePixelArrays(List<int[]> pixels, Color fillColor) throws Exception {
        if (!pixels.stream().allMatch(a -> a.length == pixels.get(0).length)) {
            throw new Exception("Nie równa ilość pixeli");
        }
        int[] result = new int[pixels.get(0).length];
        for (int j = 0; j < result.length; j++) {
            int finalJ = j;
            OptionalDouble tmp = pixels.stream().mapToInt(pix -> pix[finalJ]).filter(a -> a != Constants.pixelPlaceholder).average();
            int pix = tmp.isPresent() ? (int) tmp.getAsDouble() : Constants.pixelPlaceholder;
            result[finalJ] = pix;
        }
        return fillEmptyPixels(result, fillColor);
    }

    private static int[] fillEmptyPixels(int[] pixels, Color fillColor) {
        int[] newPixels = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            newPixels[i] = pixels[i] != Constants.pixelPlaceholder ?
                    pixels[i] : pixelValue(i, fillColor);
        }
        return newPixels;
    }

    private static int pixelValue(int ind, Color colorChoice) {
        switch (ind % 4) {
            case 0:
                return (int) (colorChoice.getRed() * 255);
            case 1:
                return (int) (colorChoice.getGreen() * 255);
            case 2:
                return (int) (colorChoice.getBlue() * 255);
            case 3:
                return (int) (colorChoice.getOpacity() * 255);
            default:
                return 255;
        }
    }

}
