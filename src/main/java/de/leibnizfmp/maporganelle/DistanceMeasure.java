package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.plugin.filter.Analyzer;
import ij.measure.ResultsTable;

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
                                                               ImagePlus measureChannelImage,
                                                               boolean distanceFromMembrane) {

        IJ.log("Starting measurements");

        double pxHeight = nucleusMask.getCalibration().pixelHeight;
        double pxWidth = nucleusMask.getCalibration().pixelWidth;
        double pxSize = pxHeight * pxWidth;

        ArrayList<ArrayList<String>> nucDistanceListCollected = new ArrayList<>();
        ArrayList<ArrayList<String>> measurementsPerCellCollected = new ArrayList<>();
        ArrayList<ArrayList<String>> intensityProfilesImage = new ArrayList<>();

        ArrayList<ArrayList<String>> membraneDistanceListCollected = new ArrayList<>();
        ArrayList<ArrayList<String>> membraneIntensityProfilesCollected = new ArrayList<>();

        for ( int cellIndex = 0; cellIndex <  manager.getCount(); cellIndex++ ) {

            IJ.log("Analyzing Cell: " + cellIndex);

            // creates EDM from the nucleus mask
            ImageProcessor nucEDM = createNucEDM(manager, nucleusMask, cellIndex);

            // organelle detection in nucleus and within cell area
            ImagePlus detectionDup = detectionsFiltered.duplicate();
            ImageProcessor detectProcessor = detectionDup.getProcessor();
            detectProcessor.fillOutside(manager.getRoi(cellIndex));

            MaximumFinder maxima = new MaximumFinder();
            Polygon detectionPolygons = maxima.getMaxima(detectProcessor, 1, false);

            IJ.log("Cell " + cellIndex + " has " + detectionPolygons.npoints + " detection(s)");

            IJ.log("Measuring distance from nucleus edge");
            // collects the measurements for each organelle
            ArrayList<ArrayList<String>> nucDistanceListCell = getDistanceDetections(organelleChannel,
                    fileNameWOtExt,
                    seriesNumber,
                    measureChannel,
                    measureChannelImage,
                    pxHeight,
                    cellIndex,
                    nucEDM,
                    detectionPolygons);

            nucDistanceListCollected.addAll(nucDistanceListCell);

            IJ.log("Measuring cell parameters");
            // collects the measurements for each cell
            ArrayList<String> measurementPerCell = getCellMeasurements(manager,
                    organelleChannel,
                    fileNameWOtExt,
                    seriesNumber,
                    backgroundMean,
                    backgroundMeasure,
                    measureChannel,
                    measureChannelImage,
                    pxSize,
                    cellIndex,
                    detectionPolygons,
                    nucleusMask);

            measurementsPerCellCollected.add(measurementPerCell);

            IJ.log("Measuring intensity profiles from nucleus edge");
            // collects the intensity profiles for each cell
            ArrayList<ArrayList<String>> nucIntensityProfilesCells = IntensityProfile(organelleChannel,
                    measureChannelImage,
                    manager,
                    nucEDM,
                    measureChannel,
                    fileNameWOtExt,
                    seriesNumber,
                    pxHeight,
                    cellIndex);

            intensityProfilesImage.addAll(nucIntensityProfilesCells);

            // measure detection distance from membrane edge
            if (distanceFromMembrane) {

                IJ.log("Measuring distance from membrane edge");

                ImageProcessor membraneEDM = createMembraneEDM(manager, nucleusMask, cellIndex);

                ArrayList<ArrayList<String>> membraneDistanceListCell = getDistanceDetections(organelleChannel,
                        fileNameWOtExt,
                        seriesNumber,
                        measureChannel,
                        measureChannelImage,
                        pxHeight,
                        cellIndex,
                        membraneEDM,
                        detectionPolygons);

                membraneDistanceListCollected.addAll(membraneDistanceListCell);

                ArrayList<ArrayList<String>> membraneIntensityProfilesCells = IntensityProfile(organelleChannel,
                        measureChannelImage,
                        manager,
                        membraneEDM,
                        measureChannel,
                        fileNameWOtExt,
                        seriesNumber,
                        pxHeight,
                        cellIndex);

                membraneIntensityProfilesCollected.addAll(membraneIntensityProfilesCells);

            }

            detectionDup.close();

            IJ.log("Distance & cell measurements finished");

        }

        ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<>();
        results.add(nucDistanceListCollected);
        results.add(measurementsPerCellCollected);
        results.add(intensityProfilesImage);

        if (distanceFromMembrane) {

            results.add(membraneDistanceListCollected);
            results.add(membraneIntensityProfilesCollected);

        }

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
                                                         Polygon detectionPolygons,
                                                         ImagePlus nucleusMask) {

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

        // duplicate the nucleus mask
        ImagePlus nucleusMaskDup = nucleusMask.duplicate();
        ImageProcessor nucProcessor = nucleusMaskDup.getProcessor();

        // get specific nucleus mask
        nucProcessor.setValue(0.0);
        nucProcessor.fillOutside(manager.getRoi(cellIndex));
        ImagePlus singleNucleusImage = new ImagePlus("singleNucleusImage", nucProcessor);

        // get center of mass for specific nucleus mask
        ResultsTable centerOfMassResults = new ResultsTable();
        Analyzer centerOfMassAnalyzer = new Analyzer(singleNucleusImage, Analyzer.CENTER_OF_MASS, centerOfMassResults);
        centerOfMassAnalyzer.measure();

        // measure mean background
        if ( backgroundMean >= 0 ) {

            cellValueList.add( String.valueOf(backgroundMean));

        } else {

            cellValueList.add( "NaN" );

        }

        cellValueList.add( String.valueOf( centerOfMassResults.getValue("XM", 0) ) );
        cellValueList.add( String.valueOf( centerOfMassResults.getValue("YM", 0) ) );

        // Optional measurements based on measurement channel
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


    private static ImageProcessor createNucEDM(RoiManager manager,
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

    private static ImageProcessor createMembraneEDM(RoiManager manager,
                                                    ImagePlus nucleusMask,
                                                    int cellIndex) {

        ImagePlus nucMaskDup = nucleusMask.duplicate();
        ImageProcessor membraneMaskProcessor = nucMaskDup.getProcessor();

        // set values outside of cell roi to 0
        // removes all other nuclei from mask image
        membraneMaskProcessor.setValue(0.0);
        membraneMaskProcessor.fillOutside(manager.getRoi(cellIndex));

        // set values inside of cell to 255
        // creates cell mask for specific cellIndex
        membraneMaskProcessor.setValue(255.0);
        membraneMaskProcessor.fill(manager.getRoi(cellIndex));

        // setup EDM
        membraneMaskProcessor.convertToByteProcessor();
        EDM edmProcessor = new EDM();
        edmProcessor.setup("", nucMaskDup);

        // compute Float EDM for large distances
        return edmProcessor.makeFloatEDM(membraneMaskProcessor, 0, false);

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

        ArrayList<ArrayList<String>> nucDistanceListCell = new ArrayList<>();

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
            valueList.add(String.valueOf(detectionPolygons.xpoints[detectIndex]));
            valueList.add(String.valueOf(detectionPolygons.ypoints[detectIndex]));
            valueList.add(String.valueOf(detectionPosition));
            valueList.add(String.valueOf(detectionPosition * pxHeight));
            valueList.add(String.valueOf(detectionValue));
            valueList.add(String.valueOf(detectionMeasureValue));
            nucDistanceListCell.add(valueList);

        }
        return nucDistanceListCell;
    }

    static ArrayList<ArrayList<String>> IntensityProfile(ImagePlus organelleChannel,
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