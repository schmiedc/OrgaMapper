package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.MaximumFinder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * separates cell using maxima finder with segmented particles option
 *
 * @author christopher Schmied
 * @version 1.0.0
 */

public class CellSeparator {

    static ImagePlus separateCells(ImagePlus nucleus, ImagePlus cytoplasm, double sigmaGauss, double prominence, boolean invertCellImageSetting) {

        Calibration calibration = nucleus.getCalibration();

        IJ.log("Gauss filter with sigma: " + sigmaGauss);

        ImagePlus cellImageDup;

        if (invertCellImageSetting) {

            IJ.log("Inverting cell image to segment membrane signal");
            ImagePlus cellImageDupIntermediate = cytoplasm.duplicate();
            ImageProcessor cellImageDupProcessor = cellImageDupIntermediate.getProcessor();
            cellImageDupProcessor.invert();
            cellImageDup = new ImagePlus("invertedCellMask", cellImageDupProcessor);

        } else {

            cellImageDup = cytoplasm.duplicate();
            IJ.log("Cell image not inverted");

        }

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus nucleusCytoplasm = calculator.run("Add 32-bit", nucleus, cellImageDup);
        ImageProcessor nucleusCytoplasmProcessor = nucleusCytoplasm.getProcessor().convertToShort(true);
        nucleusCytoplasmProcessor.blurGaussian(sigmaGauss);

        IJ.log("Detecting segmented particles with prominence: " + prominence);
        MaximumFinder findMaxima = new MaximumFinder();
        ByteProcessor segmentedParticles = findMaxima.findMaxima(nucleusCytoplasmProcessor, prominence, 2, true);

        ImagePlus segmentedParticlesImage = new ImagePlus("segmentedParticle", segmentedParticles);
        segmentedParticlesImage.setCalibration( calibration );
        IJ.log("Watershed done");

        return segmentedParticlesImage;
    }
}
