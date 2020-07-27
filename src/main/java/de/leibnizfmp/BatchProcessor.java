package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.RGBStackMerge;
import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.LUT;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BatchProcessor {

    private final String inputDir;
    private final String outputDir;
    private final ArrayList<String> fileList;
    private final String fileFormat;
    private final int channelNumber;

    private final int nucleusChannel;
    private final int cytoplasmChannel;
    private final int organelleChannel;
    private final int measureChannel;

    private final boolean calibrationSetting;
    private final double pxSizeMicron;

    private final float kernelSizeNuc;
    private final double rollingBallRadiusNuc;
    private final String thresholdNuc;
    private final int erosionNuc;
    private final double minSizeNuc;
    private final double maxSizeNuc;
    private final double lowCircNuc;
    private final double highCircNuc;
    private final float kernelSizeCellArea;
    private final double rollingBallRadiusCellArea;
    private final int manualThresholdCellArea;
    private final double sigmaGaussCellSep;
    private final double prominenceCellSep;
    private final double minCellSize;
    private final double maxCellSize;
    private final double lowCircCellSize;
    private final double highCircCelLSize;
    private final double sigmaLoGOrga;
    private final double prominenceOrga;

    void processImage() {

        IJ.log("Starting batch processing");

        ArrayList<ArrayList<String>> distanceMeasureAll = new ArrayList<>();
        ArrayList<ArrayList<String>> cellMeasureAll = new ArrayList<>();

        for ( String fileName : fileList ) {

            IJ.log("Processing file: " + fileName );

            // get file names
            String fileNameWOtExt = fileName.substring(0, fileName.lastIndexOf("_S"));
            // get series number from file name
            int stringLength = fileName.length();
            String seriesNumberString;
            seriesNumberString = fileName.substring( fileName.lastIndexOf("_S") + 2 , stringLength );
            int seriesNumber = Integer.parseInt(seriesNumberString);

            // open image
            Image processingImage = new Image(inputDir, fileFormat, channelNumber, seriesNumber, nucleusChannel, cytoplasmChannel, organelleChannel, measureChannel);
            ImagePlus image = processingImage.openWithMultiseriesBF( fileName );

            if (calibrationSetting) {

                Calibration calibration = Image.calibrate(pxSizeMicron);
                image.setCalibration(calibration);
                IJ.log("Pixel size overwritten by: " + pxSizeMicron);

            } else {

                IJ.log("Metadata will not be overwritten");

            }

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

            // measure background in organelle channel
            String saveDir = outputDir + File.separator + fileName;
            try {

                Files.createDirectories(Paths.get(saveDir));

            } catch (IOException e) {

                IJ.log("Unable to create output directory");
                e.printStackTrace();
            }

            double backgroundOrganelle = measureDetectionBackground(backgroundMask, organelle);

            double backgroundMeasure = -1;
            ArrayList<ArrayList<ArrayList<String>>> resultLists;

            if ( measureChannel == 0) {

                IJ.log("No measure channel selected");
                resultLists = measureCell(manager, nucleusMask, detectionsFiltered, organelle, fileNameWOtExt, seriesNumber, backgroundOrganelle, backgroundMeasure, measureChannel, organelle);

            } else {

                ImagePlus measure = imp_channels[processingImage.measure - 1];
                IJ.log("Measuring in channel: " + measureChannel);
                backgroundMeasure = measureDetectionBackground(backgroundMask, measure);
                resultLists = measureCell(manager, nucleusMask, detectionsFiltered, organelle, fileNameWOtExt, seriesNumber, backgroundOrganelle, backgroundMeasure, measureChannel, measure);

            }

            // measure organelle distance and intensity, cell properties
            ArrayList<ArrayList<String>> distanceMeasure = resultLists.get(0);
            ArrayList<ArrayList<String>> cellMeasure = resultLists.get(1);

            distanceMeasureAll.addAll(distanceMeasure);
            cellMeasureAll.addAll(cellMeasure);

            saveResultImages(fileName, nucleusMask, cytoplasm, manager, nucleus, organelle, detectionsFiltered);

        }

        saveMeasurements(distanceMeasureAll, cellMeasureAll, outputDir, measureChannel);

        IJ.log("== Batch processing finished ==");

    }

    double measureDetectionBackground(ImagePlus background, ImagePlus measureImage) {

        IJ.log("Starting background measurements");

        ImagePlus backgroundDup = background.duplicate();

        backgroundDup.getProcessor().invert();

        ParticleAnalyzer cellAnalyzer = new ParticleAnalyzer(2048, 0, null,
                500, Image.calculateMaxArea( backgroundDup.getWidth(), backgroundDup.getHeight() ) );

        RoiManager backgroundRoiManager = new RoiManager(false);
        ParticleAnalyzer.setRoiManager( backgroundRoiManager );

        cellAnalyzer.analyze( backgroundDup );

        double backgroundMean;

        if ( backgroundRoiManager.getCount() > 0 ) {

            int imageWidth;
            int imageHeight;

            double backgroundSum = 0;
            double backgroundArea = 0;

            imageWidth = measureImage.getWidth();
            imageHeight = measureImage.getHeight();

            ImageProcessor measureImageProcessor = measureImage.getProcessor();
            ImageProcessor backgroundMaksProcessor = backgroundDup.getProcessor();

            short[] measureImagePixels = (short[]) measureImageProcessor.getPixels();
            byte[] backgroundMaskPixels  = (byte[]) backgroundMaksProcessor.getPixels();

            for (int y = 0; y < imageHeight; y++) {

                for (int x = 0; x < imageWidth; x++) {

                    if ( backgroundMaskPixels[x + y * imageWidth] < 0 ) {

                        backgroundSum += measureImagePixels[x + y * imageWidth];
                        backgroundArea += 1;

                    }

                }

            }

            backgroundMean = backgroundSum / backgroundArea;

       } else {

            backgroundMean = -1;

        }

        backgroundDup.close();
        backgroundRoiManager.reset();

        return backgroundMean;

    }

    ArrayList<ArrayList<ArrayList<String>>> measureCell(RoiManager manager, ImagePlus nucleusMask, ImagePlus detectionsFiltered, ImagePlus organelleChannel, String fileNameWOtExt, int seriesNumber, double backgroundMean, double backgroundMeasure, int measureChannel, ImagePlus measureChannelImage) {

        IJ.log("Starting measurements");

        double pxHeight = nucleusMask.getCalibration().pixelHeight;
        double pxWidth = nucleusMask.getCalibration().pixelWidth;
        double pxSize = pxHeight * pxWidth;

        ArrayList<ArrayList<String>> distanceList = new ArrayList<>();
        ArrayList<ArrayList<String>> cellList = new ArrayList<>();

        for ( int cellIndex = 1; cellIndex <  manager.getCount(); cellIndex++ ) {

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

                double detectionPosition =  nucEDM.getPixelValue(detectionPolygons.xpoints[detectIndex], detectionPolygons.ypoints[detectIndex]);
                double detectionValue = organelleChannel.getProcessor().getPixelValue(detectionPolygons.xpoints[detectIndex], detectionPolygons.ypoints[detectIndex]);

                double detectionMeasureValue;

                if (measureChannel == 0) {

                    detectionMeasureValue = 0;

                } else {

                    detectionMeasureValue = measureChannelImage.getProcessor().getPixelValue(detectionPolygons.xpoints[detectIndex], detectionPolygons.ypoints[detectIndex]);

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

    void saveMeasurements(ArrayList<ArrayList<String>> distanceList, ArrayList<ArrayList<String>> cellList, String outputDir, int measure) {

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

            cellFile = new StringBuilder("Name,Series, ell,Ferets,CellArea,NumDetections,MeanValueOrga,MeanBackgroundOrga,MeanValueMeasure,MeanBackgroundMeasure");
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

    void saveResultImages(String fileName, ImagePlus nucleusMask, ImagePlus cytoplasm, RoiManager manager, ImagePlus nucleus, ImagePlus organelle, ImagePlus detectionImage) {

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
        java.awt.Polygon detections = maxima.getMaxima(getMaxima, 1, false);
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
    
    BatchProcessor( String inputDirectory, String outputDirectory, ArrayList<String> filesToProcess, String format, int getChannelNumber) {

        inputDir = inputDirectory;
        outputDir = outputDirectory;

        fileList = filesToProcess;
        fileFormat = format;
        channelNumber = getChannelNumber;

        // image settings
        nucleusChannel = 1;
        cytoplasmChannel = 2;
        organelleChannel = 3;
        measureChannel = 4;
        calibrationSetting = false;
        pxSizeMicron = 0.1567095;

        // settings for nucleus settings
        kernelSizeNuc = 5;
        rollingBallRadiusNuc = 50;
        thresholdNuc = "Otsu";
        erosionNuc = 2;
        minSizeNuc = 100;
        maxSizeNuc = 20000;
        lowCircNuc = 0.0;
        highCircNuc = 1.00;

        // settings for cell area segmentation
        kernelSizeCellArea = 10;
        rollingBallRadiusCellArea = 50;
        manualThresholdCellArea = 200;

        // settings for cell separator
        sigmaGaussCellSep = 15;
        prominenceCellSep = 500;

        // settings for cell filter size
        minCellSize = 100;
        maxCellSize = 150000;
        lowCircCellSize = 0.0;
        highCircCelLSize = 1.0;

        // settings for organelle detection
        sigmaLoGOrga = 2;
        prominenceOrga = 200;

    }

    BatchProcessor( String inputDirectory,
                    String outputDirectory,
                    ArrayList<String> filesToProcess,
                    String format,
                    int getChannelNumber,
                    int getNucleusChannel,
                    int getCytoplasmChannel,
                    int getOrganelleChannel,
                    int getMeasure,
                    boolean getCalibrationSetting,
                    double getPxSizeMicron,
                    float getKernelSizeNuc,
                    double getRollingBallRadiusNuc,
                    String getThresholdNuc,
                    int getErosionNuc,
                    double getMinSizeNuc,
                    double getMaxSizeNuc,
                    double getLowCircNuc,
                    double getHighCircNuc,
                    float getKernelSizeCellArea,
                    double getRollingBallRadiusCellArea,
                    int getManualThresholdCellArea,
                    double getSigmaGaussCellSep,
                    double getProminenceCellSep,
                    double getMinCellSize,
                    double getMaxCellSize,
                    double getLowCircCellSize,
                    double getHighCircCelLSize,
                    double getSigmaLoGOrga,
                    double getProminenceOrga

    ) {

        inputDir = inputDirectory;
        outputDir = outputDirectory;

        fileList = filesToProcess;
        fileFormat = format;
        channelNumber = getChannelNumber;

        // image settings
        nucleusChannel = getNucleusChannel;
        cytoplasmChannel = getCytoplasmChannel;
        organelleChannel = getOrganelleChannel;
        measureChannel = getMeasure;
        calibrationSetting = getCalibrationSetting;
        pxSizeMicron = getPxSizeMicron;

        // settings for nucleus settings
        kernelSizeNuc = getKernelSizeNuc;
        rollingBallRadiusNuc = getRollingBallRadiusNuc;
        thresholdNuc = getThresholdNuc;
        erosionNuc = getErosionNuc;
        minSizeNuc = getMinSizeNuc;
        maxSizeNuc = getMaxSizeNuc;
        lowCircNuc = getLowCircNuc;
        highCircNuc = getHighCircNuc;

        // settings for cell area segmentation
        kernelSizeCellArea = getKernelSizeCellArea;
        rollingBallRadiusCellArea = getRollingBallRadiusCellArea;
        manualThresholdCellArea = getManualThresholdCellArea;

        // settings for cell separator
        sigmaGaussCellSep = getSigmaGaussCellSep;
        prominenceCellSep = getProminenceCellSep;

        // settings for cell filter size
        minCellSize = getMinCellSize;
        maxCellSize = getMaxCellSize;
        lowCircCellSize = getLowCircCellSize;
        highCircCelLSize = getHighCircCelLSize;

        // settings for organelle detection
        sigmaLoGOrga = getSigmaLoGOrga;
        prominenceOrga = getProminenceOrga;



    }

}
