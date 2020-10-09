package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class BackgroundMeasure {

    static double measureDetectionBackground(ImagePlus background, ImagePlus measureImage) {

        IJ.log("Starting background measurements");

        ImagePlus backgroundDup = background.duplicate();

        backgroundDup.getProcessor().invert();

        ParticleAnalyzer cellAnalyzer = new ParticleAnalyzer(2048, 0, null,
                500, Image.calculateMaxArea( backgroundDup.getWidth(), backgroundDup.getHeight() ) );

        RoiManager backgroundRoiManager = new RoiManager(false);
        ParticleAnalyzer.setRoiManager( backgroundRoiManager );

        cellAnalyzer.analyze( backgroundDup );

        double backgroundMean;

        if ( backgroundRoiManager.getCount() > 0 ) {

            int imageWidth;
            int imageHeight;

            double backgroundSum = 0;
            double backgroundArea = 0;

            imageWidth = measureImage.getWidth();
            imageHeight = measureImage.getHeight();

            ImageProcessor measureImageProcessor = measureImage.getProcessor();
            ImageProcessor backgroundMaksProcessor = backgroundDup.getProcessor();

            float[] measureImagePixels = (float[]) measureImageProcessor.convertToFloat().getPixels();
            byte[] backgroundMaskPixels  = (byte[]) backgroundMaksProcessor.getPixels();

            for (int y = 0; y < imageHeight; y++) {

                for (int x = 0; x < imageWidth; x++) {

                    if ( backgroundMaskPixels[x + y * imageWidth] < 0 ) {

                        backgroundSum += measureImagePixels[x + y * imageWidth];
                        backgroundArea += 1;

                    }

                }

            }

            backgroundMean = backgroundSum / backgroundArea;

       } else {

            backgroundMean = -1;

        }

        backgroundDup.close();
        backgroundRoiManager.reset();

        return backgroundMean;

    }
}
