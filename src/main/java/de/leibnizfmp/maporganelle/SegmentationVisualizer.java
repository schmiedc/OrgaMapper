package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class SegmentationVisualizer {

    /**
     * visualization of the spot segmentation
     * @param originalImage             selected image for vis
     * @param sigmaLoG                  sigma for LoG
     * @param prominence                prominence for spot detection
     * @param organelleFilterCheck      check if detections need to be filtered by nuclei masks
     * @param setDisplayRange           sets the display range for vis
     * @param kernelSizeNuc             sigma gaussian blur for background segmentation
     * @param rollingBallRadiusNuc      rolling ball radius for background subtraction
     * @param thresholdNuc              global intensity threshold for background segmentation
     * @param erosionNuc                number of erosions
     * @param minSizeNuc                minimum background region size
     * @param maxSizeNuc                maximum background region size
     * @param lowCircNuc                lower threshold circularity
     * @param highCircNuc               higher threshold circularity
     */
    void visualizeSpots(ImagePlus originalImage,
                        Image imageObject,
                        double sigmaLoG,
                        double prominence,
                        boolean organelleFilterCheck,
                        boolean setDisplayRange,
                        float kernelSizeNuc,
                        double rollingBallRadiusNuc,
                        String thresholdNuc,
                        int erosionNuc,
                        double minSizeNuc,
                        double maxSizeNuc,
                        double lowCircNuc,
                        double highCircNuc) {

        IJ.log("Starting visualization for organelle detection...");
        originalImage.setOverlay(null);

        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
        ImagePlus organelle = imp_channels[imageObject.organelle - 1];

        ImagePlus detectedLysosomes = OrganelleDetector.detectOrganelles(organelle, sigmaLoG, prominence);

        ImagePlus detectedLysosomesFinal;

        if ( organelleFilterCheck ) {

            ImagePlus nucleus = imp_channels[imageObject.nucleus - 1];

            ImagePlus nucleiMask = NucleusSegmenter.segmentNuclei(nucleus, kernelSizeNuc, rollingBallRadiusNuc, thresholdNuc, erosionNuc, minSizeNuc, maxSizeNuc, lowCircNuc, highCircNuc);

            detectedLysosomesFinal = DetectionFilter.filterByNuclei(nucleiMask, detectedLysosomes);
            IJ.log("Detections within nuclei will not be shown");
            nucleus.close();

        } else {

            detectedLysosomesFinal = detectedLysosomes;
            IJ.log("Detections within nuclei will be shown");

        }

        // get detections as polygons and put on image as roi
        MaximumFinder maxima = new MaximumFinder();
        ImageProcessor getMaxima = detectedLysosomesFinal.getProcessor().convertToByteProcessor();
        java.awt.Polygon detections = maxima.getMaxima(getMaxima, 1, false);
        PointRoi roi = new PointRoi(detections);

        originalImage.setC( imageObject.organelle );
        originalImage.setRoi(roi);
        originalImage.show();

        if (setDisplayRange) {

            double rangeMin = originalImage.getDisplayRangeMin();
            double newLower = rangeMin * 1.75;
            double rangeMax = originalImage.getDisplayRangeMax();
            double newUpper = (rangeMax / 2);

            originalImage.setDisplayRange(newLower, newUpper);

        }

        IJ.log("Organelle visualization done: " + detections.npoints + " detection(s)");

    }

    /**
     * visualization of the nucleus segmentation
     *
     * @param originalImage     the original image for vis
     * @param imageObject       image for segmentation of the nucleus
     * @param kernelSize        sigma gaussian blur for background segmentation
     * @param rollingBallRadius rolling ball radius for background subtraction
     * @param threshold         thresholding method
     * @param erosion           number of erosions
     * @param minSize           minimum background region size
     * @param maxSize           maximum background region size
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

        IJ.log("Starting visualization for nucleus segmentation ");
        originalImage.setOverlay(null);
        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
        ImagePlus nucleus = imp_channels[imageObject.nucleus - 1];
        nucleus.setOverlay(null);

        ImagePlus nucleusMask = NucleusSegmenter.segmentNuclei(nucleus, kernelSize, rollingBallRadius, threshold, erosion, minSize, maxSize, lowCirc, highCirc);

        int maxArea = nucleus.getWidth() * nucleus.getHeight();

        RoiManager manager = new RoiManager();
        ParticleAnalyzer backAnalyzer = new ParticleAnalyzer(2048, 0, null, 0, maxArea);
        backAnalyzer.analyze(nucleusMask);

        originalImage.setC( imageObject.nucleus );
        originalImage.setOverlay(null);
        manager.moveRoisToOverlay(originalImage);
        Overlay overlay = originalImage.getOverlay();
        overlay.drawLabels(false);

        if (setDisplayRange) {

            double rangeMin = originalImage.getDisplayRangeMin();
            double newLower = rangeMin * 1.75;
            double rangeMax = originalImage.getDisplayRangeMax();
            double newUpper = (rangeMax / 1.5);
            originalImage.setDisplayRange(newLower, newUpper);

        }

        originalImage.show();
        IJ.log("Nucleus visualization done");
        manager.reset();
        manager.close();

    }

    /**
     * visualization of the background segmentation
     *
     * @param originalImage             the original image for vis
     * @param imageObject               image for segmentation of the nucleus
     * @param kernelSizeCellArea        median filter for cell area
     * @param rollingBallRadiusCellArea rolling ball radius for cell area
     * @param manualThresholdCellArea   manual threshold setting for cell area
     * @param gaussSeparateCells        gauss sigma for separated cell region
     * @param prominenceSeparatedCells  prominence for maxima finder to creat cell region
     * @param minCellSize               minimum background region size
     * @param maxCellSize               maximum background region siz
     * @param lowCellCirc               lower threshold circularity
     * @param highCellCirc              higher threshold circularity
     * @param cellFilterCheck           if one applies a further cell filter by nuclei number
     * @param setDisplayRange           sets the display range for vis
     * @param kernelSizeNuc             sigma gaussian blur for background segmentation
     * @param rollingBallRadiusNuc      rolling ball radius for background subtraction
     * @param thresholdNuc              global intensity threshold for background segmentation
     * @param erosionNuc                number of erosions
     * @param minSizeNuc                minimum background region size
     * @param maxSizeNuc                maximum background region size
     * @param lowCircNuc                lower threshold circularity
     * @param highCircNuc               higher threshold circularity
     * @param invertCellImage           Setting if cell channel should be inverted or not
     */
    void visualizeCellSegments(ImagePlus originalImage,
                               Image imageObject,
                               float kernelSizeCellArea,
                               double rollingBallRadiusCellArea,
                               int manualThresholdCellArea,
                               double gaussSeparateCells,
                               double prominenceSeparatedCells,
                               double minCellSize,
                               double maxCellSize,
                               double lowCellCirc,
                               double highCellCirc,
                               boolean cellFilterCheck,
                               boolean setDisplayRange,
                               float kernelSizeNuc,
                               double rollingBallRadiusNuc,
                               String thresholdNuc,
                               int erosionNuc,
                               double minSizeNuc,
                               double maxSizeNuc,
                               double lowCircNuc,
                               double highCircNuc,
                               boolean invertCellImage) {

        IJ.log("Starting visualization for cell segmentation ");

        originalImage.setOverlay(null);
        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
        ImagePlus nucleus = imp_channels[imageObject.nucleus - 1];
        ImagePlus cytoplasm = imp_channels[imageObject.cytoplasm - 1];
        nucleus.setOverlay(null);
        cytoplasm.setOverlay(null);

        ImagePlus backgroundMask = CellAreaSegmenter.segmentCellArea(cytoplasm,
                kernelSizeCellArea,
                rollingBallRadiusCellArea,
                manualThresholdCellArea,
                invertCellImage);

        ImagePlus separatedCells = CellSeparator.separateCells(nucleus, cytoplasm, gaussSeparateCells, prominenceSeparatedCells, invertCellImage);

        ImagePlus filteredCells = CellFilter.filterByCellSize(backgroundMask, separatedCells, minCellSize, maxCellSize, lowCellCirc, highCellCirc);

        if (cellFilterCheck) {

            ImagePlus nucleiMask = NucleusSegmenter.segmentNuclei(nucleus, kernelSizeNuc, rollingBallRadiusNuc, thresholdNuc, erosionNuc, minSizeNuc, maxSizeNuc, lowCircNuc, highCircNuc);
            RoiManager manager;
            IJ.log("Cell masks will be shown with nuclei number filter applied");
            manager = CellFilter.filterByNuclei(filteredCells, nucleiMask);

            originalImage.setC( imageObject.cytoplasm );
            originalImage.setOverlay(null);

            manager.moveRoisToOverlay(originalImage);
            Overlay overlay = originalImage.getOverlay();
            overlay.drawLabels(false);
            manager.reset();
            manager.close();


        } else {

            RoiManager manager = new RoiManager();

            IJ.log("Cell masks will be shown without nuclei number filter applied");
            int maxArea = nucleus.getWidth() * nucleus.getHeight();

            ParticleAnalyzer cellAnalyzer = new ParticleAnalyzer(2048, 0, null, 0, maxArea);
            cellAnalyzer.analyze( filteredCells );

            originalImage.setC( imageObject.cytoplasm );
            originalImage.setOverlay(null);

            manager.moveRoisToOverlay(originalImage);
            Overlay overlay = originalImage.getOverlay();
            overlay.drawLabels(false);
            manager.reset();
            manager.close();

        }

        if (setDisplayRange) {

            double rangeMin = originalImage.getDisplayRangeMin();
            double newLower = rangeMin * 1.75;
            double rangeMax = originalImage.getDisplayRangeMax();
            double newUpper = (rangeMax / 2);
            originalImage.setDisplayRange(newLower, newUpper);

        }

        originalImage.show();
        IJ.log("Cell visualization done");

    }

}
