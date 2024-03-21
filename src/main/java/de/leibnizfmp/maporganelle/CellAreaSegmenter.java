package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * segments area covered by cells
 *
 * @author christopher Schmied
 * @version 1.0.0
 */
public class CellAreaSegmenter {

    static ImagePlus segmentCellArea(ImagePlus image, float kernelSize, double rollingBallRadius, int manualThreshold, boolean invertCellImageSetting) {

        Calibration calibration = image.getCalibration();

        IJ.log("Median filter with radius: " + kernelSize);
        ImagePlus cellImageDup = image.duplicate();

        // TODO: invert cell image to segment using membrane signal
        ImageProcessor cellImageDupProcessor = cellImageDup.getProcessor();

        if (invertCellImageSetting) {

            IJ.log("Inverting cell image to segment membrane signal");
            cellImageDupProcessor.invert();

        } else {

            IJ.log("Cell image not inverted");

        }

        // apply median filter
        RankFilters medianFilter = new RankFilters();
        medianFilter.rank(cellImageDupProcessor, kernelSize, 4);

        IJ.log("Background subtraction radius: " + rollingBallRadius);

        if (rollingBallRadius == 0) {

            IJ.log("Background subtraction turned off");

        } else {
            BackgroundSubtracter subtractor = new BackgroundSubtracter();
            subtractor.rollingBallBackground(cellImageDupProcessor, rollingBallRadius,
                    false, false, true, false, false);
        }

        IJ.log("Global threshold with value: " + manualThreshold);
        cellImageDupProcessor.setThreshold(manualThreshold, 65536, 1);
        ByteProcessor cellMaskProcessor = cellImageDupProcessor.createMask();

        ImagePlus cellMask = new ImagePlus("backgroundMask", cellMaskProcessor);
        cellMask.setCalibration( calibration );
        IJ.log("Segmentation of cell area done");

        return cellMask;
    }


}
