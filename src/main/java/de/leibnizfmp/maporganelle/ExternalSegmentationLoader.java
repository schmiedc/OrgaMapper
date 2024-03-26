package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.io.File;
import java.util.Objects;

public class ExternalSegmentationLoader {

    static String directory = "/home/schmiedc/FMP_Docs/Projects/OrgaMapper/2024-02-29_Revision/Feature_External-Detection/input_extSegDetect/";
    static String nucleusInputFile = "HeLa_NucSeg_1.tif";
    static String cellInputFile = "HeLa_CellSeg_1.tif";

    ImagePlus createExternalNucleusMask(Calibration calibration) {

        // loads the label image
        ImagePlus labelImage = IJ.openImage(directory + File.separator + nucleusInputFile);

        ImageProcessor labelImageProcessor = labelImage.getProcessor();
        labelImageProcessor.threshold(1);

        ImagePlus mask = new ImagePlus("afterThreshold", labelImageProcessor);

        mask.setCalibration(calibration);

        return mask;
    }

    RoiManager createExternalCellROIs() {

        ImagePlus labelImage = IJ.openImage(directory + File.separator + cellInputFile);
        RoiManager roiManager = label2Roi(labelImage);

        return roiManager;

    }

    void visualizeExternalSegmentation(ImagePlus originalImage,
                                       Image imageObject,
                                       boolean setDisplayRange,
                                       String channelSelector) {



        IJ.log("Starting visualization for external segmentation");

        originalImage.setOverlay(null);
        // TODO: there is an issue with some floating around ROIs that only vanish after setting this:
        originalImage.setRoi((Roi) null);

        RoiManager roiManager = null;
        
        // Remove any overlays in original image
        ImagePlus[] imp_channels = ChannelSplitter.split(originalImage);
        ImagePlus nucleus = imp_channels[imageObject.nucleus - 1];
        ImagePlus cytoplasm = imp_channels[imageObject.cytoplasm - 1];
        nucleus.setOverlay(null);
        cytoplasm.setOverlay(null);

        if (Objects.equals(channelSelector, "nucleus")) {

            // TODO: need to be get user defined
            // loads the nucleus label image
            ImagePlus labelImage = IJ.openImage(directory + File.separator + nucleusInputFile);
            roiManager = label2Roi(labelImage);

            originalImage.setC( imageObject.nucleus);
            originalImage.setOverlay(null);

        } else if (Objects.equals(channelSelector, "cytoplasm")) {

            // TODO: need to be get user defined
            // loads the cell label image
            ImagePlus labelImage = IJ.openImage(directory + File.separator + cellInputFile);
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
     * After: https://github.com/BIOP/ijp-LaRoMe/blob/master/src/main/java/ch/epfl/biop/ij2command/Labels2Rois.java
     *
     * @param labelImage a gray scale label image
     * @return ROI Manager with ROIs from label image
     */
    private static RoiManager label2Roi(ImagePlus labelImage) {

        // create new ROI manager and reset it
        RoiManager manager = new RoiManager();
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
