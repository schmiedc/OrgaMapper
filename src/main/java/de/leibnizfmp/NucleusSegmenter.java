package de.leibnizfmp;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.FileInfoVirtualStack;
import ij.plugin.Filters3D;
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

    ImagePlus segmentNucleus( ImagePlus image, float kernelSize, String threshold, int erosion,
                                 int minSizePx, int maxSizePx, double lowCirc, double highCirc ) {

        Filters3D filter = new Filters3D();
        ImageStack imageStack = image.getImageStack();
        ImageStack processImage = filter.filter(imageStack, Filters3D.MEDIAN, kernelSize, kernelSize, kernelSize);

        ImageProcessor processor = processImage.getProcessor(1);
        processor.setAutoThreshold( threshold, true, 1);
        ByteProcessor unfilteredMask = processor.createMask();
        ImagePlus mask = new ImagePlus("mask", unfilteredMask);

        //TODO: filter sizes in square micron at the moment px
        //TODO: Particle analyzer also retruns ROIs maybe don't do that
        ParticleAnalyzer analyzer = new ParticleAnalyzer(2048,0,null,
                minSizePx, maxSizePx, lowCirc, highCirc );

        analyzer.analyze( mask );

        return mask;

    }

}
