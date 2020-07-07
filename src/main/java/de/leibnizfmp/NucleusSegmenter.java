package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.Filters3D;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * segments nucleus channel
 *
 * @author christopher Schmied
 * @version 1.0.0
 */

public class NucleusSegmenter {

    ImagePlus segmentNuclei(ImagePlus image, float kernelSize, double subtractBackground, String threshold, int erosion,
                            double minSize, double maxSize, double lowCirc, double highCirc ) {

        // extract calibration and convert size filter from micron to px
        Calibration calibration = image.getCalibration();
        Double pxSizeFromImage = calibration.pixelWidth;
        int minSizePx = Image.calculateSizePx( pxSizeFromImage, minSize);
        int maxSizePx = Image.calculateSizePx( pxSizeFromImage, maxSize);

        ImageStack imageStack = image.getImageStack();
        ImageStack filteredStack = Filters3D.filter(imageStack, Filters3D.MEDIAN, kernelSize, kernelSize, kernelSize);

        ImageProcessor filteredProcessor = filteredStack.getProcessor(1);
        BackgroundSubtracter subtracted= new BackgroundSubtracter();
        subtracted.rollingBallBackground(filteredProcessor, subtractBackground,
                false, false, true, false, false);

        filteredProcessor.setAutoThreshold( threshold, true, 1);
        ByteProcessor unfilteredMask = filteredProcessor.createMask();

        // 4104 = 4096 + 8
        ParticleAnalyzer analyzer = new ParticleAnalyzer(4104,0,null,
                minSizePx , maxSizePx, lowCirc, highCirc );

        ImagePlus mask = new ImagePlus("afterThreshold", unfilteredMask);
        analyzer.analyze( mask );
        ImagePlus filteredMask = analyzer.getOutputImage();
        filteredMask.hide();

        ByteProcessor unfilteredMaskByteProcessor = filteredMask.getProcessor().convertToByteProcessor();
        unfilteredMaskByteProcessor.erode(erosion, 0);
        unfilteredMaskByteProcessor.invertLut();

        ImagePlus erodedFilteredMask = new ImagePlus("nucleiMask", unfilteredMaskByteProcessor);

        // makes sure that mask keeps calibration
        erodedFilteredMask .setCalibration(calibration);

        return erodedFilteredMask ;

    }

}
