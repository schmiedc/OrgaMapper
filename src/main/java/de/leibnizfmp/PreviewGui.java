package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Calibration;
import org.scijava.util.ArrayUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class PreviewGui extends JPanel {

    // threshold method list
    private final String[] thresholdString = { "Default", "Huang", "IJ_IsoData", "Intermodes",
            "IsoData", "Li", "MaxEntropy", "Mean", "MinError", "Minimum",
            "Moments", "Otsu", "Percentile", "RenyiEntropy", "Shanbhag",
            "Triangle","Yen",
    };

    // basic settings
    private String inputDir;
    private String outputDir;
    private ArrayList<String> fileList;

    // list of files
    private JList list;

    // settings for nucleus settings
    private float kernelSizeNuc;
    private double rollingBallRadiusNuc;
    private String thresholdNuc;
    private int erosionNuc;
    private double minSizeNuc;
    private double maxSizeNuc;
    private double lowCircNuc;
    private double highCircNuc;

    SpinnerModel doubleSpinKernelSizeNuc;
    SpinnerModel doubleSpinrollingBallRadiusNuc;
    JComboBox<String> thresholdListBack;
    SpinnerModel doubleSpinErosionNuc;
    SpinnerModel doubleSpinMinSize;
    SpinnerModel doubleSpinMaxSize;
    SpinnerModel doubleSpinLowCirc;
    SpinnerModel doubleSpinHighCirc;

    // settings for cell area segmentation
    private float kernelSizeCellArea;
    private double rollingBallRadiusCellArea;
    private int manualThresholdCellArea;

    SpinnerModel doubleSpinKernelCellArea;
    SpinnerModel doubleSpinRollBallCellArea;
    SpinnerModel doubleSpinThresholdCellArea;

    // settings for cell separator
    private double sigmaGaussCellSep;
    private double prominenceCellSep;

    SpinnerModel doubleSpinGaussCellSep;
    SpinnerModel doubleSpinProminenceCellSep;

    // settings for cell filter size
    private double minCellSize;
    private double maxCellSize;
    private double lowCircCellSize;
    private double highCircCelLSize;

    SpinnerModel doubleSpinMinSizeCellFilter;
    SpinnerModel doubleSpinMaxSizeCellFilter;
    SpinnerModel doubleSpinLowCircCellFilter;
    SpinnerModel doubleSpinHighCircCellFilter;
    JCheckBox checkFilterCellFilter;

    // settings for organelle detection
    private double sigmaLoGOrga;
    private double prominenceOrga;

    SpinnerModel doubleSpinnerLoGOragenelle;
    SpinnerModel doubleSpinnerProminenceOrganelle;
    JCheckBox checkFilterOrganelle;

    // image settings
    private boolean calibrationSetting;
    private double pxSizeMicron;
    private int nucleusChannel;
    private int cytoplasmChannel;
    private int organelleChannel;
    private int measure;

    private String fileFormat;
    private boolean setDisplayRange = false;

    JCheckBox checkCalibration;
    private SpinnerModel doubleSpinnerPixelSize;

    Box nucSegBox = new Box(BoxLayout.Y_AXIS);
    Box cellSegBox = new Box(BoxLayout.Y_AXIS);
    Box organelleBox = new Box(BoxLayout.Y_AXIS);
    Box boxSettings = new Box(BoxLayout.Y_AXIS);
    Box batchBox = new Box(BoxLayout.Y_AXIS);


    // tabbed pane
    private JTabbedPane tabbedPane = new JTabbedPane();
    JFrame theFrame;

    private Border blackline;

    void setUpGui() {

        // sets up the frame
        theFrame = new JFrame("Map organelles processing");

        // needs to set to dispose otherwise it also closes Fiji
        theFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);

        // creates margin between edges of the panel and where the components are placed
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // create tabbed panes
        setUpNucleiTab();
        tabbedPane.addTab("Nuclei", nucSegBox);

        setUpCellsTab();
        tabbedPane.addTab("Cells", cellSegBox);

        setUpOrganellesTab();
        tabbedPane.addTab("Organelles", organelleBox);

        setUpSettingsTab();
        batchBox.add(boxSettings);

        // file selection scroller
        JScrollPane scroller = setUpFileList(fileList);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setPreferredSize(new Dimension(290, 100));

        // Setup Interactions for Segment Boutons
        Box saveLoadBox = new Box(BoxLayout.X_AXIS);

        // setup Buttons
        JButton  saveButton = new JButton("Save settings");
        //saveButton.addActionListener(new MySaveListener());
        saveLoadBox.add(saveButton);

        JButton loadButton = new JButton("Load settings");
        //loadButton.addActionListener(new MyLoadListener());
        saveLoadBox.add(loadButton);

        JButton resetButton = new JButton("Reset Processing Settings");
        resetButton.addActionListener(new MyResetListener());
        saveLoadBox.add(resetButton);

        JButton resetDirButton = new JButton("Reset Directories");
        //resetDirButton.addActionListener(new MyResetDirectoryListener());
        saveLoadBox.add(resetDirButton);

        // add boxes to panel and frame
        background.add(BorderLayout.WEST, tabbedPane);
        background.add(BorderLayout.EAST, batchBox);
        background.add(BorderLayout.CENTER, scroller);
        background.add(BorderLayout.SOUTH, saveLoadBox);
        theFrame.getContentPane().add(background);

        theFrame.setSize(1000,500);
        theFrame.setVisible(true);

    }

    private JScrollPane setUpFileList(ArrayList<String> fileList) {

        // setup List
        list = new JList(fileList.toArray());

        // create a new scroll pane
        JScrollPane scroller = new JScrollPane(list);

        // set scroller to use only vertical scrollbar
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        // only 4 items visible
        list.setVisibleRowCount(10);
        // only 1 selection possible
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //list.addListSelectionListener(this);

        return scroller;

    }

    private void setUpNucleiTab() {

        // box with titled borders
        Box segmentationBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleSegmentation;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleSegmentation = BorderFactory.createTitledBorder(blackline, "Processing and threshold: ");
        segmentationBox.setBorder(titleSegmentation);

        doubleSpinKernelSizeNuc = new SpinnerNumberModel(kernelSizeNuc, 0.0, 50.0, 1.0);
        String spinBackLabel1 = "Median filter size: ";
        String spinBackUnit1 = "px";
        Box spinnerBack1 = addLabeledSpinnerUnit(spinBackLabel1, doubleSpinKernelSizeNuc, spinBackUnit1);
        segmentationBox.add(spinnerBack1);

        doubleSpinrollingBallRadiusNuc = new SpinnerNumberModel(rollingBallRadiusNuc, 0.0, 10000, 1.0);
        String spinBackLabel2 = "Rolling ball radius: ";
        String spinBackUnit2 = "px";
        Box spinnerBack2 = addLabeledSpinnerUnit(spinBackLabel2, doubleSpinrollingBallRadiusNuc, spinBackUnit2);
        segmentationBox.add(spinnerBack2);

        thresholdListBack = new JComboBox<>(thresholdString);
        JLabel thresholdListBackLabel  = new JLabel("Select threshold: ");
        Box thresholdListBackBox= new Box(BoxLayout.X_AXIS);
        thresholdListBack.setMaximumSize(new Dimension(Integer.MAX_VALUE, thresholdListBack.getMinimumSize().height));
        // get index of selected threshold using arrayUtils
        int indexOfThreshold = ArrayUtils.indexOf(thresholdString, thresholdNuc);
        // set this index as selected in the threshold list
        thresholdListBack.setSelectedIndex(indexOfThreshold);

        thresholdListBackBox.add(thresholdListBackLabel);
        thresholdListBackBox.add(thresholdListBack);
        segmentationBox.add(thresholdListBackBox);

        doubleSpinErosionNuc = new SpinnerNumberModel(erosionNuc, 0.0, 10, 1.0);
        String spinBackLabel3 = "Erode: ";
        String spinBackUnit3 = "x";
        Box spinnerBack3 = addLabeledSpinnerUnit(spinBackLabel3, doubleSpinErosionNuc, spinBackUnit3);
        segmentationBox.add(spinnerBack3);

        nucSegBox.add(segmentationBox);

        // box with titled borders
        Box filterBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleFilter;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleFilter = BorderFactory.createTitledBorder(blackline, "Filter: size and circ.");
        filterBox.setBorder(titleFilter);

        doubleSpinMinSize = new SpinnerNumberModel(minSizeNuc,0.0,1000000,10.0);
        String minSizeLabel = "Select min. size: ";
        String minUnitLabel = "µm²";
        Box spinnerNuc4 = addLabeledSpinnerUnit(minSizeLabel, doubleSpinMinSize, minUnitLabel );
        filterBox.add(spinnerNuc4);

        doubleSpinMaxSize = new SpinnerNumberModel(maxSizeNuc,0.0,1000000,10.0);
        String maxSizeLabel = "Select max. size: ";
        String maxUnitLabel = "µm²";
        Box spinnerNuc5 = addLabeledSpinnerUnit(maxSizeLabel, doubleSpinMaxSize, maxUnitLabel);
        filterBox.add(spinnerNuc5);

        doubleSpinLowCirc = new SpinnerNumberModel(lowCircNuc,0.0,1.0,0.1);
        String minCircLabel = "Select minimal circularity: ";
        String minCircUnit = "";
        Box lowCircBox = addLabeledSpinnerUnit(minCircLabel, doubleSpinLowCirc, minCircUnit);
        filterBox.add(lowCircBox);

        doubleSpinHighCirc = new SpinnerNumberModel(highCircNuc,0.0,1.0,0.1);
        String highCircLabel = "Select maximal circularity: ";
        String highCircUnit = "";
        Box highCircBox = addLabeledSpinnerUnit(highCircLabel, doubleSpinHighCirc, highCircUnit);
        filterBox.add(highCircBox);

        nucSegBox.add(filterBox);

        // setup Buttons
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(new MyPreviewNucleusListener());
        nucSegBox.add(previewButton);

    }

    private void setUpCellsTab() {

        // box with titled borders
        Box segmentationBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleSegmentation;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleSegmentation = BorderFactory.createTitledBorder(blackline, "Processing and threshold: ");
        segmentationBox.setBorder(titleSegmentation);

        doubleSpinKernelCellArea = new SpinnerNumberModel(kernelSizeCellArea, 0.0, 50.0, 1.0);
        String spinBackLabel1 = "Median filter size: ";
        String spinBackUnit1 = "px";
        Box spinnerBack1 = addLabeledSpinnerUnit(spinBackLabel1, doubleSpinKernelCellArea, spinBackUnit1);
        segmentationBox.add(spinnerBack1);

        doubleSpinRollBallCellArea = new SpinnerNumberModel(rollingBallRadiusCellArea, 0.0, 10000, 1.0);
        String spinBackLabel2 = "Rolling ball radius: ";
        String spinBackUnit2 = "px";
        Box spinnerBack2 = addLabeledSpinnerUnit(spinBackLabel2, doubleSpinRollBallCellArea, spinBackUnit2);
        segmentationBox.add(spinnerBack2);

        doubleSpinThresholdCellArea = new SpinnerNumberModel(manualThresholdCellArea, 0.0, 65536, 1.0);
        String spinBackLabel3 = "Global Threshold: ";
        String spinBackUnit3 = "A.U.";
        Box spinnerBack3 = addLabeledSpinnerUnit(spinBackLabel3, doubleSpinThresholdCellArea, spinBackUnit3);
        segmentationBox.add(spinnerBack3);

        cellSegBox.add(segmentationBox);

        // settings for cell separator
        //private double prominenceCellSep;
        Box separationBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleSeparation;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleSeparation = BorderFactory.createTitledBorder(blackline, "Settings for separating cells: ");
        separationBox.setBorder(titleSeparation);

        doubleSpinGaussCellSep = new SpinnerNumberModel(sigmaGaussCellSep, 0.0, 50.0, 1.0);
        String spinGaussCellSep = "Gauss sigma: ";
        String spinGaussCellSepUnit = "px";
        Box spinnerGaussCellSep = addLabeledSpinnerUnit(spinGaussCellSep, doubleSpinGaussCellSep, spinGaussCellSepUnit);
        separationBox.add(spinnerGaussCellSep);

        doubleSpinProminenceCellSep = new SpinnerNumberModel(prominenceCellSep, 0.0,1000.0, 0.0001);
        String spinLabelProminence = "Prominence: ";
        String spinUnitProminence = "A.U.";
        Box spinSpot2 = addLabeledSpinner5Digit(spinLabelProminence, doubleSpinProminenceCellSep, spinUnitProminence);
        separationBox.add(spinSpot2);

        cellSegBox.add(separationBox);

        // settings for cell filter size
        Box filterBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleFilter;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleFilter = BorderFactory.createTitledBorder(blackline, "Filter: size");
        filterBox.setBorder(titleFilter);

        doubleSpinMinSizeCellFilter = new SpinnerNumberModel(minCellSize,0.0,1000000,10.0);
        String minSizeLabel = "Select min. size: ";
        String minUnitLabel = "µm²";
        Box spinnerNuc4 = addLabeledSpinnerUnit(minSizeLabel, doubleSpinMinSizeCellFilter, minUnitLabel );
        filterBox.add(spinnerNuc4);

        doubleSpinMaxSizeCellFilter = new SpinnerNumberModel(maxCellSize,0.0,1000000,10.0);
        String maxSizeLabel = "Select max. size: ";
        String maxUnitLabel = "µm²";
        Box spinnerNuc5 = addLabeledSpinnerUnit(maxSizeLabel, doubleSpinMaxSizeCellFilter, maxUnitLabel);
        filterBox.add(spinnerNuc5);

        doubleSpinLowCircCellFilter = new SpinnerNumberModel(lowCircCellSize,0.0,1.0,0.1);
        String minCircLabel = "Select minimal circularity: ";
        String minCircUnit = "";
        Box lowCircBox = addLabeledSpinnerUnit(minCircLabel, doubleSpinLowCircCellFilter, minCircUnit);
        filterBox.add(lowCircBox);

        doubleSpinHighCircCellFilter = new SpinnerNumberModel(highCircCelLSize,0.0,1.0,0.1);
        String highCircLabel = "Select maximal circularity: ";
        String highCircUnit = "";
        Box highCircBox = addLabeledSpinnerUnit(highCircLabel, doubleSpinHighCircCellFilter, highCircUnit);
        filterBox.add(highCircBox);

        cellSegBox.add(filterBox);

        checkFilterCellFilter = new JCheckBox("Filter by nuclei?");
        checkFilterCellFilter.setSelected(true);
        checkFilterCellFilter.setToolTipText("Only affects visualization");
        cellSegBox.add(checkFilterCellFilter);

        // setup Buttons
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(new MyPreviewCellListener());
        cellSegBox.add(previewButton);

    }

    private void setUpOrganellesTab() {

        //box with titled borders
        Box detectionBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleDetection;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleDetection = BorderFactory.createTitledBorder(blackline, "Detect: number & position of spots");
        detectionBox.setBorder(titleDetection);

        // Spinner for some number input
        doubleSpinnerLoGOragenelle = new SpinnerNumberModel(sigmaLoGOrga, 0.0,20.0, 0.1);
        String spinLabelSpot1 = "LoG sigma: ";
        String spinUnitSpot1 = "px";
        Box spinSpot1 = addLabeledSpinnerUnit(spinLabelSpot1, doubleSpinnerLoGOragenelle, spinUnitSpot1);
        detectionBox.add(spinSpot1);


        doubleSpinnerProminenceOrganelle = new SpinnerNumberModel(prominenceOrga, 0.0,1000.0, 0.0001);
        String spinLabelSpot2 = "Prominence: ";
        String spinUnitSpot2 = "A.U.";
        Box spinSpot2 = addLabeledSpinner5Digit(spinLabelSpot2, doubleSpinnerProminenceOrganelle, spinUnitSpot2);
        detectionBox.add(spinSpot2);

        organelleBox.add(detectionBox);


        checkFilterOrganelle = new JCheckBox("Filter in nucleus?");
        checkFilterOrganelle.setToolTipText("Only affects visualization");
        checkFilterOrganelle.setSelected(true);
        organelleBox.add(checkFilterOrganelle);

        // setup Buttons
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(new MyPreviewOrganelleListener());
        organelleBox.add(previewButton);

    }

    private void setUpSettingsTab() {

        JLabel settingsLabel = new JLabel("Specify experimental Settings: ");
        boxSettings.add(settingsLabel);

        doubleSpinnerPixelSize = new SpinnerNumberModel(pxSizeMicron, 0.000,10.000, 0.001);
        String pixelSizeLabel = "Pixel size: ";
        String pixelSizeUnit = "µm";
        Box boxPixelSize = addLabeledSpinnerUnit(pixelSizeLabel,doubleSpinnerPixelSize, pixelSizeUnit);
        boxSettings.add(boxPixelSize);

        checkCalibration = new JCheckBox("Override metadata?");
        checkCalibration.setToolTipText("Use when metadata is corrupted");
        checkCalibration.setSelected(calibrationSetting);
        boxSettings.add(checkCalibration);

        // TODO: create channel settings
        // TODO: channel selection sanity check - each channel is selected only once
        // TODO: option for measureing in another channel

        JButton batchButton = new JButton("Batch Process");
        //batchButton.addActionListener(new MyBatchListener());
        boxSettings.add(batchButton);

    }

    /**
     * creates a labeled spinner
     * @param label name
     * @param model for which spinner
     * @param unit label after spinner
     * @return box with labeled spinner
     */
    private static Box addLabeledSpinnerUnit(String label,
                                             SpinnerModel model,
                                             String unit) {

        Box spinnerLabelBox = new Box(BoxLayout.X_AXIS);
        JLabel l1 = new JLabel(label);
        l1.setPreferredSize(new Dimension(150, l1.getMinimumSize().height));
        spinnerLabelBox.add(l1);

        JSpinner spinner = new JSpinner(model);
        l1.setLabelFor(spinner);
        spinnerLabelBox.add(spinner);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, spinner.getMinimumSize().height));

        JLabel l2 = new JLabel(unit);
        l2.setPreferredSize(new Dimension(30, l2.getMinimumSize().height));
        spinnerLabelBox.add(l2);

        return spinnerLabelBox;
    }

    /**
     * creates a 5 digit spinner
     * @param label name
     * @param model for spinner
     * @param unit label after the spinner box
     * @return box with labeled spinner with 5 digits
     */
    private static Box addLabeledSpinner5Digit(String label,
                                               SpinnerModel model,
                                               String unit) {

        Box spinnerLabelBox = new Box(BoxLayout.X_AXIS);
        JLabel l1 = new JLabel(label);
        l1.setPreferredSize(new Dimension(150, l1.getMinimumSize().height));
        spinnerLabelBox.add(l1);

        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)spinner.getEditor();
        DecimalFormat format = editor.getFormat();
        format.setMinimumFractionDigits(4);
        l1.setLabelFor(spinner);
        spinnerLabelBox.add(spinner);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, spinner.getMinimumSize().height));

        JLabel l2 = new JLabel(unit);
        l2.setPreferredSize(new Dimension(30, l2.getMinimumSize().height));
        spinnerLabelBox.add(l2);

        return spinnerLabelBox;
    }

    /**
     * resets the settings to default values
     */
    public class MyResetListener extends Component implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            boolean checkResetSettings = IJ.showMessageWithCancel("Warning!", "Reset Segmentation Settings?");

            if ( checkResetSettings ) {

                IJ.log("Resetting settings to default parameters");

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
                prominenceOrga = 2;

                // create tabbed panes
                nucSegBox.removeAll();
                setUpNucleiTab();
                tabbedPane.addTab("Nuclei", nucSegBox);

                cellSegBox.removeAll();
                setUpCellsTab();
                tabbedPane.addTab("Cells", cellSegBox);

                organelleBox.removeAll();
                setUpOrganellesTab();
                tabbedPane.addTab("Organelles", organelleBox);

            } else {

                IJ.log("Canceled resetting of processing settings!");

            }
        }
    }


    PreviewGui ( String inputDirectory, String outputDirectory, ArrayList<String> filesToProcess, String format) {

        inputDir = inputDirectory;
        outputDir = outputDirectory;
        fileList = filesToProcess;
        fileFormat = format;

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
        prominenceOrga = 2;

        // image settings
        calibrationSetting = false;
        pxSizeMicron = 0.1567095;
        nucleusChannel = 1;
        cytoplasmChannel = 2;
        organelleChannel = 3;
        measure = 0;

    }

    PreviewGui ( String inputDirectory, String outputDirectory, ArrayList<String> filesToProcess, String format,
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
                 boolean getCalibrationSetting,
                 double getPxSizeMicron,
                 int getNucleusChannel,
                 int getCytoplasmChannel,
                 int getOrganelleChannel,
                 int getMeasure) {

        inputDir = inputDirectory;
        outputDir = outputDirectory;
        fileList = filesToProcess;
        fileFormat = format;

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

        // image settings
        calibrationSetting = getCalibrationSetting;
        pxSizeMicron = getPxSizeMicron;
        nucleusChannel = getNucleusChannel;
        cytoplasmChannel = getCytoplasmChannel;
        organelleChannel = getOrganelleChannel;
        measure = getMeasure;
    }


    private class MyPreviewNucleusListener implements ActionListener {

        public void actionPerformed(ActionEvent a) {

            IJ.log("Starting preview for nuclei segmentation");

            Double nucFilterSizeDouble = (Double) doubleSpinKernelSizeNuc.getValue();
            float nucFilterSize = nucFilterSizeDouble.floatValue();
            Double nucRollBallRadius = (Double) doubleSpinrollingBallRadiusNuc.getValue();
            String nucThreshold = (String) thresholdListBack.getSelectedItem();
            Double nucErosionDouble = (Double) doubleSpinErosionNuc.getValue();
            int nucErosion= nucErosionDouble.intValue();
            Double nucMinSize = (Double) doubleSpinMinSize.getValue();
            Double nucMaxSize = (Double) doubleSpinMaxSize.getValue();
            Double nucLowCirc = (Double) doubleSpinLowCirc.getValue();
            Double nucHighCirc = (Double) doubleSpinHighCirc.getValue();

            boolean calibrationSetting = checkCalibration.isSelected();
            Double pxSizeMicronSetting = (Double) doubleSpinnerPixelSize.getValue();

            int selectionChecker = list.getSelectedIndex();

            // TODO: need to get settings from settings in preview GUI
            Image previewImage = new Image(inputDir, fileFormat,3, 0, 1, 2, 3);

            if (selectionChecker != -1){

                String selectedFile = (String) list.getSelectedValue();
                IJ.log("Selected File: " + selectedFile);

                // check if there are windows open already
                int openImages = WindowManager.getImageCount();

                // if there are image windows open check if they are of the list and of the selected image
                if  ( openImages != 0 ) {

                    IJ.log("There are images open!");

                    String[] openImage = WindowManager.getImageTitles();
                    ArrayList<String> openImageList = new ArrayList<>(Arrays.asList(openImage));

                    FileList fileUtility = new FileList(fileFormat);
                    ArrayList<String> openInputImages = fileUtility.intersection(openImageList, fileList);

                    boolean selectedFileChecker = false;

                    for (String image : openInputImages) {

                        if (image.equals(selectedFile)) {

                            IJ.log(selectedFile + " is already open");
                            selectedFileChecker = true;

                        } else {

                            IJ.selectWindow(image);
                            IJ.run("Close");

                        }

                    }

                    if (selectedFileChecker) {

                        IJ.log("Selected file is already open");
                        IJ.selectWindow(selectedFile);
                        ImagePlus selectedImage = WindowManager.getCurrentImage();
                        setDisplayRange = false;

                        if (calibrationSetting) {

                            Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                            selectedImage.setCalibration(calibration);
                            IJ.log("Metadata will be overwritten.");
                            IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                        } else {

                            // Here I just make sure that the calibration is really from the original
                            // in case the override metadata option has been set and unset before
                            ImagePlus imageForCalibration = previewImage.openImage(selectedFile);
                            Calibration originalCalibration = imageForCalibration.getCalibration();
                            selectedImage.setCalibration(originalCalibration);
                            imageForCalibration.close();
                            IJ.log("Metadata will not be overwritten");

                        }

                        SegmentationVisualizer visualizer = new SegmentationVisualizer();

                        visualizer.visualizeNucleiSegments(selectedImage,
                                previewImage,
                                nucFilterSize,
                                nucRollBallRadius,
                                nucThreshold,
                                nucErosion,
                                nucMinSize,
                                nucMaxSize,
                                nucLowCirc,
                                nucHighCirc,
                                setDisplayRange);

                    } else {

                        IJ.log("The selected image is not open");

                        // segment background and show for validation
                        setDisplayRange = true;

                        ImagePlus originalImage = previewImage.openImage(selectedFile);

                        if (calibrationSetting) {

                            Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                            originalImage.setCalibration(calibration);
                            IJ.log("Metadata will be overwritten.");
                            IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                        } else {

                            IJ.log("Metadata will not be overwritten");

                        }

                        SegmentationVisualizer visualizer = new SegmentationVisualizer();

                        visualizer.visualizeNucleiSegments(originalImage,
                                previewImage,
                                nucFilterSize,
                                nucRollBallRadius,
                                nucThreshold,
                                nucErosion,
                                nucMinSize,
                                nucMaxSize,
                                nucLowCirc,
                                nucHighCirc,
                                setDisplayRange);

                    }

                } else {

                    IJ.log("There are no images open!");

                    // segment background and show for validation
                    ImagePlus originalImage = previewImage.openImage(selectedFile);
                    setDisplayRange = true;

                    if (calibrationSetting) {

                        Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                        originalImage.setCalibration(calibration);
                        IJ.log("Metadata will be overwritten.");
                        IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                    } else {

                        IJ.log("Metadata will not be overwritten");

                    }

                    SegmentationVisualizer visualizer = new SegmentationVisualizer();

                    visualizer.visualizeNucleiSegments(originalImage,
                            previewImage,
                            nucFilterSize,
                            nucRollBallRadius,
                            nucThreshold,
                            nucErosion,
                            nucMinSize,
                            nucMaxSize,
                            nucLowCirc,
                            nucHighCirc,
                            setDisplayRange);
                }

            } else {

                IJ.error("Please choose a file in the file list!");

            }


        }
    }

    private class MyPreviewCellListener implements ActionListener {

        public void actionPerformed(ActionEvent a) {

            IJ.log("Starting preview for cell segmentation");

            Double cellAreaFilterSize = (Double) doubleSpinKernelCellArea.getValue();
            float cellAreaFilterSizeFloat = cellAreaFilterSize.floatValue();
            Double cellAreaRollBall = (Double) doubleSpinRollBallCellArea.getValue();
            Double cellAreaThreshold = (Double)  doubleSpinThresholdCellArea.getValue();
            int cellAreaThresholdFloat = cellAreaThreshold.intValue();
            Double cellSepGaussCellSep = (Double) doubleSpinGaussCellSep.getValue();
            Double cellSepProminence = (Double) doubleSpinProminenceCellSep.getValue();

            Double cellFilterMinSize = (Double) doubleSpinMinSizeCellFilter.getValue();
            Double cellFilterMaxSize = (Double) doubleSpinMaxSizeCellFilter.getValue();
            Double cellFilterLowCirc = (Double) doubleSpinLowCircCellFilter.getValue();
            Double cellFilterHighCirc = (Double) doubleSpinHighCircCellFilter.getValue();
            boolean cellFilterCheck = checkFilterCellFilter.isSelected();

            boolean calibrationSetting = checkCalibration.isSelected();
            Double pxSizeMicronSetting = (Double) doubleSpinnerPixelSize.getValue();
            int selectionChecker = list.getSelectedIndex();

            // TODO: need to get settings from settings in preview GUI
            Image previewImage = new Image(inputDir, fileFormat,3, 0, 1, 2, 3);

            if (selectionChecker != -1){

                String selectedFile = (String) list.getSelectedValue();
                IJ.log("Selected File: " + selectedFile);

                // check if there are windows open already
                int openImages = WindowManager.getImageCount();

                // if there are image windows open check if they are of the list and of the selected image
                if  ( openImages != 0 ) {

                    IJ.log("There are images open!");

                    String[] openImage = WindowManager.getImageTitles();
                    ArrayList<String> openImageList = new ArrayList<>(Arrays.asList(openImage));

                    FileList fileUtility = new FileList(fileFormat);
                    ArrayList<String> openInputImages = fileUtility.intersection(openImageList, fileList);

                    boolean selectedFileChecker = false;

                    for (String image : openInputImages) {

                        if (image.equals(selectedFile)) {

                            IJ.log(selectedFile + " is already open");
                            selectedFileChecker = true;

                        } else {

                            IJ.selectWindow(image);
                            IJ.run("Close");

                        }

                    }

                    if (selectedFileChecker) {

                        IJ.log("Selected file is already open");
                        IJ.selectWindow(selectedFile);
                        ImagePlus selectedImage = WindowManager.getCurrentImage();
                        setDisplayRange = false;

                        if (calibrationSetting) {

                            Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                            selectedImage.setCalibration(calibration);
                            IJ.log("Metadata will be overwritten.");
                            IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                        } else {

                            // Here I just make sure that the calibration is really from the original
                            // in case the override metadata option has been set and unset before
                            ImagePlus imageForCalibration = previewImage.openImage(selectedFile);
                            Calibration originalCalibration = imageForCalibration.getCalibration();
                            selectedImage.setCalibration(originalCalibration);
                            imageForCalibration.close();
                            IJ.log("Metadata will not be overwritten");

                        }

                        SegmentationVisualizer visualizer = new SegmentationVisualizer();

                        visualizer.visualizeCellSegments(selectedImage, previewImage, cellAreaFilterSizeFloat, cellAreaRollBall, cellAreaThresholdFloat, cellSepGaussCellSep, cellSepProminence, cellFilterMinSize, cellFilterMaxSize, cellFilterLowCirc, cellFilterHighCirc, setDisplayRange);

                    } else {

                        IJ.log("The selected image is not open");

                        // segment background and show for validation
                        setDisplayRange = true;

                        ImagePlus originalImage = previewImage.openImage(selectedFile);

                        if (calibrationSetting) {

                            Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                            originalImage.setCalibration(calibration);
                            IJ.log("Metadata will be overwritten.");
                            IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                        } else {

                            IJ.log("Metadata will not be overwritten");

                        }

                        SegmentationVisualizer visualizer = new SegmentationVisualizer();

                        visualizer.visualizeCellSegments(originalImage, previewImage, cellAreaFilterSizeFloat, cellAreaRollBall, cellAreaThresholdFloat, cellSepGaussCellSep, cellSepProminence, cellFilterMinSize, cellFilterMaxSize, cellFilterLowCirc, cellFilterHighCirc, setDisplayRange);

                    }

                } else {

                    IJ.log("There are no images open!");

                    // segment background and show for validation
                    ImagePlus originalImage = previewImage.openImage(selectedFile);
                    setDisplayRange = true;

                    if (calibrationSetting) {

                        Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                        originalImage.setCalibration(calibration);
                        IJ.log("Metadata will be overwritten.");
                        IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                    } else {

                        IJ.log("Metadata will not be overwritten");

                    }

                    SegmentationVisualizer visualizer = new SegmentationVisualizer();

                    visualizer.visualizeCellSegments(originalImage, previewImage, cellAreaFilterSizeFloat, cellAreaRollBall, cellAreaThresholdFloat, cellSepGaussCellSep, cellSepProminence, cellFilterMinSize, cellFilterMaxSize, cellFilterLowCirc, cellFilterHighCirc, setDisplayRange);
                }

            } else {

                IJ.error("Please choose a file in the file list!");

            }


        }
    }

    private class MyPreviewOrganelleListener implements ActionListener {

        public void actionPerformed(ActionEvent a) {

            IJ.log("Starting preview for cell segmentation");

            Double organelleLoGSigma = (Double) doubleSpinnerLoGOragenelle.getValue();
            Double organelleProminence = (Double) doubleSpinnerProminenceOrganelle.getValue();
            boolean organellteFilterCheck = checkFilterOrganelle.isSelected();

            boolean calibrationSetting = checkCalibration.isSelected();
            Double pxSizeMicronSetting = (Double) doubleSpinnerPixelSize.getValue();
            int selectionChecker = list.getSelectedIndex();

            // TODO: need to get settings from settings in preview GUI
            Image previewImage = new Image(inputDir, fileFormat,3, 0, 1, 2, 3);

            if (selectionChecker != -1){

                String selectedFile = (String) list.getSelectedValue();
                IJ.log("Selected File: " + selectedFile);

                // check if there are windows open already
                int openImages = WindowManager.getImageCount();

                // if there are image windows open check if they are of the list and of the selected image
                if  ( openImages != 0 ) {

                    IJ.log("There are images open!");

                    String[] openImage = WindowManager.getImageTitles();
                    ArrayList<String> openImageList = new ArrayList<>(Arrays.asList(openImage));

                    FileList fileUtility = new FileList(fileFormat);
                    ArrayList<String> openInputImages = fileUtility.intersection(openImageList, fileList);

                    boolean selectedFileChecker = false;

                    for (String image : openInputImages) {

                        if (image.equals(selectedFile)) {

                            IJ.log(selectedFile + " is already open");
                            selectedFileChecker = true;

                        } else {

                            IJ.selectWindow(image);
                            IJ.run("Close");

                        }

                    }

                    if (selectedFileChecker) {

                        IJ.log("Selected file is already open");
                        IJ.selectWindow(selectedFile);
                        ImagePlus selectedImage = WindowManager.getCurrentImage();
                        setDisplayRange = false;

                        if (calibrationSetting) {

                            Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                            selectedImage.setCalibration(calibration);
                            IJ.log("Metadata will be overwritten.");
                            IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                        } else {

                            // Here I just make sure that the calibration is really from the original
                            // in case the override metadata option has been set and unset before
                            ImagePlus imageForCalibration = previewImage.openImage(selectedFile);
                            Calibration originalCalibration = imageForCalibration.getCalibration();
                            selectedImage.setCalibration(originalCalibration);
                            imageForCalibration.close();
                            IJ.log("Metadata will not be overwritten");

                        }

                        SegmentationVisualizer visualizer = new SegmentationVisualizer();

                        visualizer.visualizeSpots(selectedImage, previewImage, organelleLoGSigma, organelleProminence, setDisplayRange);

                    } else {

                        IJ.log("The selected image is not open");

                        // segment background and show for validation
                        setDisplayRange = true;

                        ImagePlus originalImage = previewImage.openImage(selectedFile);

                        if (calibrationSetting) {

                            Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                            originalImage.setCalibration(calibration);
                            IJ.log("Metadata will be overwritten.");
                            IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                        } else {

                            IJ.log("Metadata will not be overwritten");

                        }

                        SegmentationVisualizer visualizer = new SegmentationVisualizer();

                        visualizer.visualizeSpots(originalImage, previewImage, organelleLoGSigma, organelleProminence, setDisplayRange);

                    }

                } else {

                    IJ.log("There are no images open!");

                    // segment background and show for validation
                    ImagePlus originalImage = previewImage.openImage(selectedFile);
                    setDisplayRange = true;

                    if (calibrationSetting) {

                        Calibration calibration = Image.calibrate("µm", pxSizeMicronSetting);
                        originalImage.setCalibration(calibration);
                        IJ.log("Metadata will be overwritten.");
                        IJ.log("Pixel size set to: " + pxSizeMicronSetting);

                    } else {

                        IJ.log("Metadata will not be overwritten");

                    }

                    SegmentationVisualizer visualizer = new SegmentationVisualizer();

                    visualizer.visualizeSpots(originalImage, previewImage, organelleLoGSigma, organelleProminence, setDisplayRange);

                }

            } else {

                IJ.error("Please choose a file in the file list!");

            }


        }


    }
}

