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
        ArrayList<ArrayList<String>> valueListImage = new ArrayList<>();

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

            distanceListImage.addAll(distanceListCell);

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

            ArrayList<ArrayList<String>> valueListCell = intensityProfile(organelleChannel,
                    measureChannelImage,
                    manager,
                    nucEDM,
                    measureChannel,
                    fileNameWOtExt,
                    seriesNumber,
                    pxSize,
                    cellIndex);

            valueListImage.addAll(valueListCell);

            detectionDup.close();

            IJ.log("Distance & cell measurements finished");

        }

        ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<>();
        results.add(distanceListImage);
        results.add(cellList);
        results.add(valueListImage);

        IJ.log("Measurement done!");

        return results;

    }

    private static ArrayList<String> getCellMeasurements(RoiManager manager,
                                                         ImagePlus organelleChannel,
                                                         String fileNameWOtExt,
                                                         int seriesNumber,
                                                         double backgroundMean,
                                                         double backgroundMeasure,
                                                         int measureChannel,
                                                         ImagePlus measureChannelImage,
                                                         double pxSize,
                                                         int cellIndex,
                                                         Polygon detectionPolygons) {

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
            valueList.add(String.valueOf(detectionPolygons.xpoints[detectIndex]));
            valueList.add(String.valueOf(detectionPolygons.ypoints[detectIndex]));
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
        return edmProcessor.makeFloatEDM(nucProcessor, 0, false);
    }

    static ArrayList<ArrayList<String>> intensityProfile(ImagePlus organelleChannel,
                                                         ImagePlus measureChannelImage,
                                                         RoiManager manager,
                                                         ImageProcessor nucEDM,
                                                         int measureChannel,
                                                         String fileNameWOtExt,
                                                         int seriesNumber,
                                                         double pxHeight,
                                                         int cellIndex) {

        Roi cellRoi = manager.getRoi(cellIndex);

        ArrayList<ArrayList<String>> valueListCell = new ArrayList<>();

        for (Point p : cellRoi) {

            float valueDistance = nucEDM.getPixelValue(p.x, p.y);
            float valueImage = organelleChannel.getProcessor().getPixelValue(p.x, p.y);

            float valueMeasure;

            if (measureChannel == 0) {

                valueMeasure = 0;

            } else {

                valueMeasure = measureChannelImage.getProcessor().getPixelValue(p.x, p.y);

            }

            ArrayList<String> valueList = new ArrayList<>();
            valueList.add(fileNameWOtExt);
            valueList.add(String.valueOf(seriesNumber));
            valueList.add(String.valueOf(cellIndex));
            valueList.add(String.valueOf(p.x));
            valueList.add(String.valueOf(p.y));
            valueList.add(String.valueOf(valueDistance));
            valueList.add(String.valueOf(valueDistance * pxHeight));
            valueList.add(String.valueOf(valueImage));
            valueList.add(String.valueOf(valueMeasure));
            valueListCell.add(valueList);

        }

        return valueListCell;

    }

}