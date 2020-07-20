package de.leibnizfmp;

import ij.IJ;
import fiji.util.gui.GenericDialogPlus;
import ij.Prefs;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * implements the setup dialog for the input output directories using ImageJ's inbuilt dialogs
 * thus now enable to drag and drop folders
 * also the code is simpler in this version compared to the old InputGui
 */
class InputGuiFiji {

    private File settingsFile;

    String defaultInputDirectory;
    String defaultOutputDirectory;
    int defaultChannelNumber;
    String defaultFileFormat;
    String defaultSettingsFile;
    Boolean showSettingsSwitch;

    /**
     * default constructor
     */
    InputGuiFiji() {

        defaultInputDirectory = "Choose Directory";
        defaultOutputDirectory = "Choose Directory";
        defaultChannelNumber = 3;
        defaultFileFormat = ".tif";
        defaultSettingsFile = "Choose a File or leave empty";
        settingsFile = null;
        showSettingsSwitch = true;
    }


    /**
     * Constructor to change only the directories
     * The option for selecting a settingsFile are hidden
     *
     * @param settingsFileString name of the xml file that stores the analysis settings
     * @param showSettings boolean that switches the settingsFile dialog on or off
     */
    InputGuiFiji(String settingsFileString, int channelNumber, Boolean showSettings) {

        defaultInputDirectory = "Choose Directory";
        defaultOutputDirectory = "Choose Directory";
        defaultChannelNumber = channelNumber;
        defaultFileFormat = ".tif";
        defaultSettingsFile = settingsFileString;
        settingsFile = new File(settingsFileString);
        showSettingsSwitch = showSettings;

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
    InputGuiFiji(String inputDir, String outputDir, int channelNumber, String fileFormat, String settingsFileString ) {

        defaultInputDirectory = inputDir;
        defaultOutputDirectory = outputDir;
        defaultChannelNumber = channelNumber;
        defaultFileFormat = fileFormat;
        defaultSettingsFile = settingsFileString;
        settingsFile = null;
        showSettingsSwitch = true;

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

        GenericDialogPlus gdPlus = new GenericDialogPlus("Setup dialog");

        gdPlus.addDirectoryField("Input directory: ", defaultInputDirectory, 50);
        gdPlus.addDirectoryField("Output directory: ", defaultOutputDirectory, 50);
        gdPlus.addNumericField("Numbers of Channels: ", defaultChannelNumber, 0, 50, "");
        gdPlus.addStringField("File ending: ", defaultFileFormat, 50);

        if ( !showSettingsSwitch ) {

            IJ.log("Hiding Settings File option");

        } else {

            gdPlus.addFileField("Settings File", defaultSettingsFile, 50);

        }

        gdPlus.showDialog();

        // when canceled is pressed
        if ( gdPlus.wasCanceled() ) {

            System.out.println("Processing canceled");

        } else {

            File inputDirectory = new File(defaultInputDirectory = gdPlus.getNextString());
            File outputDirectory = new File(defaultOutputDirectory = gdPlus.getNextString());

            int channelNumber= (int) gdPlus.getNextNumber();

            String fileFormat = gdPlus.getNextString();

            if ( showSettingsSwitch ) {

                settingsFile = new File(defaultSettingsFile = gdPlus.getNextString());
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
                String outputFileString = outputDirectory.toString();

                // generates the file list that is fed to the preview GUI
                ArrayList<String> fileList = getFileList.getFileMultiSeriesList(checkTrailingSlash(inputFileString));

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

                            PreviewGui previewGui = new PreviewGui(checkTrailingSlash(inputFileString), checkTrailingSlash(outputFileString), fileList, fileFormat, channelNumber,
                                    readMyXml.readKernelSizeNuc, readMyXml.readRollingBallRadiusNuc, readMyXml.readThresholdNuc, readMyXml.readErosionNuc, readMyXml.readMinSizeNuc, readMyXml.readMaxSizeNuc, readMyXml.readLowCircNuc, readMyXml.readHighCircNuc,
                                    readMyXml.readKernelSizeCellArea, readMyXml.readRollBallRadiusCellArea, readMyXml.readManualThresholdCellArea,
                                    readMyXml.readSigmaGaussCellSep, readMyXml.readProminenceCellSep,
                                    readMyXml.readMinCellSize, readMyXml.readMaxCellSize, readMyXml.readLowCircCellSize, readMyXml.readHighCircCelLSize,
                                    readMyXml.readSigmaLoGOrga, readMyXml.readProminenceOrga,
                                    readMyXml.readCalibrationSetting, readMyXml.readPxSizeMicron, readMyXml.readNucleusChannel, readMyXml.readCytoplasmChannel, readMyXml.readOrganelleChannel, readMyXml.readMeasure);

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

                        IJ.log("Did no find xml settings file using default values");

                        // constructs previewGui from default settings since no valid settings file was given
                        PreviewGui previewGui = new PreviewGui(checkTrailingSlash(inputFileString),
                                checkTrailingSlash(outputFileString), fileList, fileFormat, channelNumber);

                        // instantiates previewGui
                        previewGui.setUpGui();

                    }

                }

            }

        }

    }
}
