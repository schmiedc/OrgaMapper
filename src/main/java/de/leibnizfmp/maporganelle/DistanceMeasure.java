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

        ArrayList<ArrayList<String>> distanceList = new ArrayList<>();
        ArrayList<ArrayList<String>> cellList = new ArrayList<>();

        for ( int cellIndex = 0; cellIndex <  manager.getCount(); cellIndex++ ) {

            IJ.log("Analyzing Cell: " + cellIndex);

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

            // organelle detection in nucleus and within cell area
            ImagePlus detectionDup = detectionsFiltered.duplicate();
            ImageProcessor detectProcessor = detectionDup.getProcessor();
            detectProcessor.fillOutside(manager.getRoi(cellIndex));

            MaximumFinder maxima = new MaximumFinder();
            Polygon detectionPolygons = maxima.getMaxima(detectProcessor, 1, false);

            IJ.log("Cell " + cellIndex + " has " + detectionPolygons.npoints + " detection(s)");

            for ( int detectIndex = 0; detectIndex < detectionPolygons.npoints; detectIndex++ ) {

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
                distanceList.add(valueList);

            }

            ArrayList<String> cellValueList = new ArrayList<>();
            cellValueList.add(fileNameWOtExt);
            cellValueList.add(String.valueOf(seriesNumber));
            cellValueList.add(String.valueOf(cellIndex));
            Roi cellRoi = manager.getRoi(cellIndex);

            // Ferets diameter
            cellValueList.add(String.valueOf(cellRoi.getFeretsDiameter()));

            // cell area
            ImageStatistics cellStat = cellRoi.getStatistics();
            cellValueList.add(String.valueOf( cellStat.area * pxSize ));

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

            cellList.add(cellValueList);

            //nucleusMaskDup.close();
            detectionDup.close();

            IJ.log("Distance & cell measurements finished");

        }

        ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<>();
        results.add(distanceList);
        results.add(cellList);

        IJ.log("Measurement done!");

        return results;

    }
}
