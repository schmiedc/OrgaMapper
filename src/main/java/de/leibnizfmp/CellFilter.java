package de.leibnizfmp;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;

public class CellFilter {

    ImagePlus filterCells( ImagePlus backgroundMask, ImagePlus separatedCells,
                           double minCellSize, double maxCellSize, double highCirc, double lowCirc) {

        Calibration calibration = backgroundMask.getCalibration();

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus individualCells = calculator.run("Multiply create", backgroundMask, separatedCells);
        ImageProcessor cellsMask individualCells.getProcessor().convertToByteProcessor();

        ImagePlus individualCellsFinal = new ImagePlus ("individualCells", );

        segmentedParticlesImage.setCalibration( calibration );

        return segmentedParticlesImage;



    }
}
