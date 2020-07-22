package de.leibnizfmp;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;

public class DetectionFilter {

    // TODO: maybe implement a filter by cell segmentation

    static ImagePlus filterByNuclei(ImagePlus nuclei, ImagePlus detections ) {

        Calibration calibration = nuclei.getCalibration();
        ImagePlus nucleiMaskFilter = nuclei.duplicate();
        nucleiMaskFilter.getProcessor().invert();

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus filteredDetections = calculator.run("Multiply 32-bit", detections, nucleiMaskFilter);
        ImageProcessor filteredDetectionsProcessor = filteredDetections.getProcessor().convertToShort(true);
        ImagePlus filteredDetectionsImage = new ImagePlus( "filteredDetections", filteredDetectionsProcessor );

        filteredDetectionsImage.setCalibration(calibration);

        nucleiMaskFilter.close();

        return filteredDetectionsImage ;

    }
}
