package de.leibnizfmp;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.Filters3D;
import ij.plugin.filter.BackgroundSubtracter;
import ij.process.ImageProcessor;

public class BackgroundSegmenter {

    ImagePlus segmentBackground(ImagePlus image, float kernelSize, double rollingBallRadius, int manualThreshold) {

        Calibration calibration = image.getCalibration();

        ImageStack filteredImage = Filters3D.filter( image.getImageStack(), Filters3D.MEDIAN, kernelSize, kernelSize, kernelSize );

        ImageProcessor filteredProcessor = filteredImage.getProcessor(1);
        BackgroundSubtracter subtractor = new BackgroundSubtracter();
        subtractor.rollingBallBackground( filteredProcessor, rollingBallRadius,
                false, false, true, false, false );

        filteredProcessor.setThreshold(manualThreshold, 65536, 1);
        filteredProcessor.createMask();

        ImagePlus cellMask = new ImagePlus("backgroundMask", filteredProcessor);
        cellMask.setCalibration( calibration );

        return cellMask;
    }


}
