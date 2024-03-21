package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.RGBStackMerge;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BatchResultSaver {

    protected static void saveCellMeasure(ArrayList<ArrayList<String>> cellList,
                                          String outputDir,
                                          String FileName,
                                          int measure) {

        StringBuilder cellFile;

        if ( measure == 0 ) {

            cellFile = new StringBuilder("identifier,series,cell,ferets,cellArea,numberDetections,orgaMeanIntensity,nucleusCenterMassX,nucleusCenterMassY,orgaMeanBackground");

        } else {

            cellFile = new StringBuilder("identifier,series,cell,ferets,cellArea,numberDetections,orgaMeanIntensity,nucleusCenterMassX,nucleusCenterMassY,orgaMeanBackground, measureMeanIntensity,measureMeanBackground");

        }
        cellFile.append("\n");

        for (ArrayList<String> strings : cellList) {

            cellFile.append(strings.get(0));
            cellFile.append(",");
            cellFile.append(strings.get(1));
            cellFile.append(",");
            cellFile.append(strings.get(2));
            cellFile.append(",");
            cellFile.append(strings.get(3));
            cellFile.append(",");
            cellFile.append(strings.get(4));
            cellFile.append(",");
            cellFile.append(strings.get(5));
            cellFile.append(",");
            cellFile.append(strings.get(6));
            cellFile.append(",");
            cellFile.append(strings.get(7));
            cellFile.append(",");
            cellFile.append(strings.get(8));
            cellFile.append(",");
            cellFile.append(strings.get(9));

            if ( measure > 0  ) {

                cellFile.append(",");
                cellFile.append(strings.get(10));
                cellFile.append(",");
                cellFile.append(strings.get(11));

            }

            cellFile.append("\n");

        }

        // now write to file
        try {

            Files.write(Paths.get(outputDir + File.separator + FileName), cellFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write cell measurement!");
            e.printStackTrace();

        }
    }

    protected static void saveDistanceMeasure(ArrayList<ArrayList<String>> distanceList, String outputDir, String FileName, int measure) {

        StringBuilder distanceFile;

        if ( measure == 0 ) {

            distanceFile = new StringBuilder("identifier,series,cell,detection,xDetection,yDetection,detectionDistanceRaw,detectionDistanceCalibrated,orgaDetectionPeak");

        } else {

            distanceFile = new StringBuilder("identifier,series,cell,detection,xDetection,yDetection,detectionDistanceRaw,detectionDistanceCalibrated,orgaDetectionPeak,measureDetectionPeak");

        }
        distanceFile.append("\n");

        // now append your data in a loop
        for (ArrayList<String> stringArrayList : distanceList) {

            distanceFile.append(stringArrayList.get(0));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(1));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(2));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(3));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(4));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(5));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(6));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(7));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(8));

            if ( measure > 0  ) {

                distanceFile.append(",");
                distanceFile.append(stringArrayList.get(9));

            }

            distanceFile.append("\n");

        }

        // now write to file
        try {
            Files.write(Paths.get(outputDir + File.separator + FileName), distanceFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write distance measurement!");
            e.printStackTrace();

        }
    }


    protected static void saveIntensityProfiles(ArrayList<ArrayList<String>> valueMeasure, String outputDir, String FileName, int measure) {

        StringBuilder valueFile;

        if ( measure == 0 ) {

            valueFile = new StringBuilder("identifier,series,cell,xIntensity,yIntensity,intensityDistanceRaw,intensityDistanceCalibrated,orgaIntensity");

        } else {

            valueFile = new StringBuilder("identifier,series,cell,xIntensity,yIntensity,intensityDistanceRaw,intensityDistanceCalibrated,orgaIntensity,measureIntensity");

        }
        valueFile.append("\n");

        // now append your data in a loop
        for (ArrayList<String> stringArrayList : valueMeasure) {

            valueFile.append(stringArrayList.get(0));
            valueFile.append(",");
            valueFile.append(stringArrayList.get(1));
            valueFile.append(",");
            valueFile.append(stringArrayList.get(2));
            valueFile.append(",");
            valueFile.append(stringArrayList.get(3));
            valueFile.append(",");
            valueFile.append(stringArrayList.get(4));
            valueFile.append(",");
            valueFile.append(stringArrayList.get(5));
            valueFile.append(",");
            valueFile.append(stringArrayList.get(6));
            valueFile.append(",");
            valueFile.append(stringArrayList.get(7));

            if ( measure > 0  ) {

                valueFile.append(",");
                valueFile.append(stringArrayList.get(8));

            }

            valueFile.append("\n");

        }

        // now write to file
        try {

            Files.write(Paths.get(outputDir + File.separator + FileName), valueFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write distance measurement!");
            e.printStackTrace();

        }
    }

    protected static void saveResultImages(String outputDir, String fileName,
                                 ImagePlus nucleusMask,
                                 ImagePlus cytoplasm,
                                 RoiManager manager,
                                 ImagePlus nucleus,
                                 ImagePlus organelle,
                                 ImagePlus detectionImage) {

        String saveDir = outputDir + File.separator + fileName;
        try {

            Files.createDirectories(Paths.get(saveDir));

        } catch (IOException e) {

            IJ.log("Unable to create output directory");
            e.printStackTrace();
        }

        Calibration cytocalib = cytoplasm.getCalibration();

        ImagePlus[] cellSegmentation = new ImagePlus[2];
        nucleusMask.setLut(LUT.createLutFromColor(Color.magenta));
        cellSegmentation[0] = nucleusMask;

        cytoplasm.setLut(LUT.createLutFromColor(Color.green));
        cellSegmentation[1] = cytoplasm;

        ImagePlus cellSegResult = RGBStackMerge.mergeChannels(cellSegmentation, false);
        cellSegResult.setCalibration(cytocalib);
        manager.moveRoisToOverlay(cellSegResult);

        FileSaver cellSaver = new FileSaver(cellSegResult);
        cellSaver.saveAsPng( saveDir + File.separator + "cellSegmentation.png");

        Calibration nucCalib = nucleus.getCalibration();

        nucleus.setLut(LUT.createLutFromColor(Color.gray));
        ParticleAnalyzer nucAnalyzer = new ParticleAnalyzer(2048, 0, null,
                0, Image.calculateMaxArea( nucleusMask.getWidth(), nucleusMask.getHeight() ) );

        RoiManager nucRoiManager = new RoiManager(false);
        ParticleAnalyzer.setRoiManager( nucRoiManager );
        nucAnalyzer.analyze( nucleusMask );

        nucleusMask.setCalibration(nucCalib);
        nucRoiManager.moveRoisToOverlay(nucleus);

        FileSaver nucSaver = new FileSaver(nucleus);
        nucSaver.saveAsPng( saveDir + File.separator + "nucSegmentation.png");

        Calibration detectCalib = detectionImage.getCalibration();
        ImagePlus detectionsResult = DetectionFilter.filterByCells(detectionImage, manager);

        // get detections as polygons and put on image as roi
        MaximumFinder maxima = new MaximumFinder();
        ImageProcessor getMaxima = detectionsResult.getProcessor().convertToByteProcessor();
        Polygon detections = maxima.getMaxima(getMaxima, 1, false);
        PointRoi roi = new PointRoi(detections);

        organelle.setLut(LUT.createLutFromColor(Color.gray));
        Overlay detectionOverlay = new Overlay();
        detectionOverlay.add(roi);
        organelle.setOverlay(detectionOverlay);
        nucRoiManager.moveRoisToOverlay(organelle);
        organelle.setCalibration(detectCalib);
        manager.moveRoisToOverlay(organelle);

        FileSaver organelleSave = new FileSaver(organelle);
        organelleSave.saveAsTiff(saveDir + File.separator + "detections.tiff");

        nucRoiManager.reset();

    }

}
