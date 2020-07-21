/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import ij.Prefs;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 */
@Plugin(type = Command.class, menuPath = "Plugins>Cellular Imaging>Map Organelle")
public class MapOrganelle<T extends RealType<T>>  implements Command {

    @Override
    public void run() {

        Prefs.blackBackground = true;

        IJ.log("Starting map-organelle plugin");

        InputGuiFiji start = new InputGuiFiji();
        start.createWindow();

    }

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {

        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        Prefs.blackBackground = true;
        ij.command().run(MapOrganelle.class, true);

        boolean runTest = false;

        if ( runTest ) {

            //String testInDir = "/data1/FMP_Docs/Projects/orgaPosJ_ME/Plugin_InputTest/";
            String testInDir = "/data1/FMP_Docs/Projects/orgaPosJ_ME/Plugin_InputTest_nd2/";
            String testOutDir = "/data1/FMP_Docs/Projects/orgaPosJ_ME/Plugin_OutputTest";
            int channelNumber = 3;
            //String fileEnding = ".tif";
            String fileEnding = ".nd2";
            String settings = "setting";

            FileList getFileList = new FileList(fileEnding);
            ArrayList<String> fileList = getFileList.getFileMultiSeriesList(testInDir);

            for (String file : fileList) {
                System.out.println(file);
            }

            // settings for nucleus settings
            float kernelSizeNuc = 5;
            double rollingBallRadiusNuc = 50;
            String thresholdNuc = "Otsu";
            int erosionNuc = 2;
            double minSizeNuc = 100;
            double maxSizeNuc = 20000;
            double lowCircNuc = 0.0;
            double highCircNuc = 1.00;

            // settings for cell area segmentation
            float kernelSizeCellArea = 10;
            double rollingBallRadiusCellArea = 50;
            int manualThresholdCellArea = 200;

            // settings for cell separator
            double sigmaGaussCellSep = 15;
            double prominenceCellSep = 500;

            // settings for cell filter size
            double minCellSize = 100;
            double maxCellSize = 150000;
            double lowCircCellSize = 0.0;
            double highCircCelLSize = 1.0;

            // settings for organelle detection
            double sigmaLoGOrga = 2;
            double prominenceOrga = 200;

            // image settings
            boolean calibrationSetting = false;
            double pxSizeMicron = 0.1567095;
            int nucleusChannel = 1;
            int cytoplasmChannel = 2;
            int organelleChannel = 3;
            int measure = 0;

            int seriesNumber = 0;

            Image previewImage = new Image(testInDir, fileEnding, channelNumber, seriesNumber, 1, 2, 3, 0);

            ImagePlus testimage = previewImage.openWithMultiseriesBF(fileList.get(0));

            String fileName = fileList.get(0);
            String fileNameWOtExt = fileName.substring(0, fileName.lastIndexOf("_S"));

            ImagePlus[] imp_channels = ChannelSplitter.split(testimage);
            ImagePlus nucleus = imp_channels[previewImage.nucleus - 1];
            ImagePlus cytoplasm = imp_channels[previewImage.cytoplasm - 1];
            ImagePlus organelle = imp_channels[previewImage.organelle - 1];
            nucleus.setOverlay(null);


            // get nucleus masks
            ImagePlus nucleusMask = NucleusSegmenter.segmentNuclei(nucleus, kernelSizeNuc, rollingBallRadiusNuc, thresholdNuc, erosionNuc, minSizeNuc, maxSizeNuc, lowCircNuc, highCircNuc);

            // get filtered cell ROIs
            ImagePlus backgroundMask = CellAreaSegmenter.segmentCellArea(cytoplasm, kernelSizeCellArea, rollingBallRadiusCellArea, manualThresholdCellArea);
            ImagePlus separatedCells = CellSeparator.separateCells(nucleus, cytoplasm, sigmaGaussCellSep, prominenceCellSep);
            ImagePlus filteredCells = CellFilter.filterByCellSize(backgroundMask, separatedCells, minCellSize, maxCellSize, lowCircCellSize, highCircCelLSize);

            RoiManager manager;
            manager = CellFilter.filterByNuclei(filteredCells, nucleusMask);

            // lysosome detection
            ImagePlus detections = OrganelleDetector.detectOrganelles(organelle, sigmaLoGOrga, prominenceOrga);
            ImagePlus detectionsFiltered = DetectionFilter.filterByNuclei(nucleusMask, detections);

            IJ.log("Found " + manager.getCount() + " cell(s)");

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

                IJ.log("Cell " + cellIndex + " has " + String.valueOf(detectionPolygons.npoints) + " detection(s)");

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

            //final String lineSeparator = System.getProperty("line separator");
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
            Files.write(Paths.get(testOutDir + "/organelleDistance.csv"), distanceFile.toString().getBytes());

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
            Files.write(Paths.get(testOutDir + "/cellMeasurements.csv"), cellFile.toString().getBytes());





            //ImagePlus nucEDMImage = new ImagePlus("EDM", nucEDM);
            //FileSaver saver = new FileSaver(nucEDMImage);
            //saver.saveAsTiff(testOutDir + File.separator + "nucEDM.tif");


            //PreviewGui guiTest = new PreviewGui(testInDir, testOutDir, fileList, fileEnding, 3);
            //guiTest.setUpGui();

            //InputGuiFiji guiTest = new InputGuiFiji(testInDir, testOutDir, channelNumber,fileEnding, settings);
            //guiTest.createWindow();

        }

    }

}
