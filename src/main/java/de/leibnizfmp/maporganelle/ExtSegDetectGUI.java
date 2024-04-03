package de.leibnizfmp.maporganelle;

import fiji.util.gui.GenericDialogPlus;

import java.io.File;
import java.util.ArrayList;

import static de.leibnizfmp.maporganelle.InputGuiFiji.checkTrailingSlash;

/**
 * Implements the setup dialog for external segmentation and detection
 * Uses ImageJ's inbuilt dialogs
 */
public class ExtSegDetectGUI {

    String InputDirectory;

    // image settings
    private final String inputDir;
    private final String outputDir;
    private final ArrayList<String> fileList;
    private final String fileFormat;
    private final double pxSizeMicron;
    private final int channelNumber;
    private final boolean multiSeries;
    private final boolean settings;

    // settings for nucleus segmentation
    private float kernelSizeNuc;
    private double rollingBallRadiusNuc;
    private String thresholdNuc;
    private int erosionNuc;
    private double minSizeNuc;
    private double maxSizeNuc;
    private double lowCircNuc;
    private double highCircNuc;

    // settings for cell area segmentation
    private boolean invertCellImageSetting;
    private float kernelSizeCellArea;
    private double rollingBallRadiusCellArea;
    private int manualThresholdCellArea;

    // settings for cell separator
    private double sigmaGaussCellSep;
    private double prominenceCellSep;

    // settings for cell filter size
    private double minCellSize;
    private double maxCellSize;
    private double lowCircCellSize;
    private double highCircCelLSize;

    // settings for organelle detection
    private double sigmaLoGOrga;
    private double prominenceOrga;

    // image settings
    private boolean calibrationSetting;
    private boolean distanceFromMembraneSetting;
    private int nucleusChannel;
    private int cytoplasmChannel;
    private int organelleChannel;
    private int measure;

    // external seg settings
    String externalFileDirectory;
    private final Boolean externalNucSegSwitch;
    String defaultExternalNucSegIndicator;
    private final Boolean externalCellSegSwitch;
    String defaultExternalCellSegIndicator;
    private final Boolean externalDetectionSwitch;
    String defaultExternalDetectionIndicator;

    void createWindow() {

        GenericDialogPlus gdPlus = new GenericDialogPlus("External segmentation and detection setup dialog");

        gdPlus.addDirectoryField("External file directory: ", externalFileDirectory, 50);

        if (externalNucSegSwitch) {

            gdPlus.addStringField("Nucleus segmentation suffix: ", defaultExternalNucSegIndicator, 50);

        }

        if (externalCellSegSwitch) {

            gdPlus.addStringField("Cell segmentation suffix: ", defaultExternalCellSegIndicator, 50);

        }

        if (externalDetectionSwitch) {

            gdPlus.addStringField("Detection suffix: ", defaultExternalDetectionIndicator, 50);

        }

        gdPlus.showDialog();

        // when canceled is pressed
        if ( gdPlus.wasCanceled() ) {

            System.out.println("Processing canceled");

        } else {

            File externalSegmentationDirectory = new File(InputDirectory = gdPlus.getNextString());
            System.out.println(externalSegmentationDirectory);

            String externalNucSegDirectory = null;
            String externalNucSegSuffix = null;
            String externalCellSegDirectory = null;
            String externalCellSegSuffix = null;
            String externalDetectionDirectory = null;
            String externalDetectionSuffix = null;

            if (externalNucSegSwitch) {

                externalNucSegDirectory = checkTrailingSlash(String.valueOf(externalSegmentationDirectory));
                externalNucSegSuffix = gdPlus.getNextString();
            }

            if (externalCellSegSwitch) {

                externalCellSegDirectory = checkTrailingSlash(String.valueOf(externalSegmentationDirectory));
                externalCellSegSuffix = gdPlus.getNextString();

            }

            if (externalDetectionSwitch) {

                externalDetectionDirectory = checkTrailingSlash(String.valueOf(externalSegmentationDirectory));
                externalDetectionSuffix = gdPlus.getNextString();

            }

            if (settings) {

                PreviewGui previewGui = new PreviewGui(
                        inputDir,
                        outputDir,
                        fileList,
                        fileFormat,
                        channelNumber,
                        kernelSizeNuc,
                        rollingBallRadiusNuc,
                        thresholdNuc,
                        erosionNuc,
                        minSizeNuc,
                        maxSizeNuc,
                        lowCircNuc,
                        highCircNuc,
                        invertCellImageSetting,
                        kernelSizeCellArea,
                        rollingBallRadiusCellArea,
                        manualThresholdCellArea,
                        sigmaGaussCellSep,
                        prominenceCellSep,
                        minCellSize,
                        maxCellSize,
                        lowCircCellSize,
                        highCircCelLSize,
                        sigmaLoGOrga,
                        prominenceOrga,
                        calibrationSetting,
                        pxSizeMicron,
                        distanceFromMembraneSetting,
                        nucleusChannel,
                        cytoplasmChannel,
                        organelleChannel,
                        measure,
                        multiSeries,
                        externalNucSegSwitch,
                        externalCellSegSwitch,
                        externalDetectionSwitch,
                        externalNucSegDirectory,
                        externalCellSegDirectory,
                        externalDetectionDirectory,
                        externalNucSegSuffix,
                        externalCellSegSuffix,
                        externalDetectionSuffix);

                previewGui.setUpGui();

            } else {

                // constructs previewGui from default settings since no valid settings file was given
                PreviewGui previewGui = new PreviewGui(
                        inputDir,
                        outputDir,
                        fileList,
                        fileFormat,
                        channelNumber,
                        pxSizeMicron,
                        multiSeries,
                        externalNucSegSwitch,
                        externalCellSegSwitch,
                        externalDetectionSwitch,
                        externalNucSegDirectory,
                        externalCellSegDirectory,
                        externalDetectionDirectory,
                        externalNucSegSuffix,
                        externalCellSegSuffix,
                        externalDetectionSuffix);

                // instantiates previewGui
                previewGui.setUpGui();

            }

        }

    }

    ExtSegDetectGUI(String inputDirectory,
                    String outputDirectory,
                    ArrayList<String> filesToProcess,
                    String format,
                    int getChannelNumber,
                    double pixelSize,
                    boolean getMultiSeries,
                    boolean getExternalNucSegSwitch,
                    boolean getExternalCellSegSwitch,
                    boolean getExternalDetectionSwitch) {

        // From InputGUI
        inputDir = inputDirectory;
        outputDir = outputDirectory;
        fileList = filesToProcess;
        fileFormat = format;
        channelNumber = getChannelNumber;
        pxSizeMicron = pixelSize;
        multiSeries = getMultiSeries;

        // Settings for ExternalSeg GUI
        externalNucSegSwitch = getExternalNucSegSwitch;
        externalCellSegSwitch = getExternalCellSegSwitch;
        externalDetectionSwitch = getExternalDetectionSwitch;

        externalFileDirectory = "Choose Directory";
        defaultExternalNucSegIndicator = "_NucSeg.tif";
        defaultExternalCellSegIndicator = "_CellSeg.tif";
        defaultExternalDetectionIndicator = "_Detect.tif";

        settings = false;

    }

    ExtSegDetectGUI(String inputDirectory,
                    String outputDirectory,
                    ArrayList<String> filesToProcess,
                    String format,
                    int getChannelNumber,
                    float getKernelSizeNuc,
                    double getRollingBallRadiusNuc,
                    String getThresholdNuc,
                    int getErosionNuc,
                    double getMinSizeNuc,
                    double getMaxSizeNuc,
                    double getLowCircNuc,
                    double getHighCircNuc,
                    boolean getInvertCellImageSetting,
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
                    boolean getCalibrationSetting,
                    double getPxSizeMicron,
                    boolean getdistanceFromMembraneSetting,
                    int getNucleusChannel,
                    int getCytoplasmChannel,
                    int getOrganelleChannel,
                    int getMeasure,
                    boolean getMultiSeries,
                    boolean getExternalNucSegSwitch,
                    boolean getExternalCellSegSwitch,
                    boolean getExternalDetectionSwitch) {

        inputDir = inputDirectory;
        outputDir = outputDirectory;
        fileList = filesToProcess;
        fileFormat = format;
        channelNumber = getChannelNumber;

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
        invertCellImageSetting = getInvertCellImageSetting;
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

        // image settings
        calibrationSetting = getCalibrationSetting;
        pxSizeMicron = getPxSizeMicron;
        distanceFromMembraneSetting = getdistanceFromMembraneSetting;
        nucleusChannel = getNucleusChannel;
        cytoplasmChannel = getCytoplasmChannel;
        organelleChannel = getOrganelleChannel;
        measure = getMeasure;

        // Settings for ExternalSeg GUI
        externalNucSegSwitch = getExternalNucSegSwitch;
        externalCellSegSwitch = getExternalCellSegSwitch;
        externalDetectionSwitch = getExternalDetectionSwitch;

        externalFileDirectory = "Choose Directory";
        defaultExternalNucSegIndicator = "_NucSeg.tif";
        defaultExternalCellSegIndicator = "_CellSeg.tif";
        defaultExternalDetectionIndicator = "_Detect.tif";

        multiSeries = getMultiSeries;

        settings = true;
    }


}
