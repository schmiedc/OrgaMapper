package de.leibnizfmp.maporganelle;

import fiji.util.gui.GenericDialogPlus;

/**
 * Implements the setup dialog for external segmentation and detection
 * Uses ImageJ's inbuilt dialogs
 */
public class ExtSegDetectGUI {


    private final Boolean externalCellSegSwitch;
    String externalCellSegDirectory;
    String defaultExternalCellSegIndicator;
    String defaultCellSegFileFormat;
    private final Boolean externalNucSegSwitch;
    String  externalNucSegDirectory;
    String defaultExternalNucSegIndicator;
    String defaultNucSegFileFormat;
    private final Boolean externalDetectionSwitch;
    String externalDetectionDirectory;
    String defaultExternalDetectionIndicator;

    String defaultDetectionFileFormat;

    void createWindow() {

        // TODO: Sequence - InputGUI check if files are present get base name
        // TODO: After extSegDetectGUI then check if the matching files according to settings exist
        // TODO: Cell Preview GUI and load in new settings
        // TODO: Linkt to InputGUIFiji

        GenericDialogPlus gdPlus = new GenericDialogPlus("External segmentation (seg.) and detection dialog");


        if (externalCellSegSwitch) {

            gdPlus.addDirectoryField("Cell seg. input directory: ", externalCellSegDirectory, 50);
            gdPlus.addStringField("Cell seg. name indicator: ", defaultExternalCellSegIndicator, 50);
            gdPlus.addStringField("Cell seg. file ending: ", defaultCellSegFileFormat, 50);

        }

        if (externalNucSegSwitch) {

            gdPlus.addDirectoryField("Nuc seg. input directory: ", externalCellSegDirectory, 50);
            gdPlus.addStringField("Nuc seg. name indicator: ", defaultExternalCellSegIndicator, 50);
            gdPlus.addStringField("Nuc seg. file ending: ", defaultNucSegFileFormat, 50);

        }

        if (externalDetectionSwitch) {

            gdPlus.addDirectoryField("Detection input directory: ", externalCellSegDirectory, 50);
            gdPlus.addStringField("Detection name indicator: ", defaultExternalCellSegIndicator, 50);
            gdPlus.addStringField("Detection file ending: ", defaultDetectionFileFormat , 50);

        }


        gdPlus.showDialog();

        // TODO: Implement pass back to Input directory when canceled

    }

    ExtSegDetectGUI(Boolean getExternalCellSegSwitch, Boolean getExternalNucSegSwitch, Boolean getExternalDetectionSwitch) {

        externalCellSegSwitch = getExternalCellSegSwitch;
        externalNucSegSwitch = getExternalNucSegSwitch;
        externalDetectionSwitch = getExternalDetectionSwitch;

        externalCellSegDirectory = "Choose Directory";
        defaultExternalCellSegIndicator = "CellSeg";
        defaultCellSegFileFormat = ".tif";

        externalNucSegDirectory = "Choose Directory";
        defaultExternalNucSegIndicator = "NucSeg";
        defaultNucSegFileFormat = ".tif";

        externalDetectionDirectory = "Choose Directory";
        defaultExternalDetectionIndicator = "Detect";
        defaultDetectionFileFormat = ".tif";

    }


}
