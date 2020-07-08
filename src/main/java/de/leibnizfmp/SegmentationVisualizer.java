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
     * @param originalImage   selected image for vis
     * @param sigmaLoG        sigma for LoG
     * @param prominence      prominence for spot detection
     * @param setDisplayRange sets the display range for vis
     */
    void visualizeSpots(ImagePlus originalImage,
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
        // TODO: filter lysosomes from nuclei

        // get detections as polygons and put on image as roi
        MaximumFinder maxima = new MaximumFinder();
        ImageProcessor getMaxima = detectedLysosomes.getProcessor().convertToByteProcessor();
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
            double newUpper = (rangeMax / 2);

            originalImage.setDisplayRange(newLower, newUpper);

        }


    }

    /**
     * visualization of the background segmentation
     *
     * @param originalImage     the original image for vis
     * @param imageObject       image for segmentation of the nucleus
     * @param kernelSize        sigma gaussian blur for background segmentation
     * @param rollingBallRadius global intensity threshold for background segmentation
     * @param threshold         thresholding method
     * @param erosion           number of erosions
     * @param minSize           minimum background region size
     * @param maxSize           maximum background region siz
     * @param lowCirc           lower threshold circularity
     * @param highCirc          higher threshold circularity
     * @param setDisplayRange   sets the display range for vis
     */
    void visulizeNucleiSegments(ImagePlus originalImage,
                                Image imageObject,
                                float kernelSize,
                                double rollingBallRadius,
                                String threshold,
                                int erosion,
                                double minSize,
                                double maxSize,
                                double lowCirc,
                                double highCirc,
                                boolean setDisplayRange) {

        ChannelSplitter splitter = new ChannelSplitter();
        ImagePlus[] imp_channels = splitter.split(originalImage);
        ImagePlus nucleus = imp_channels[imageObject.nucleus - 1];

        // set the specified calibration
        originalImage.setOverlay(null);

        NucleusSegmenter nuc = new NucleusSegmenter();
        ImagePlus nucleusMask = nuc.segmentNuclei(nucleus, kernelSize, rollingBallRadius, threshold, erosion, minSize, maxSize, lowCirc, highCirc);

        int maxArea = nucleus.getWidth() * nucleus.getHeight();

        RoiManager manager = new RoiManager();
        ParticleAnalyzer backAnalyzer = new ParticleAnalyzer(2048, 0, null, 0, maxArea);
        backAnalyzer.analyze(nucleusMask);

        manager.moveRoisToOverlay(originalImage);
        Overlay overlay = originalImage.getOverlay();
        overlay.drawLabels(false);

        if (setDisplayRange) {

            double rangeMin = originalImage.getDisplayRangeMin();
            double newLower = rangeMin * 1.75;
            double rangeMax = originalImage.getDisplayRangeMax();
            double newUpper = (rangeMax / 2);
            originalImage.setDisplayRange(newLower, newUpper);

        }

        originalImage.show();

        manager.reset();
        manager.close();

    }

}
