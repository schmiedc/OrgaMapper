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

    static ImagePlus segmentCellArea(ImagePlus image, float kernelSize, double rollingBallRadius, int manualThreshold) {

        Calibration calibration = image.getCalibration();

        IJ.log("Median filter with radius: " + kernelSize);
        ImagePlus cellImageDup = image.duplicate();
        RankFilters medianFilter = new RankFilters();
        medianFilter.rank(cellImageDup.getProcessor(), kernelSize, 4);

        IJ.log("Background subtraction radius: " + rollingBallRadius);
        ImageProcessor filteredProcessor = cellImageDup.getProcessor();
        BackgroundSubtracter subtractor = new BackgroundSubtracter();
        subtractor.rollingBallBackground( filteredProcessor, rollingBallRadius,
                false, false, true, false, false );

        IJ.log("Global threshold with value: " + manualThreshold);
        filteredProcessor.setThreshold(manualThreshold, 65536, 1);
        ByteProcessor cellMaskProcessor = filteredProcessor.createMask();

        ImagePlus cellMask = new ImagePlus("backgroundMask", cellMaskProcessor);
        cellMask.setCalibration( calibration );
        IJ.log("Segmentation of cell area done");

        return cellMask;
    }


}
