package com.example.reprojector.helpers;

import com.example.reprojector.SingleInputImage;
import com.example.reprojector.globetoimagecalculations.EquirectangularCalculations;
import com.example.reprojector.globetoimagecalculations.GlobeToImageCalculations;
import com.example.reprojector.globetoimagecalculations.UVMapCalculations;
import com.example.reprojector.imagetoglobecalculations.InputToGlobeCalculations;
import com.example.reprojector.options.MapProjection;
import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ImageBuilder {

    public static BufferedImage uvImageFromList(ArrayList<SingleInputImage> inputImages, int width, int height, Color fillColor) {
        return imageFromList(inputImages, width, height, MapProjection.UV, fillColor);
    }

    public static BufferedImage imageFromList(ArrayList<SingleInputImage> inputImages, int width, int height, MapProjection mapProjection, Color fillColor) {
        AtomicInteger[][] counts = createAtomicInteger2DArrayWithZeros(height, width);
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        GlobeToImageCalculations globeToImageCalculations;
        switch (mapProjection) {
            case UV -> globeToImageCalculations = new UVMapCalculations();
            default -> globeToImageCalculations = new EquirectangularCalculations();
        }
        //Loop through all images to be added
        inputImages.forEach(input -> {
            input.recalculate();
            BufferedImage addedImage = input.getResizedImage();
            InputToGlobeCalculations imageToGlobeCalculation = input.getInputToGlobeCalculations();
            int i = 0;
            int heightStep;
            int thisStep;
            //Loop through whole image row by row
            while (i < addedImage.getHeight()) {
                //Calculating pixel height of added row
                try {
                    heightStep = Math.toIntExact(Utils.usableMemory() / Constants.memoryDividerForImageBuilding / addedImage.getWidth());
                    thisStep = Math.min((addedImage.getHeight() - i), heightStep);
                } catch (ArithmeticException exception) {
                    thisStep = Math.min((addedImage.getHeight() - i), Integer.MAX_VALUE);
                }
                //Calculating coordinates of added pixels on composed image
                int[][][] coordinates = globeToImageCalculations.imageCoordinates(imageToGlobeCalculation.angleCoordinates(i, thisStep), height, width);
                int minX = Arrays.stream(coordinates).flatMap(Arrays::stream).map(a -> a[0]).min(Integer::compareTo).get();
                int maxX = Arrays.stream(coordinates).flatMap(Arrays::stream).map(a -> a[0]).max(Integer::compareTo).get();
                int minY = Arrays.stream(coordinates).flatMap(Arrays::stream).map(a -> a[1]).min(Integer::compareTo).get();
                int maxY = Arrays.stream(coordinates).flatMap(Arrays::stream).map(a -> a[1]).max(Integer::compareTo).get();
                int snippetWidth = maxX - minX + 1;
                int snippetHeight = maxY - minY + 1;
                //Getting pixels added up to this point
                int[] mainImagePixels = new int[snippetWidth * snippetHeight * 4];
                newImage.getRaster().getPixels(minX, minY, snippetWidth, snippetHeight, mainImagePixels);
                AtomicIntegerArray mainImagePixelsAtomic = fillInitialPixelArray(mainImagePixels, minX, minY, snippetWidth, snippetHeight, counts);
                //Adding pixels of new image
                addOtherImagePixels(mainImagePixelsAtomic, addedImage, minX, minY, i, thisStep, snippetWidth, coordinates, counts);
                //Dividing accumulated value of pixels by count of pixels that took part in creating it
                preparePixelsToDraw(mainImagePixels, mainImagePixelsAtomic, minX, minY, snippetWidth, coordinates, counts);

                newImage.getRaster().setPixels(minX, minY, snippetWidth, snippetHeight, mainImagePixels);
                i += thisStep;
            }
            input.dropUnneededVariables();
        });

        return fillEmptySpots(newImage, counts, fillColor);
    }

    private static AtomicIntegerArray fillInitialPixelArray(int[] pixels, int minX, int minY, int snippetWidth, int snippetHeight, AtomicInteger[][] counts) {
        AtomicIntegerArray emptyPixels = new AtomicIntegerArray(pixels.length);
        for (int j = 0; j < (snippetHeight); j++) {
            int finalJ = j;
            ThreadsHolder.addTask(() -> {
                for (int k = 0; k < (snippetWidth); k++) {
                    int id = (finalJ * (snippetWidth) + k) * 4;
                    emptyPixels.set(id, pixels[id] * counts[finalJ + minY][k + minX].get());
                    emptyPixels.set(id + 1, pixels[id + 1] * counts[finalJ + minY][k + minX].get());
                    emptyPixels.set(id + 2, pixels[id + 2] * counts[finalJ + minY][k + minX].get());
                    emptyPixels.set(id + 3, pixels[id + 3] * counts[finalJ + minY][k + minX].get());
                }
            });
        }
        ThreadsHolder.waitForTasksToFinish();
        return emptyPixels;
    }


    private static void addOtherImagePixels(AtomicIntegerArray newPixels, BufferedImage additionalImage, int minX, int minY, int startY, int stepSize, int snippetWidth, int[][][] coordinates, AtomicInteger[][] counts) {
        int alphaMultiplayer = additionalImage.getColorModel().hasAlpha() ? 4 : 3;
        int[] addedPixels = new int[coordinates.length * coordinates[0].length * alphaMultiplayer];
        additionalImage.getRaster().getPixels(0, startY, additionalImage.getWidth(), stepSize, addedPixels);
        for (int j = 0; j < coordinates.length; j++) {
            int finalJ = j;
            ThreadsHolder.addTask(() -> {
                for (int k = 0; k < coordinates[finalJ].length; k++) {
                    int x = coordinates[finalJ][k][0];
                    int y = coordinates[finalJ][k][1];
                    int id = (finalJ * coordinates[finalJ].length + k) * alphaMultiplayer;
                    int id2 = (snippetWidth * (y - minY) + (x - minX)) * 4;
                    newPixels.addAndGet(id2, addedPixels[id]);
                    newPixels.addAndGet(id2 + 1, addedPixels[id + 1]);
                    newPixels.addAndGet(id2 + 2, addedPixels[id + 2]);
                    if (alphaMultiplayer == 4) {
                        newPixels.addAndGet(id2 + 3, addedPixels[id + 3]);
                    } else {
                        newPixels.addAndGet(id2 + 3, 255);
                    }
                    counts[y][x].addAndGet(1);
                }
            });
        }
        ThreadsHolder.waitForTasksToFinish();
    }


    private static void preparePixelsToDraw(int[] inputPixels, AtomicIntegerArray prePreparationPixels, int minX, int minY, int snippetWidth, int[][][] coordinates, AtomicInteger[][] counts) {
        for (int j = 0; j < coordinates.length; j++) {
            int finalJ = j;
            ThreadsHolder.addTask(() -> {
                for (int k = 0; k < coordinates[finalJ].length; k++) {
                    int x = coordinates[finalJ][k][0];
                    int y = coordinates[finalJ][k][1];
                    int id2 = (snippetWidth * (y - minY) + (x - minX)) * 4;
                    inputPixels[id2] = prePreparationPixels.get(id2) / Math.max(counts[y][x].get(), 1);
                    inputPixels[id2 + 1] = prePreparationPixels.get(id2 + 1) / Math.max(counts[y][x].get(), 1);
                    inputPixels[id2 + 2] = prePreparationPixels.get(id2 + 2) / Math.max(counts[y][x].get(), 1);
                    inputPixels[id2 + 3] = prePreparationPixels.get(id2 + 3) / Math.max(counts[y][x].get(), 1);
                }
            });
        }
        ThreadsHolder.waitForTasksToFinish();
    }


    private static BufferedImage fillEmptySpots(BufferedImage image, AtomicInteger[][] counts, Color fillColor) {
        int startY = 0;
        int heightStep;
        int stepSize;

        while (startY < image.getHeight()) {
            try {
                heightStep = Math.toIntExact(Utils.usableMemory() / Constants.memoryDividerForColorFilling / image.getWidth());
                stepSize = Math.min((image.getHeight() - startY), heightStep);
            } catch (ArithmeticException exception) {
                stepSize = Math.min((image.getHeight() - startY), Integer.MAX_VALUE);
            }
            int[] pixels = new int[image.getWidth() * stepSize * 4];
            image.getRaster().getPixels(0, startY, image.getWidth(), stepSize, pixels);
            for (int i = 0; i < stepSize; i++) {
                int finalStartY = startY;
                int finalI = i;
                ThreadsHolder.addTask(() -> {
                    for (int j = 0; j < image.getWidth(); j++) {
                        if (counts[finalStartY + finalI][j].get() == 0) {
                            pixels[(image.getWidth() * finalI + j) * 4] = (int) (fillColor.getRed() * 255);
                            pixels[(image.getWidth() * finalI + j) * 4 + 1] = (int) (fillColor.getGreen() * 255);
                            pixels[(image.getWidth() * finalI + j) * 4 + 2] = (int) (fillColor.getBlue() * 255);
                            pixels[(image.getWidth() * finalI + j) * 4 + 3] = (int) (fillColor.getOpacity() * 255);
                        }
                    }
                });
            }
            ThreadsHolder.waitForTasksToFinish();
            image.getRaster().setPixels(0, startY, image.getWidth(), stepSize, pixels);
            startY += stepSize;
        }
        return image;
    }

    private static AtomicInteger[][] createAtomicInteger2DArrayWithZeros(int width, int height) {
        AtomicInteger[][] atomicInteger2DArray = new AtomicInteger[width][height];
        for (int i = 0; i < atomicInteger2DArray.length; i++) {
            int finalI = i;
            ThreadsHolder.addTask(() -> {
                for (int j = 0; j < atomicInteger2DArray[finalI].length; j++) {
                    atomicInteger2DArray[finalI][j] = new AtomicInteger(0);
                }
            });
        }
        ThreadsHolder.waitForTasksToFinish();
        return atomicInteger2DArray;
    }
}
