package de.leibnizfmp;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class DetectionFilter {

    static ImagePlus filterByCells(ImagePlus detections, RoiManager manager) {

        Calibration calibration = detections.getCalibration();
        ImagePlus detectionsDup = detections.duplicate();
        manager.moveRoisToOverlay(detectionsDup);

        ByteProcessor cellMask = detectionsDup.createRoiMask();
        ImagePlus cellMaskImage = new ImagePlus("cellMask", cellMask);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus filteredDetections = calculator.run("Multiply 32-bit", detections, cellMaskImage);

        filteredDetections.setCalibration(calibration);
        detectionsDup.close();

        return filteredDetections;

    }

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
