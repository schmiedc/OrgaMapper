package de.leibnizfmp.maporganelle;

import ij.IJ;
import fiji.util.gui.GenericDialogPlus;
import ij.ImagePlus;
import ij.io.OpenDialog;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static ij.io.OpenDialog.setDefaultDirectory;
import static ij.io.OpenDialog.setLastDirectory;

/**
 * implements the setup dialog for the input output directories using ImageJ's inbuilt dialogs
 * thus now enable to drag and drop folders
 * also the code is simpler in this version compared to the old InputGui
 */

class InputGuiFiji {

    private File settingsFile;

    String InputDirectory;
    String OutputDirectory;
    String defaultFileFormat;
    String defaultSettingsFile;
    Boolean showSettingsSwitch;
    Boolean testMode;

    /**
     * default constructor
     */
    InputGuiFiji() {

        InputDirectory = "Choose Directory";
        OutputDirectory = "Choose Directory";
        defaultFileFormat = ".tif";
        defaultSettingsFile = "Choose a File or leave empty";
        settingsFile = null;
        showSettingsSwitch = true;
        testMode = false;
    }

    /**
     * Constructor to change only the directories
     * The option for selecting a settingsFile are hidden
     *
     * @param settingsFileString name of the xml file that stores the analysis settings
     * @param showSettings boolean that switches the settingsFile dialog on or off
     */
    InputGuiFiji(String settingsFileString, Boolean showSettings) {

        InputDirectory = "Choose Directory";
        OutputDirectory = "Choose Directory";
        defaultFileFormat = ".tif";
        defaultSettingsFile = settingsFileString;
        settingsFile = new File(settingsFileString);
        showSettingsSwitch = showSettings;
        testMode = false;

        IJ.log("Taking settings from previous preview:");
        IJ.log(settingsFileString);
    }

    /**
     * Constructor that takes inputs for input and output directories
     * as well as a settingsFile for testing the workflow
     *
     * @param inputDir location for test files
     * @param outputDir location for writing the test outputs
     * @param fileFormat fileFormat
     * @param settingsFileString location of test settingsFile
     */
    InputGuiFiji(String inputDir, String outputDir, String fileFormat, String settingsFileString ) {

        InputDirectory = inputDir;
        OutputDirectory = outputDir;
        defaultFileFormat = fileFormat;
        defaultSettingsFile = settingsFileString;
        settingsFile = null;
        showSettingsSwitch = true;
        testMode = true;

    }

    /**
     * checks inputString for trailing slash if not adds the file separator to it
     *
     * @param inputString input string
     * @return input string with trailing slash for OS
     */
    private static String checkTrailingSlash(String inputString) {

        return inputString.endsWith(File.separator) ? inputString : inputString + File.separator;
    }

    /**
     * creates the setupGui dialog in the beginning
     */
    void createWindow() {

        String lastDirectory = OpenDialog.getLastDirectory();

        if ( lastDirectory == null && !testMode) {

            lastDirectory = "Choose directory";

        } else if ( testMode ) {

            lastDirectory = InputDirectory;

        }

        String defaultDirectory = OpenDialog.getDefaultDirectory();

        if ( defaultDirectory == null && !testMode) {

            defaultDirectory = "Choose directory";

        } else if ( testMode ) {

            defaultDirectory = OutputDirectory;

        }

        GenericDialogPlus gdPlus = new GenericDialogPlus("OrgaMapper setup dialog");
        gdPlus.addDirectoryField("Input directory: ", lastDirectory , 50);
        gdPlus.addDirectoryField("Output directory: ", defaultDirectory, 50);
        gdPlus.addStringField("File ending: ", defaultFileFormat, 50);

        if ( !showSettingsSwitch ) {

            IJ.log("Hiding Settings File option");

        } else {

            gdPlus.addFileField("Settings File", defaultSettingsFile, 50);

            gdPlus.addCheckbox("External nucleus segmentation", false);
            gdPlus.addCheckbox("External cell segmentation", false);
            gdPlus.addCheckbox("External detection", false);

        }

        gdPlus.showDialog();

        // when canceled is pressed
        if ( gdPlus.wasCanceled() ) {

            System.out.println("Processing canceled");

        } else {

            File inputDirectory = new File(InputDirectory = gdPlus.getNextString());
            File outputDirectory = new File(OutputDirectory = gdPlus.getNextString());
            String fileFormat = gdPlus.getNextString();

            if ( showSettingsSwitch ) {

                settingsFile = new File(defaultSettingsFile = gdPlus.getNextString());

                boolean externalNucleusSegmentation = gdPlus.getNextBoolean();
                boolean externalCellSegmentation = gdPlus.getNextBoolean();
                boolean externalDetection = gdPlus.getNextBoolean();

                IJ.log("externalNucleusSegmentation: " + externalNucleusSegmentation);
                IJ.log("externalCellSegmentation: " + externalCellSegmentation);
                IJ.log("externalDetection: " + externalDetection);

            }

            // display error message if there is no input and output directory
            if (!inputDirectory.exists() || !outputDirectory.exists()) {

                System.out.println("No valid input and/or output directory selected");
                IJ.error("Please choose a valid input and/or output directory!");

                //reshow setup dialog
                this.createWindow();

            } else {

                System.out.println("Proceed with preview");
                System.out.println("Input Directory: " + inputDirectory);
                System.out.println("Output Directory: " + outputDirectory);
                System.out.println("File Ending: " + fileFormat);
                System.out.println("Settings File: " + settingsFile);

                FileList getFileList = new FileList(fileFormat);

                String inputFileString = inputDirectory.toString();
                setLastDirectory(inputFileString);

                String outputFileString = outputDirectory.toString();
                setDefaultDirectory(outputFileString);

                // generates the file list that is fed to the preview GUI
                FileListProperties fileListProperties = getFileList.getFileMultiSeriesList(checkTrailingSlash(inputFileString));
                ArrayList<String> fileList = fileListProperties.fileList;
                boolean multiSeries = fileListProperties.multiSeries;

                if (fileList.isEmpty()) {

                    IJ.error("No suitable files found for processing! Choose another directory!");
                    IJ.log("No suitable files found for processing!");
                    this.createWindow();

                } else {

                    if ( settingsFile != null && settingsFile.exists() ) {

                        String settingsFileString = settingsFile.toString();

                        IJ.log("Found xml settings file: " + settingsFileString);
                        XmlHandler readMyXml = new XmlHandler();

                        try {

                            // reads settings file
                            readMyXml.xmlReader(settingsFileString);

                            String selectedFile = fileList.get(0);
                            ImagePlus imageForMetadata = Image.getImagePlusBF(selectedFile, fileFormat, checkTrailingSlash(inputFileString), 0);
                            int channelN = imageForMetadata.getNChannels();

                            PreviewGui previewGui = new PreviewGui(
                                    checkTrailingSlash(inputFileString),
                                    checkTrailingSlash(outputFileString),
                                    fileList,
                                    fileFormat,
                                    channelN,
                                    readMyXml.readKernelSizeNuc,
                                    readMyXml.readRollingBallRadiusNuc,
                                    readMyXml.readThresholdNuc,
                                    readMyXml.readErosionNuc,
                                    readMyXml.readMinSizeNuc,
                                    readMyXml.readMaxSizeNuc,
                                    readMyXml.readLowCircNuc,
                                    readMyXml.readHighCircNuc,
                                    readMyXml.readInvertCellImage,
                                    readMyXml.readKernelSizeCellArea,
                                    readMyXml.readRollBallRadiusCellArea,
                                    readMyXml.readManualThresholdCellArea,
                                    readMyXml.readSigmaGaussCellSep,
                                    readMyXml.readProminenceCellSep,
                                    readMyXml.readMinCellSize,
                                    readMyXml.readMaxCellSize,
                                    readMyXml.readLowCircCellSize,
                                    readMyXml.readHighCircCelLSize,
                                    readMyXml.readSigmaLoGOrga,
                                    readMyXml.readProminenceOrga,
                                    readMyXml.readCalibrationSetting,
                                    readMyXml.readPxSizeMicron,
                                    readMyXml.readMembraneDistanceMeasurement,
                                    readMyXml.readNucleusChannel,
                                    readMyXml.readCytoplasmChannel,
                                    readMyXml.readOrganelleChannel,
                                    readMyXml.readMeasure,
                                    multiSeries);

                            // instantiates previewGui
                            previewGui.setUpGui();

                        } catch (ParserConfigurationException ex) {

                            ex.printStackTrace();
                            IJ.log("ERROR: XML reader, Parser Configuration exception");
                            IJ.error("Please select a valid .xml or leave empty");
                            settingsFile = null;

                        } catch (IOException ex) {

                            ex.printStackTrace();
                            IJ.log("ERROR: XML reader, IOException");
                            IJ.error("Please select a valid .xml or leave empty");
                            settingsFile = null;

                        } catch (SAXException ex) {

                            ex.printStackTrace();
                            IJ.log("ERROR: XML reader, SAXException");
                            IJ.error("Please select a valid .xml or leave empty");
                            settingsFile = null;

                        }

                    } else {

                        String selectedFile = fileList.get(0);
                        ImagePlus imageForMetadata = Image.getImagePlusBF(selectedFile, fileFormat, checkTrailingSlash(inputFileString), 0);
                        double pixelHeight = imageForMetadata.getCalibration().pixelHeight;
                        int channelN = imageForMetadata.getNChannels();

                        IJ.log("Did no find xml settings file using default values");

                        // constructs previewGui from default settings since no valid settings file was given
                        PreviewGui previewGui = new PreviewGui(
                                checkTrailingSlash(inputFileString),
                                checkTrailingSlash(outputFileString),
                                fileList,
                                fileFormat,
                                channelN,
                                pixelHeight,
                                multiSeries);

                        // instantiates previewGui
                        previewGui.setUpGui();

                    }

                }

            }

        }

    }
}
