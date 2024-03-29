package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.io.File;
import java.util.Objects;

public class ExternalSegmentationLoader {

    String createExternalFileNameSingleSeries(String baseName,
                                              String SegmentationSuffix,
                                              Integer seriesNumber,
                                              boolean multiSeries){
        String fileName;

        if (multiSeries) {

            fileName = baseName + "_S" + seriesNumber + SegmentationSuffix;

        } else {

            fileName = baseName + SegmentationSuffix;
        }

        // Add segmentationSuffix and return
        return fileName;

    }

    ImagePlus createExternalSegmentationMask(String externalSegmentationDirectory,
                                             String FileName,
                                             Calibration calibration) {

        IJ.log("External directory " + externalSegmentationDirectory);
        IJ.log("External segmentation file processed " + FileName);
        // loads the label image
        ImagePlus labelImage = IJ.openImage(externalSegmentationDirectory + File.separator + FileName);

        ImageProcessor labelImageProcessor = labelImage.getProcessor();
        labelImageProcessor.threshold(1);

        ImagePlus mask = new ImagePlus("afterThreshold", labelImageProcessor);

        mask.setCalibration(calibration);

        return mask;
    }

    RoiManager createExternalCellROIs(String externalCellSegmentationDirectory, String FileName) {

        IJ.log("External segmentation file processed " + FileName);

        ImagePlus labelImage = IJ.openImage(externalCellSegmentationDirectory + File.separator + FileName);

        return label2Roi(labelImage);

    }
    void visualizeExternalSpots(String externalDetectionDirectory,
                                String externalDetectionFileName,
                                ImagePlus originalImage,
                                Image imageObject,
                                boolean setDisplayRange) {

        IJ.log("External detection file processed " + externalDetectionFileName);

        originalImage.setOverlay(null);
        // TODO: there is an issue with some floating around ROIs that only vanish after setting this:
        originalImage.setRoi((Roi) null);

        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
        ImagePlus organelle = imp_channels[imageObject.organelle - 1];

        ImagePlus labelImage = IJ.openImage(externalDetectionDirectory + File.separator + externalDetectionFileName);

        // get detections as polygons and put on image as roi
        MaximumFinder maxima = new MaximumFinder();
        ImageProcessor getMaxima = labelImage.getProcessor().convertToByteProcessor();
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

    void visualizeExternalSegmentation(String externalSegmentationDirectory,
                                       String externalSegmentationFileName,
                                       ImagePlus originalImage,
                                       Image imageObject,
                                       boolean setDisplayRange,
                                       String channelSelector) {

        IJ.log("External segmentation file processed " + externalSegmentationFileName);

        originalImage.setOverlay(null);
        // TODO: there is an issue with some floating around ROIs that only vanish after setting this:
        originalImage.setRoi((Roi) null);

        RoiManager roiManager = new RoiManager(false);
        roiManager.reset();
        
        // Remove any overlays in original image
        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
        ImagePlus nucleus = imp_channels[imageObject.nucleus - 1];
        ImagePlus cytoplasm = imp_channels[imageObject.cytoplasm - 1];
        nucleus.setOverlay(null);
        cytoplasm.setOverlay(null);

        if (Objects.equals(channelSelector, "nucleus")) {

            // loads the nucleus label image
            ImagePlus labelImage = IJ.openImage(externalSegmentationDirectory + File.separator + externalSegmentationFileName);
            roiManager = label2Roi(labelImage);

            originalImage.setC( imageObject.nucleus);
            originalImage.setOverlay(null);

        } else if (Objects.equals(channelSelector, "cytoplasm")) {

            // loads the cell label image
            ImagePlus labelImage = IJ.openImage(externalSegmentationDirectory + File.separator + externalSegmentationFileName);
            roiManager = label2Roi(labelImage);

            originalImage.setC( imageObject.cytoplasm );
            originalImage.setOverlay(null);

        } else {

            IJ.error("Incorrect channel setting");

        }

        roiManager.moveRoisToOverlay(originalImage);
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
        roiManager.reset();
        roiManager.close();
    }

    /**
     * Converts gray scale label image to ROIs
     * After: <a href="https://github.com/BIOP/ijp-LaRoMe/blob/master/src/main/java/ch/epfl/biop/ij2command/Labels2Rois.java">...</a>
     *
     * @param labelImage a gray scale label image
     * @return ROI Manager with ROIs from label image
     */
    private static RoiManager label2Roi(ImagePlus labelImage) {

        // create new ROI manager
        RoiManager manager = new RoiManager(false);
        manager.reset();

        ImageProcessor labelImageProcessor = labelImage.getProcessor();

        // for filling the already found ROI with 0
        labelImageProcessor.setColor(0);

        Wand wand = new Wand(labelImageProcessor);

        int width = labelImageProcessor.getWidth();
        int height = labelImageProcessor.getHeight();

        int roiNumber = 0;

        for (int xCoordinate = 0; xCoordinate < width; xCoordinate++) {

            for (int yCoordinate = 0; yCoordinate < height; yCoordinate++) {

                float labelPixelValue = labelImageProcessor.getPixelValue(xCoordinate, yCoordinate);

                if ( labelPixelValue > 0.0 ) {

                    wand.autoOutline(xCoordinate, yCoordinate, labelPixelValue, labelPixelValue);

                    Roi roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.FREEROI);

                    // Name the Roi per value in the image
                    String roiName = "ID " + String.format("%04d", new Float(labelPixelValue).intValue());
                    roi.setName(roiName);

                    // ip.fill should use roi, otherwise make a rectangle that erases surrounding pixels
                    manager.add(roi, roiNumber);
                    labelImageProcessor.fill(roi);

                }

                roiNumber++;

            }

        }

        return manager;

    }

}
