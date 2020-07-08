package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.util.List;

public class SegmentationVisualizer {

    /**
     * visualization of the spot segmentation
     *
     * @param originalImage selected image for vis
     * @param sigmaLoG sigma for LoG
     * @param prominence prominence for spot detection
     * @param setDisplayRange sets the display range for vis
     */
    void spotVisualization(ImagePlus originalImage,
                           Image imageObject,
                           double sigmaLoG,
                           double prominence,
                           boolean setDisplayRange) {

        // remove existing overlays
        originalImage.setOverlay(null);

        ChannelSplitter splitter = new ChannelSplitter();
        ImagePlus[] imp_channels = splitter.split(originalImage);
        ImagePlus organelle = imp_channels[imageObject.organelle - 1];

        LysosomeDetector lysoDetector = new LysosomeDetector();
        ImagePlus detectedLysosomes = lysoDetector.detectLysosomes(organelle, sigmaLoG, prominence);

        ImageProcessor getMaxima = detectedLysosomes.getProcessor().convertToByteProcessor();

        // get detections as polygons and put on image as roi
        MaximumFinder maxima = new MaximumFinder();
        java.awt.Polygon detections = maxima.getMaxima(getMaxima, 1, false);
        PointRoi roi = new PointRoi(detections);

        // TODO: set on original image with correctly selected channel
        organelle.setRoi(roi);
        organelle.show();

        IJ.log("Visualizing " + detections.npoints + " lysosome(s)");

        if (setDisplayRange) {

            double rangeMin = originalImage.getDisplayRangeMin();
            double newLower = rangeMin * 1.75;
            double rangeMax = originalImage.getDisplayRangeMax();
            double newUpper = (rangeMax / 2 );

            originalImage.setDisplayRange(newLower,newUpper);

        }


    }


}
