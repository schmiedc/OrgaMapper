package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.filter.MaximumFinder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import trainableSegmentation.ImageScience;

/**
 * separates cell using maxima finder with segmented particles option
 *
 * @author christopher Schmied
 * @version 1.0.0
 */

public class OrganelleDetector {

    static ImagePlus detectOrganelles(ImagePlus image, double simgaLoG, double prominence) {

        Calibration calibration = image.getCalibration();

        Calibration setToPx = new Calibration();

        setToPx.pixelWidth = 1;
        setToPx.pixelHeight = 1;
        setToPx.setXUnit("pixel");
        setToPx.setYUnit("pixel");

        image.setCalibration(setToPx);

        IJ.log("Applying a LoG filter with sigma: " + simgaLoG);
        ImagePlus logImage = ImageScience.computeLaplacianImage(simgaLoG, image);

        IJ.log("Detecting Minima with prominence: " + prominence);
        ImageProcessor getMaxima = logImage.getProcessor().convertToFloatProcessor();

        getMaxima.invert();

        MaximumFinder maxima = new MaximumFinder();
        ByteProcessor selection = maxima.findMaxima(getMaxima, prominence, 0, false);
        IJ.log("Spot detection finished");

        ImagePlus selectionImage = new ImagePlus("lysosomeDetection", selection);
        selectionImage.setCalibration(calibration);

        return selectionImage;

    }
}
