package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.RoiManager;

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
            Image processingImage = new Image(inputDir,
                    fileFormat,
                    channelNumber,
                    seriesNumber,
                    nucleusChannel,
                    cytoplasmChannel,
                    organelleChannel,
                    measureChannel);

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
            ImagePlus nucleusMask = NucleusSegmenter.segmentNuclei(nucleus,
                    kernelSizeNuc,
                    rollingBallRadiusNuc,
                    thresholdNuc,
                    erosionNuc,
                    minSizeNuc,
                    maxSizeNuc,
                    lowCircNuc,
                    highCircNuc);

            // get filtered cell ROIs
            ImagePlus backgroundMask = CellAreaSegmenter.segmentCellArea(cytoplasm,
                    kernelSizeCellArea, rollingBallRadiusCellArea, manualThresholdCellArea);

            ImagePlus separatedCells = CellSeparator.separateCells(nucleus,
                    cytoplasm, sigmaGaussCellSep, prominenceCellSep);

            ImagePlus filteredCells = CellFilter.filterByCellSize(backgroundMask,
                    separatedCells, minCellSize, maxCellSize, lowCircCellSize, highCircCelLSize);

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

            double backgroundOrganelle = BackgroundMeasure.measureDetectionBackground(backgroundMask, organelle);

            double backgroundMeasure = -1;
            ArrayList<ArrayList<ArrayList<String>>> resultLists;

            if ( measureChannel == 0) {

                IJ.log("No measure channel selected");
                resultLists = DistanceMeasure.measureCell(manager,
                        nucleusMask,
                        detectionsFiltered,
                        organelle,
                        fileNameWOtExt,
                        seriesNumber,
                        backgroundOrganelle,
                        backgroundMeasure,
                        measureChannel,
                        organelle);

            } else {

                ImagePlus measure = imp_channels[processingImage.measure - 1];
                IJ.log("Measuring in channel: " + measureChannel);
                backgroundMeasure = BackgroundMeasure.measureDetectionBackground(backgroundMask, measure);
                resultLists = DistanceMeasure.measureCell(manager,
                        nucleusMask,
                        detectionsFiltered,
                        organelle,
                        fileNameWOtExt,
                        seriesNumber,
                        backgroundOrganelle,
                        backgroundMeasure,
                        measureChannel,
                        measure);

            }

            // measure organelle distance and intensity, cell properties
            ArrayList<ArrayList<String>> distanceMeasure = resultLists.get(0);
            ArrayList<ArrayList<String>> cellMeasure = resultLists.get(1);

            distanceMeasureAll.addAll(distanceMeasure);
            cellMeasureAll.addAll(cellMeasure);

            BatchResultSaver.saveResultImages(outputDir, fileName, nucleusMask, cytoplasm, manager, nucleus, organelle, detectionsFiltered);

        }

        BatchResultSaver.saveMeasurements(distanceMeasureAll, cellMeasureAll, outputDir, measureChannel);

        IJ.log("== Batch processing finished ==");
        IJ.showProgress(1);

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
