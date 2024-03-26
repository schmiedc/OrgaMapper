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
    private final boolean distanceFromMembraneSetting;
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
    private final boolean invertCellImageSetting;

    // settings for external segmentation
    private final boolean useInternalNucleusSegmentation;
    private final boolean useInternalCellSegmentation;
    private final boolean useInternalDetection;
    private String extNucleusSegmentationDirectory = "/home/schmiedc/FMP_Docs/Projects/OrgaMapper/2024-02-29_Revision/Feature_External-Detection/input_extSegDetect/";
    private String extCellSegmentationDirectory = extNucleusSegmentationDirectory;
    private String extDetectionDirectory = extNucleusSegmentationDirectory;
    private String externalNucleusInputFile = "HeLa_NucSeg_1.tif";
    private String externalCellInputFile = "HeLa_CellSeg_1.tif";
    private String externalOrganelleInputFile = "HeLa_Detect_1.tif";

    void processImage() {

        IJ.log("Starting batch processing");

        ArrayList<ArrayList<String>> nucDistanceMeasureAll = new ArrayList<>();
        ArrayList<ArrayList<String>> cellMeasureAll = new ArrayList<>();

        ArrayList<ArrayList<String>> membraneDistanceMeasureAll = new ArrayList<>();
        ArrayList<ArrayList<String>> membraneIntensityProfilesAll = new ArrayList<>();


        for ( String fileName : fileList ) {

            IJ.log("Processing file: " + fileName );

            // get file names
            String fileNameWOtExt = fileName.substring(0, fileName.lastIndexOf("_S"));

            // get series number from file name
            int stringLength = fileName.length();
            String seriesNumberString;
            seriesNumberString = fileName.substring( fileName.lastIndexOf("_S") + 2 , stringLength );
            int seriesNumber = Integer.parseInt(seriesNumberString);

            // define and open image
            Image processingImage = new Image(
                    inputDir,
                    fileFormat,
                    channelNumber,
                    seriesNumber,
                    nucleusChannel,
                    cytoplasmChannel,
                    organelleChannel,
                    measureChannel);

            ImagePlus image = processingImage.openWithMultiseriesBF( fileName );

            // override calibration settings if selected
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
            ImagePlus nucleusMask;

            if (useInternalNucleusSegmentation) {

                nucleusMask = NucleusSegmenter.segmentNuclei(
                        nucleus,
                        kernelSizeNuc,
                        rollingBallRadiusNuc,
                        thresholdNuc,
                        erosionNuc,
                        minSizeNuc,
                        maxSizeNuc,
                        lowCircNuc,
                        highCircNuc);

            } else {

                ExternalSegmentationLoader externalSegmentation = new ExternalSegmentationLoader();

                nucleusMask = externalSegmentation.createExternalSegmentationMask(
                        image.getCalibration(),
                        "HeLa_NucSeg_1.tif");

            }

            // get cell ROI manager
            RoiManager manager;
            ImagePlus backgroundMask;

            if (useInternalCellSegmentation) {

                IJ.log("Using internal segmentation");

                // get cell segmentations
                backgroundMask = CellAreaSegmenter.segmentCellArea(
                        cytoplasm,
                        kernelSizeCellArea,
                        rollingBallRadiusCellArea,
                        manualThresholdCellArea,
                        invertCellImageSetting);

                // get separated cells
                ImagePlus separatedCells = CellSeparator.separateCells(
                        nucleus,
                        cytoplasm,
                        sigmaGaussCellSep,
                        prominenceCellSep,
                        invertCellImageSetting);

                // filter cells for size and circularity
                ImagePlus filteredCells = CellFilter.filterByCellSize(
                        backgroundMask,
                        separatedCells,
                        minCellSize,
                        maxCellSize,
                        lowCircCellSize,
                        highCircCelLSize);

                manager = CellFilter.filterByNuclei(filteredCells, nucleusMask);

                IJ.log("Found " + manager.getCount() + " cell(s)");

            } else {

                ExternalSegmentationLoader externalSegmentation = new ExternalSegmentationLoader();

                manager = externalSegmentation.createExternalCellROIs(extCellSegmentationDirectory, externalCellInputFile);

                // IMPORTANT: The external cell segmentation is used to define background area.
                // This can be a useful approximation of the background. But also might not be!!
                backgroundMask = externalSegmentation.createExternalSegmentationMask(
                        image.getCalibration(),
                        "HeLa_CellSeg_1.tif");

            }

            ImagePlus detectionsFiltered;

            // Detection of organelles
            if (useInternalDetection) {

                // lysosome detection
                ImagePlus detections = OrganelleDetector.detectOrganelles(organelle, sigmaLoGOrga, prominenceOrga);
                detectionsFiltered = DetectionFilter.filterByNuclei(nucleusMask, detections);

            } else {

                ExternalSegmentationLoader loadDetectionMask = new ExternalSegmentationLoader();
                detectionsFiltered = loadDetectionMask.createExternalSegmentationMask(
                        image.getCalibration(),
                        "HeLa_Detect_1.tif");

            }

            // measure background in organelle channel
            double backgroundOrganelle = BackgroundMeasure.measureDetectionBackground(backgroundMask, organelle);
            double backgroundMeasure = -1;

            ArrayList<ArrayList<ArrayList<String>>> resultLists;

            // performing measurements in measurement channel if selected
            if ( measureChannel == 0) {

                IJ.log("No measure channel selected");

                resultLists = DistanceMeasure.measureCell(
                        manager,
                        nucleusMask,
                        detectionsFiltered,
                        organelle,
                        fileNameWOtExt,
                        seriesNumber,
                        backgroundOrganelle,
                        backgroundMeasure,
                        measureChannel,
                        organelle,
                        distanceFromMembraneSetting
                );

            } else {

                ImagePlus measure = imp_channels[processingImage.measure - 1];
                IJ.log("Measuring in channel: " + measureChannel);

                backgroundMeasure = BackgroundMeasure.measureDetectionBackground(backgroundMask, measure);

                resultLists = DistanceMeasure.measureCell(
                        manager,
                        nucleusMask,
                        detectionsFiltered,
                        organelle,
                        fileNameWOtExt,
                        seriesNumber,
                        backgroundOrganelle,
                        backgroundMeasure,
                        measureChannel,
                        measure,
                        distanceFromMembraneSetting
                );

            }

            // measure organelle distance and intensity, cell properties
            ArrayList<ArrayList<String>> nucDistanceMeasure = resultLists.get(0);
            ArrayList<ArrayList<String>> cellMeasure = resultLists.get(1);
            ArrayList<ArrayList<String>> nucIntensityProfiles = resultLists.get(2);

            nucDistanceMeasureAll.addAll(nucDistanceMeasure);
            cellMeasureAll.addAll(cellMeasure);

            // get distance measure from membrane
            if (distanceFromMembraneSetting) {

                ArrayList<ArrayList<String>> membraneDistanceMeasure = resultLists.get(3);
                membraneDistanceMeasureAll.addAll(membraneDistanceMeasure);

                ArrayList<ArrayList<String>> membraneIntensityProfiles = resultLists.get(4);
                membraneIntensityProfilesAll.addAll(membraneIntensityProfiles);

            }

            // create directory for saving result images
            String saveDir = outputDir + File.separator + fileName;
            try {

                Files.createDirectories(Paths.get(saveDir));

            } catch (IOException e) {

                IJ.log("Unable to create output directory");
                e.printStackTrace();
            }

            // save result images
            BatchResultSaver.saveResultImages(
                    outputDir,
                    fileName,
                    nucleusMask,
                    cytoplasm,
                    manager,
                    nucleus,
                    organelle,
                    detectionsFiltered);

            // results for value measurement too large for large datasets save measurement for each image
            BatchResultSaver.saveIntensityProfiles(
                    nucIntensityProfiles,
                    saveDir,
                    "intensityDistance.csv" , measureChannel);

            if (distanceFromMembraneSetting) {

                BatchResultSaver.saveIntensityProfiles(
                        membraneIntensityProfilesAll,
                        saveDir,
                        "intensityDistanceFromMembrane.csv" , measureChannel);

            }

        }

        BatchResultSaver.saveCellMeasure(
                cellMeasureAll,
                outputDir,
                "cellMeasurements.csv",
                measureChannel);
        BatchResultSaver.saveDistanceMeasure(
                nucDistanceMeasureAll,
                outputDir,
                "organelleDistance.csv", measureChannel);

        if (distanceFromMembraneSetting) {

            BatchResultSaver.saveDistanceMeasure(
                    membraneDistanceMeasureAll,
                    outputDir,
                    "organelleDistanceFromMembrane.csv",
                    measureChannel);

        }

        IJ.log("== Batch processing finished ==");
        IJ.showProgress(1);

    }

    BatchProcessor(String inputDirectory,
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
                   boolean getDistanceFromMembraneSetting,
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
                   double getProminenceOrga,
                   boolean getInvertCellImageSetting,
                   boolean getUseInternalNucleusSegmentation,
                   boolean getUseInternalCellSegmentation,
                   boolean getUseInternalDetection) {

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
        distanceFromMembraneSetting = getDistanceFromMembraneSetting;

        // settings for nucleus settings
        invertCellImageSetting = getInvertCellImageSetting;
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

        // settings for external segmentation / detection
        useInternalNucleusSegmentation = getUseInternalNucleusSegmentation;
        useInternalCellSegmentation = getUseInternalCellSegmentation;
        useInternalDetection = getUseInternalDetection;

    }

}
