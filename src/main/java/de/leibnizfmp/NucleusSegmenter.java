package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.Binary;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * segments nucleus channel
 *
 * @author christopher Schmied
 * @version 1.0.0
 */

public class NucleusSegmenter {

    static ImagePlus segmentNuclei(ImagePlus image, float kernelSize, double rollingBallRadius, String threshold, int erosion,
                            double minSize, double maxSize, double lowCirc, double highCirc ) {

        // extract calibration and convert size filter from micron to px
        Calibration calibration = image.getCalibration();
        Double pxSizeFromImage = calibration.pixelWidth;
        int minSizePx = Image.calculateSizePx( pxSizeFromImage, minSize);
        int maxSizePx = Image.calculateSizePx( pxSizeFromImage, maxSize);

        IJ.log("Median filter with radius: " + kernelSize);
        ImagePlus nucImageDup = image.duplicate();
        RankFilters medianFilter = new RankFilters();
        medianFilter.rank(nucImageDup.getProcessor(), kernelSize, 4);

        IJ.log("Background subtraction radius: " + rollingBallRadius);
        ImageProcessor filteredProcessor = nucImageDup.getProcessor();
        BackgroundSubtracter subtracted= new BackgroundSubtracter();
        subtracted.rollingBallBackground(filteredProcessor, rollingBallRadius,
                false, false, true, false, false);

        IJ.log("Automatic threshold method: " + threshold);
        filteredProcessor.setAutoThreshold( threshold, true, 0);
        ByteProcessor unfilteredMask = filteredProcessor.createMask();

        IJ.log("Filter ROIs with size: " + minSize + "-" + maxSize + " µm²");
        IJ.log("Filter ROIs with circ: " + lowCirc + " - " + highCirc);
        // 4104 = ( show masks = 4096 + exclude on edges = 8 )
        // problematic! somehow does not return a proper filtered mask
        ParticleAnalyzer analyzer = new ParticleAnalyzer(4104,0,null, minSizePx , maxSizePx, lowCirc, highCirc );

        ImagePlus mask = new ImagePlus("afterThreshold", unfilteredMask);

        analyzer.analyze( mask, unfilteredMask);
        ImagePlus filteredMask = analyzer.getOutputImage();
        filteredMask.hide();

        //filteredMask.hide();

        IJ.log("Erode mask " + erosion + "x");
        ByteProcessor unfilteredMaskByteProcessor = filteredMask.getProcessor().convertToByteProcessor();
        unfilteredMaskByteProcessor.invertLut();
        //unfilteredMaskByteProcessor.invert();

        Binary binaryProcessorFill = new Binary();
        binaryProcessorFill.setup("fill holes", filteredMask );
        binaryProcessorFill.run(unfilteredMaskByteProcessor);

        if ( erosion > 0 ) {

            IJ.log("Applying erosion: " + erosion + "x");

            Binary binaryProcessorErode = new Binary();
            binaryProcessorErode.setup("erode", filteredMask);

            for ( int i = 0; i < erosion; i++) {

                binaryProcessorErode.run(unfilteredMaskByteProcessor);

            }

        } else {

            IJ.log("No erosion applied");

        }


        ImagePlus erodedFilteredMask = new ImagePlus("nucleiMask", unfilteredMaskByteProcessor);

        // makes sure that mask keeps calibration
        IJ.log("Segmentation of nuclei done.");
        erodedFilteredMask.setCalibration(calibration);

        return erodedFilteredMask ;

    }

}
