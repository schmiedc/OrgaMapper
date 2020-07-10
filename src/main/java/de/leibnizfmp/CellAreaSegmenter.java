package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.Filters3D;
import ij.plugin.filter.BackgroundSubtracter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * segments area covered by cells
 *
 * @author christopher Schmied
 * @version 1.0.0
 */
public class CellAreaSegmenter {

    static ImagePlus segmentCellArea(ImagePlus image, float kernelSize, double rollingBallRadius, int manualThreshold) {


        Calibration calibration = image.getCalibration();

        IJ.log("Median filter with radius: " + kernelSize);
        ImageStack filteredImage = Filters3D.filter( image.getImageStack(), Filters3D.MEDIAN, kernelSize, kernelSize, kernelSize );

        IJ.log("Background subtraction radius: " + rollingBallRadius);
        ImageProcessor filteredProcessor = filteredImage.getProcessor(1);
        BackgroundSubtracter subtractor = new BackgroundSubtracter();
        subtractor.rollingBallBackground( filteredProcessor, rollingBallRadius,
                false, false, true, false, false );

        IJ.log("Global threshold with value: " + manualThreshold);
        filteredProcessor.setThreshold(manualThreshold, 65536, 1);
        ByteProcessor nucleiMask = filteredProcessor.createMask();

        ImagePlus cellMask = new ImagePlus("backgroundMask", nucleiMask);
        cellMask.setCalibration( calibration );
        IJ.log("Segmentation of cell area done");

        return cellMask;
    }


}
