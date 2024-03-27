package de.leibnizfmp.maporganelle;

import fiji.util.gui.GenericDialogPlus;

import java.util.ArrayList;

/**
 * Implements the setup dialog for external segmentation and detection
 * Uses ImageJ's inbuilt dialogs
 */
public class ExtSegDetectGUI {

    private final String inputDir;
    private final String outputDir;
    private final ArrayList<String> fileList;
    private final String fileFormat;
    private final double pxSizeMicron;
    private final int channelNumber;
    private boolean multiSeries;
    String externalFileDirectory;
    String defaultSegmentationFileFormat;
    private final Boolean externalCellSegSwitch;
    String externalCellSegDirectory;
    String defaultExternalCellSegIndicator;
    private final Boolean externalNucSegSwitch;
    String  externalNucSegDirectory;
    String defaultExternalNucSegIndicator;
    private final Boolean externalDetectionSwitch;
    String externalDetectionDirectory;
    String defaultExternalDetectionIndicator;

    void createWindow() {

        GenericDialogPlus gdPlus = new GenericDialogPlus("External segmentation (seg.) and detection dialog");

        gdPlus.addDirectoryField("External file directory: ", externalNucSegDirectory, 50);
        gdPlus.addStringField("External file ending: ", defaultSegmentationFileFormat , 50);

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

        // TODO: Implement pass back to Input directory when canceled

    }

    ExtSegDetectGUI (String inputDirectory,
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
        defaultSegmentationFileFormat = ".tif";

        externalNucSegDirectory = null;
        defaultExternalNucSegIndicator = "NucSeg";

        externalCellSegDirectory = null;
        defaultExternalCellSegIndicator = "CellSeg";

        externalDetectionDirectory = null;
        defaultExternalDetectionIndicator = "Detect";

    }


}
