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

    static void saveMeasurements(ArrayList<ArrayList<String>> distanceList, ArrayList<ArrayList<String>> cellList, String outputDir, int measure) {

        IJ.log("Saving measurements to: " + outputDir);

        final String lineSeparator = "\n";
        StringBuilder distanceFile;

        if ( measure == 0 ) {

            distanceFile = new StringBuilder("Name,Series,Cell,Detection,DistanceRaw,DistanceCal,PeakDetectionInt");
            distanceFile.append(lineSeparator);

        } else {

            distanceFile = new StringBuilder("Name,Series,Cell,Detection,DistanceRaw,DistanceCal,PeakDetectionInt,PeakMeasureInt");
            distanceFile.append(lineSeparator);

        }

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

            if ( measure > 0  ) {

                distanceFile.append(",");
                distanceFile.append(stringArrayList.get(7));

            }

            distanceFile.append(lineSeparator);

        }

        // now write to file
        try {
            Files.write(Paths.get(outputDir + "/organelleDistance.csv"), distanceFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write distance measurement!");
            e.printStackTrace();

        }

        StringBuilder cellFile;

        if ( measure == 0 ) {

            cellFile = new StringBuilder("Name, Series, Cell, Ferets, CellArea, NumDetections, MeanValueOrga, MeanBackgroundOrga");
            cellFile.append(lineSeparator);

        } else {

            cellFile = new StringBuilder("Name,Series, Cell,Ferets,CellArea,NumDetections,MeanValueOrga,MeanBackgroundOrga,MeanValueMeasure,MeanBackgroundMeasure");
            cellFile.append(lineSeparator);

        }

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

            if ( measure > 0  ) {

                cellFile.append(",");
                cellFile.append(strings.get(8));
                cellFile.append(",");
                cellFile.append(strings.get(9));

            }

            cellFile.append(lineSeparator);

        }

        // now write to file
        try {

            Files.write(Paths.get(outputDir + "/cellMeasurements.csv"), cellFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write cell measurement!");
            e.printStackTrace();

        }

        IJ.log("Measurements saved");
    }

    static void saveResultImages(String outputDir, String fileName,
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
        organelleSave.saveAsTiff(saveDir + File.separator + "detections.png");

        nucRoiManager.reset();

    }
}
