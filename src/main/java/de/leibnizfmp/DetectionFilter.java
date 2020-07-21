package de.leibnizfmp;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;

public class DetectionFilter {

    // TODO: maybe implement a filter by cell segmentation

    static ImagePlus filterByNuclei(ImagePlus nuclei, ImagePlus detections ) {

        Calibration calibration = nuclei.getCalibration();
        ImagePlus nucleiMask = nuclei.duplicate();
        nucleiMask.getProcessor().invert();

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus filteredDetections = calculator.run("Multiply 32-bit", detections, nucleiMask);
        ImageProcessor filteredDetectionsProcessor = filteredDetections.getProcessor().convertToShort(true);
        ImagePlus filteredDetectionsImage = new ImagePlus( "filteredDetections", filteredDetectionsProcessor );

        filteredDetectionsImage.setCalibration(calibration);

        nucleiMask.close();

        return filteredDetectionsImage ;

    }
}
