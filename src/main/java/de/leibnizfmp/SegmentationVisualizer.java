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

        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
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
    void visualizeNucleiSegments(ImagePlus originalImage,
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

        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
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

    /**
     * visualization of the background segmentation
     *
     * @param originalImage     the original image for vis
     * @param imageObject       image for segmentation of the nucleus
     * @param kernelSizeCellArea median filter for cell area
     * @param rollingBallRadiusCellArea rolling ball radius for cell area
     * @param manualThresholdCellArea manual threshold setting for cell area
     * @param gaussSeparateCells gauss sigma for separated cell region
     * @param prominenceSeparatedCells prominence for maxima finder to creat cell region
     * @param minCellSize           minimum background region size
     * @param maxCellSize           maximum background region siz
     * @param lowCirc           lower threshold circularity
     * @param highCirc          higher threshold circularity
     * @param setDisplayRange   sets the display range for vis
     */
    void visulizeCellSegments(ImagePlus originalImage,
                                Image imageObject,
                                float kernelSizeCellArea,
                                double rollingBallRadiusCellArea,
                                int manualThresholdCellArea,
                                double gaussSeparateCells,
                                double prominenceSeparatedCells,
                                double minCellSize,
                                double maxCellSize,
                                double lowCirc,
                                double highCirc,
                                boolean setDisplayRange) {

        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
        ImagePlus nucleus = imp_channels[imageObject.nucleus - 1];
        ImagePlus cytoplasm = imp_channels[imageObject.cytoplasm - 1];

        // set the specified calibration
        originalImage.setOverlay(null);

        CellAreaSegmenter back = new CellAreaSegmenter();
        ImagePlus backgroundMask = back.segmentCellArea(cytoplasm, kernelSizeCellArea, rollingBallRadiusCellArea, manualThresholdCellArea);

        CellSeparator separator = new CellSeparator();
        ImagePlus separatedCells = separator.separateCells(nucleus, cytoplasm, gaussSeparateCells, prominenceSeparatedCells);

        CellFilter cellFilter = new CellFilter();
        ImagePlus filteredCells = cellFilter.filterCells(backgroundMask, separatedCells, minCellSize, maxCellSize, lowCirc, highCirc);

        int maxArea = nucleus.getWidth() * nucleus.getHeight();

        RoiManager manager = new RoiManager();
        ParticleAnalyzer backAnalyzer = new ParticleAnalyzer(2048, 0, null, 0, maxArea);
        backAnalyzer.analyze( filteredCells );

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
