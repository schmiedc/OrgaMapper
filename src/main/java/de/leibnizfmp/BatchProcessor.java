package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BatchProcessor {

    void processImage(String testInDir, String fileEnding, int channelNumber, int seriesNumber,
                      boolean calibrationSetting, double pxSizeMicron,
                      int nucleusChannel, int cytoplasmChannel, int organelleChannel, int measureChannel,
                      ArrayList<String> fileList, int fileNumber,
                      float kernelSizeNuc, double rollingBallRadiusNuc, String thresholdNuc, int erosionNuc,
                      double minSizeNuc, double maxSizeNuc, double lowCircNuc, double highCircNuc,
                      float kernelSizeCellArea, double rollingBallRadiusCellArea, int manualThresholdCellArea,
                      double sigmaGaussCellSep, double prominenceCellSep,
                      double minCellSize, double maxCellSize, double lowCircCellSize, double highCircCelLSize,
                      double sigmaLoGOrga, double prominenceOrga, String outputDir) {

        Image processingImage = new Image(testInDir, fileEnding, channelNumber, seriesNumber, nucleusChannel, cytoplasmChannel, organelleChannel, measureChannel);

        IJ.log("Processing file: " + fileList.get( fileNumber ));

        // open image
        ImagePlus image = processingImage.openWithMultiseriesBF( fileList.get( fileNumber ) );

        if (calibrationSetting) {

            Calibration calibration = Image.calibrate("µm", pxSizeMicron);
            image.setCalibration(calibration);
            IJ.log("Pixel size overwritten by: " + pxSizeMicron);

        } else {

            IJ.log("Metadata will not be overwritten");

        }
        // get file names
        String fileName = fileList.get( fileNumber );
        String fileNameWOtExt = fileName.substring(0, fileName.lastIndexOf("_S"));

        // open individual channels
        ImagePlus[] imp_channels = ChannelSplitter.split(image);
        ImagePlus nucleus = imp_channels[processingImage.nucleus - 1];
        ImagePlus cytoplasm = imp_channels[processingImage.cytoplasm - 1];
        ImagePlus organelle = imp_channels[processingImage.organelle - 1];
        nucleus.setOverlay(null);

        // get nucleus masks
        ImagePlus nucleusMask = NucleusSegmenter.segmentNuclei(nucleus, kernelSizeNuc, rollingBallRadiusNuc, thresholdNuc, erosionNuc, minSizeNuc, maxSizeNuc, lowCircNuc, highCircNuc);

        // get filtered cell ROIs
        ImagePlus backgroundMask = CellAreaSegmenter.segmentCellArea(cytoplasm, kernelSizeCellArea, rollingBallRadiusCellArea, manualThresholdCellArea);
        ImagePlus separatedCells = CellSeparator.separateCells(nucleus, cytoplasm, sigmaGaussCellSep, prominenceCellSep);
        ImagePlus filteredCells = CellFilter.filterByCellSize(backgroundMask, separatedCells, minCellSize, maxCellSize, lowCircCellSize, highCircCelLSize);

        RoiManager manager;
        manager = CellFilter.filterByNuclei(filteredCells, nucleusMask);

        IJ.log("Found " + manager.getCount() + " cell(s)");

        // lysosome detection
        ImagePlus detections = OrganelleDetector.detectOrganelles(organelle, sigmaLoGOrga, prominenceOrga);
        ImagePlus detectionsFiltered = DetectionFilter.filterByNuclei(nucleusMask, detections);

        ArrayList<ArrayList<ArrayList<String>>>  resultLists = measureCell(manager, nucleusMask, detectionsFiltered, fileNameWOtExt, seriesNumber);

        ArrayList<ArrayList<String>> distanceMeasure = resultLists.get(0);
        ArrayList<ArrayList<String>> cellMeasure = resultLists.get(0);

        saveMeasurements(distanceMeasure, cellMeasure, outputDir);

    }

    ArrayList<ArrayList<ArrayList<String>>> measureCell(RoiManager manager, ImagePlus nucleusMask, ImagePlus detectionsFiltered, String fileNameWOtExt, int seriesNumber) {

        ArrayList<ArrayList<String>> distanceList = new ArrayList<>();
        ArrayList<ArrayList<String>> cellList = new ArrayList<>();

        for ( int cellIndex = 1; cellIndex <  manager.getCount(); cellIndex++ ) {

            IJ.log("Analyzing Cell: " + cellIndex);

            ImagePlus nucleusMaskDup = nucleusMask.duplicate();
            // TODO Fill holes
            ImageProcessor nucProcessor = nucleusMaskDup.getProcessor();

            // get the EDM of cell outside of nucleus
            nucProcessor.setValue(0.0);
            nucProcessor.fillOutside(manager.getRoi(cellIndex));
            nucProcessor.invert();
            EDM edmProcessor = new EDM();
            ImageProcessor nucEDM = edmProcessor.make16bitEDM(nucProcessor);

            // organelle detection in nucleus and within cell area
            ImagePlus detectionDup = detectionsFiltered.duplicate();
            ImageProcessor detectProcessor = detectionDup.getProcessor();
            detectProcessor.fillOutside(manager.getRoi(cellIndex));

            MaximumFinder maxima = new MaximumFinder();
            Polygon detectionPolygons = maxima.getMaxima(detectProcessor, 1, false);

            IJ.log("Cell " + cellIndex + " has " + detectionPolygons.npoints + " detection(s)");

            for ( int detectIndex = 0; detectIndex < detectionPolygons.npoints; detectIndex++ ) {

                double pixelValue =  nucEDM.getPixelValue(detectionPolygons.xpoints[detectIndex], detectionPolygons.ypoints[detectIndex]);

                ArrayList<String> valueList = new ArrayList<>();
                valueList.add(fileNameWOtExt);
                valueList.add(String.valueOf(seriesNumber));
                valueList.add(String.valueOf(cellIndex));
                valueList.add(String.valueOf(detectIndex));
                valueList.add(String.valueOf(pixelValue));

                distanceList.add(valueList);

            }


            ArrayList<String> cellValueList = new ArrayList<>();
            cellValueList.add(fileNameWOtExt);
            cellValueList.add(String.valueOf(seriesNumber));
            cellValueList.add(String.valueOf(cellIndex));

            Roi cellRoi = manager.getRoi(cellIndex);

            cellValueList.add(String.valueOf(cellRoi.getFeretsDiameter()));
            cellValueList.add(String.valueOf(detectionPolygons.npoints));

            cellList.add(cellValueList);

            nucleusMaskDup.close();
            detectionDup.close();

            IJ.log("Distance measurement finished");

        }

        ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<>();
        results.add(distanceList);
        results.add(cellList);

        return results;

    }

    void saveMeasurements(ArrayList<ArrayList<String>>  distanceList, ArrayList<ArrayList<String>>  cellList, String outputDir ) {

        final String lineSeparator = "\n";

        StringBuilder distanceFile = new StringBuilder("Name,Series,Cell,Organelle,Distance");
        distanceFile.append(lineSeparator);

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
            distanceFile.append(lineSeparator);

        }

        // now write to file
        try {
            Files.write(Paths.get(outputDir + "/organelleDistance.csv"), distanceFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write distance measurement!");
            e.printStackTrace();

        }

        StringBuilder cellFile = new StringBuilder("Name, Series, Cell, Ferets, NumDetections");
        cellFile.append(lineSeparator);

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
            cellFile.append(lineSeparator);

        }

        // now write to file
        try {

            Files.write(Paths.get(outputDir + "/cellMeasurements.csv"), cellFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write cell measurement!");
            e.printStackTrace();

        }
    }

}
