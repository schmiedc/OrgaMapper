package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DistanceMeasure {

    static ArrayList<ArrayList<ArrayList<String>>> measureCell(RoiManager manager,
                                                               ImagePlus nucleusMask,
                                                               ImagePlus detectionsFiltered,
                                                               ImagePlus organelleChannel,
                                                               String fileNameWOtExt,
                                                               int seriesNumber,
                                                               double backgroundMean,
                                                               double backgroundMeasure,
                                                               int measureChannel,
                                                               ImagePlus measureChannelImage) {

        IJ.log("Starting measurements");

        double pxHeight = nucleusMask.getCalibration().pixelHeight;
        double pxWidth = nucleusMask.getCalibration().pixelWidth;
        double pxSize = pxHeight * pxWidth;

        ArrayList<ArrayList<String>> distanceListImage = new ArrayList<>();
        ArrayList<ArrayList<String>> cellList = new ArrayList<>();

        for ( int cellIndex = 0; cellIndex <  manager.getCount(); cellIndex++ ) {

            IJ.log("Analyzing Cell: " + cellIndex);

            ImageProcessor nucEDM = createEDM(manager, nucleusMask, cellIndex);

            // organelle detection in nucleus and within cell area
            ImagePlus detectionDup = detectionsFiltered.duplicate();
            ImageProcessor detectProcessor = detectionDup.getProcessor();
            detectProcessor.fillOutside(manager.getRoi(cellIndex));

            MaximumFinder maxima = new MaximumFinder();
            Polygon detectionPolygons = maxima.getMaxima(detectProcessor, 1, false);

            IJ.log("Cell " + cellIndex + " has " + detectionPolygons.npoints + " detection(s)");

            ArrayList<ArrayList<String>> distanceListCell = getDistanceDetections(organelleChannel,
                    fileNameWOtExt,
                    seriesNumber,
                    measureChannel,
                    measureChannelImage,
                    pxHeight,
                    cellIndex,
                    nucEDM,
                    detectionPolygons);

            for ( int i = 0; i < distanceListCell.size(); ++i ) {

                distanceListImage.add( distanceListCell.get(i) );

            }

            ArrayList<String> cellValueList = getCellMeasurements(manager,
                    organelleChannel,
                    fileNameWOtExt,
                    seriesNumber,
                    backgroundMean,
                    backgroundMeasure,
                    measureChannel,
                    measureChannelImage,
                    pxSize,
                    cellIndex,
                    detectionPolygons);

            cellList.add(cellValueList);

            //nucleusMaskDup.close();
            detectionDup.close();

            //intensityProfile(nucEDM, organelleChannel, manager, cellIndex);


            IJ.log("Distance & cell measurements finished");

        }

        ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<>();
        results.add(distanceListImage);
        results.add(cellList);

        IJ.log("Measurement done!");

        return results;

    }

    private static ArrayList<String> getCellMeasurements(RoiManager manager, ImagePlus organelleChannel, String fileNameWOtExt, int seriesNumber, double backgroundMean, double backgroundMeasure, int measureChannel, ImagePlus measureChannelImage, double pxSize, int cellIndex, Polygon detectionPolygons) {

        ArrayList<String> cellValueList = new ArrayList<>();
        cellValueList.add(fileNameWOtExt);
        cellValueList.add(String.valueOf(seriesNumber));
        cellValueList.add(String.valueOf(cellIndex));
        Roi cellRoi = manager.getRoi(cellIndex);

        // Ferets diameter
        cellValueList.add(String.valueOf(cellRoi.getFeretsDiameter()));

        // cell area
        ImageStatistics cellStat = cellRoi.getStatistics();
        cellValueList.add(String.valueOf( cellStat.area * pxSize));

        // detection number
        cellValueList.add(String.valueOf(detectionPolygons.npoints));

        // intensity in detection channel
        organelleChannel.setRoi( manager.getRoi(cellIndex) );
        cellValueList.add( String.valueOf(organelleChannel.getProcessor().getStats().mean ) );


        if ( backgroundMean >= 0 ) {

            cellValueList.add( String.valueOf(backgroundMean));

        } else {

            cellValueList.add( "NaN" );

        }

        if ( measureChannel > 0) {

            measureChannelImage.setRoi( manager.getRoi(cellIndex) );
            cellValueList.add( String.valueOf(measureChannelImage.getProcessor().getStats().mean ) );

            if ( backgroundMeasure >= 0 ) {

                cellValueList.add( String.valueOf(backgroundMeasure));

            } else {

                cellValueList.add( "NaN" );

            }

        }
        return cellValueList;
    }

    private static ArrayList<ArrayList<String>> getDistanceDetections(ImagePlus organelleChannel,
                                                                      String fileNameWOtExt,
                                                                      int seriesNumber,
                                                                      int measureChannel,
                                                                      ImagePlus measureChannelImage,
                                                                      double pxHeight,
                                                                      int cellIndex,
                                                                      ImageProcessor nucEDM,
                                                                      Polygon detectionPolygons) {

        ArrayList<ArrayList<String>> distanceListCell = new ArrayList<>();

        for (int detectIndex = 0; detectIndex < detectionPolygons.npoints; detectIndex++ ) {

            double detectionPosition =  nucEDM.getPixelValue(detectionPolygons.xpoints[detectIndex],
                    detectionPolygons.ypoints[detectIndex]);

            double detectionValue = organelleChannel.getProcessor().getPixelValue(detectionPolygons.xpoints[detectIndex],
                    detectionPolygons.ypoints[detectIndex]);

            double detectionMeasureValue;

            if (measureChannel == 0) {

                detectionMeasureValue = 0;

            } else {

                detectionMeasureValue = measureChannelImage.getProcessor().getPixelValue(detectionPolygons.xpoints[detectIndex],
                        detectionPolygons.ypoints[detectIndex]);

            }

            ArrayList<String> valueList = new ArrayList<>();
            valueList.add(fileNameWOtExt);
            valueList.add(String.valueOf(seriesNumber));
            valueList.add(String.valueOf(cellIndex));
            valueList.add(String.valueOf(detectIndex));
            valueList.add(String.valueOf(detectionPosition));
            valueList.add(String.valueOf(detectionPosition * pxHeight));
            valueList.add(String.valueOf(detectionValue));
            valueList.add(String.valueOf(detectionMeasureValue));
            distanceListCell.add(valueList);

        }
        return distanceListCell;
    }

    private static ImageProcessor createEDM(RoiManager manager,
                                            ImagePlus nucleusMask,
                                            int cellIndex) {

        ImagePlus nucleusMaskDup = nucleusMask.duplicate();
        ImageProcessor nucProcessor = nucleusMaskDup.getProcessor();

        // get the EDM of cell outside of nucleus
        nucProcessor.setValue(0.0);
        nucProcessor.fillOutside(manager.getRoi(cellIndex));
        nucProcessor.invert();
        nucProcessor.convertToByteProcessor();
        EDM edmProcessor = new EDM();
        edmProcessor.setup("", nucleusMaskDup);
        ImageProcessor nucEDM = edmProcessor.makeFloatEDM(nucProcessor, 0, false);
        return nucEDM;
    }

    static void intensityProfile(ImageProcessor nucEDM,
                                 ImagePlus imageToProfile,
                                 RoiManager manager,
                                 int cellIndex) {

        Roi cellRoi = manager.getRoi(cellIndex);

        ArrayList<ArrayList<String>> valueListCell = new ArrayList<>();

        for (Point p : cellRoi) {

            int corrX = p.x;
            int corrY = p.y;

            float valueImage = imageToProfile.getProcessor().getPixelValue(p.x, p.y);
            float valueDistance = nucEDM.getPixelValue(p.x, p.y);

            ArrayList<String> valueList = new ArrayList<>();
            valueList.add(String.valueOf(corrX));
            valueList.add(String.valueOf(corrY));
            valueList.add(String.valueOf(valueImage));
            valueList.add(String.valueOf(valueDistance));
            valueListCell.add(valueList);

        }

        final String lineSeparator = "\n";
        StringBuilder distanceFile;
        distanceFile = new StringBuilder("X,Y,Int,Dist");
        distanceFile.append(lineSeparator);

        // now append your data in a loop
        for (ArrayList<String> stringArrayList : valueListCell) {

            distanceFile.append(stringArrayList.get(0));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(1));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(2));
            distanceFile.append(",");
            distanceFile.append(stringArrayList.get(3));

            distanceFile.append(lineSeparator);

        }

        // now write to file
        try {
            String outputDir = "/home/schmiedc/Desktop/Test/test_tif/output/";
            Files.write(Paths.get(outputDir + "/intDistance" + cellIndex + ".csv"), distanceFile.toString().getBytes());

        } catch (IOException e) {

            IJ.log("Unable to write distance measurement!");
            e.printStackTrace();

        }

    }
}
