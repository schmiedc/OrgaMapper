package de.leibnizfmp;

import ij.IJ;
import org.scijava.util.ArrayUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

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

    // settings for nucleus settings
    private float kernelSizeNuc;
    private double rollingBallRadiusNuc;
    private String thresholdNuc;
    private int erosionNuc;
    private double minSizeNuc;
    private double maxSizeNuc;
    private double lowCircNuc;
    private double highCircNuc;

    // settings for cell area segmentation
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
    private double pxSizeMicron;
    private int nucleusChannel;
    private int cytoplasmChannel;
    private int organelleChannel;
    private int measure;

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
        JList list = new JList(fileList.toArray());

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

        SpinnerModel doubleSpinKernelSizeNuc = new SpinnerNumberModel(kernelSizeNuc, 0.0, 50.0, 1.0);
        String spinBackLabel1 = "Median filter size: ";
        String spinBackUnit1 = "px";
        Box spinnerBack1 = addLabeledSpinnerUnit(spinBackLabel1, doubleSpinKernelSizeNuc, spinBackUnit1);
        segmentationBox.add(spinnerBack1);

        SpinnerModel doubleSpinrollingBallRadiusNuc = new SpinnerNumberModel(rollingBallRadiusNuc, 0.0, 10000, 1.0);
        String spinBackLabel2 = "Rolling ball radius: ";
        String spinBackUnit2 = "px";
        Box spinnerBack2 = addLabeledSpinnerUnit(spinBackLabel2, doubleSpinrollingBallRadiusNuc, spinBackUnit2);
        segmentationBox.add(spinnerBack2);

        JComboBox<String> thresholdListBack = new JComboBox<>(thresholdString);
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

        SpinnerModel doubleSpinErosionNuc = new SpinnerNumberModel(erosionNuc, 0.0, 10, 1.0);
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

        SpinnerModel doubleSpinMinSize = new SpinnerNumberModel(minSizeNuc,0.0,1000000,10.0);
        String minSizeLabel = "Select min. size: ";
        String minUnitLabel = "µm²";
        Box spinnerNuc4 = addLabeledSpinnerUnit(minSizeLabel, doubleSpinMinSize, minUnitLabel );
        filterBox.add(spinnerNuc4);

        SpinnerModel doubleSpinMaxSize = new SpinnerNumberModel(maxSizeNuc,0.0,1000000,10.0);
        String maxSizeLabel = "Select max. size: ";
        String maxUnitLabel = "µm²";
        Box spinnerNuc5 = addLabeledSpinnerUnit(maxSizeLabel, doubleSpinMaxSize, maxUnitLabel);
        filterBox.add(spinnerNuc5);

        SpinnerModel doubleSpinLowCirc = new SpinnerNumberModel(lowCircNuc,0.0,1.0,0.1);
        String minCircLabel = "Select minimal circularity: ";
        String minCircUnit = "";
        Box lowCircBox = addLabeledSpinnerUnit(minCircLabel, doubleSpinLowCirc, minCircUnit);
        filterBox.add(lowCircBox);

        SpinnerModel doubleSpinHighCirc = new SpinnerNumberModel(highCircNuc,0.0,1.0,0.1);
        String highCircLabel = "Select maximal circularity: ";
        String highCircUnit = "";
        Box highCircBox = addLabeledSpinnerUnit(highCircLabel, doubleSpinHighCirc, highCircUnit);
        filterBox.add(highCircBox);

        nucSegBox.add(filterBox);

        // setup Buttons
        JButton previewButton = new JButton("Preview");
        //previewButton.addActionListener(new MyPreviewNucleusListener());
        nucSegBox.add(previewButton);

    }

    private void setUpCellsTab() {

        // box with titled borders
        Box segmentationBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleSegmentation;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleSegmentation = BorderFactory.createTitledBorder(blackline, "Processing and threshold: ");
        segmentationBox.setBorder(titleSegmentation);

        SpinnerModel doubleSpinKernelSizeNuc = new SpinnerNumberModel(kernelSizeCellArea, 0.0, 50.0, 1.0);
        String spinBackLabel1 = "Median filter size: ";
        String spinBackUnit1 = "px";
        Box spinnerBack1 = addLabeledSpinnerUnit(spinBackLabel1, doubleSpinKernelSizeNuc, spinBackUnit1);
        segmentationBox.add(spinnerBack1);

        SpinnerModel doubleSpinrollingBallRadiusNuc = new SpinnerNumberModel(rollingBallRadiusCellArea, 0.0, 10000, 1.0);
        String spinBackLabel2 = "Rolling ball radius: ";
        String spinBackUnit2 = "px";
        Box spinnerBack2 = addLabeledSpinnerUnit(spinBackLabel2, doubleSpinrollingBallRadiusNuc, spinBackUnit2);
        segmentationBox.add(spinnerBack2);

        SpinnerModel doubleSpinThreshold = new SpinnerNumberModel(manualThresholdCellArea, 0.0, 65536, 1.0);
        String spinBackLabel3 = "Global Threshold: ";
        String spinBackUnit3 = "A.U.";
        Box spinnerBack3 = addLabeledSpinnerUnit(spinBackLabel3, doubleSpinThreshold, spinBackUnit3);
        segmentationBox.add(spinnerBack3);

        cellSegBox.add(segmentationBox);

        // settings for cell separator
        //private double prominenceCellSep;
        Box separationBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleSeparation;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleSeparation = BorderFactory.createTitledBorder(blackline, "Settings for separating cells: ");
        separationBox.setBorder(titleSeparation);

        SpinnerModel doubleSpinGaussCellSep = new SpinnerNumberModel(kernelSizeCellArea, 0.0, 50.0, 1.0);
        String spinGaussCellSep = "Gauss sigma: ";
        String spinGaussCellSepUnit = "px";
        Box spinnerGaussCellSep = addLabeledSpinnerUnit(spinGaussCellSep, doubleSpinGaussCellSep, spinGaussCellSepUnit);
        separationBox.add(spinnerGaussCellSep);

        SpinnerModel doubleSpinnerProminenceSpot = new SpinnerNumberModel(prominenceCellSep, 0.0,1000.0, 0.0001);
        String spinLabelProminence = "Prominence: ";
        String spinUnitProminence = "A.U.";
        Box spinSpot2 = addLabeledSpinner5Digit(spinLabelProminence, doubleSpinnerProminenceSpot, spinUnitProminence);
        separationBox.add(spinSpot2);

        cellSegBox.add(separationBox);

        // settings for cell filter size
        Box filterBox = new Box(BoxLayout.Y_AXIS);
        TitledBorder titleFilter;
        blackline = BorderFactory.createLineBorder(Color.black);
        titleFilter = BorderFactory.createTitledBorder(blackline, "Filter: size");
        filterBox.setBorder(titleFilter);

        SpinnerModel doubleSpinMinSize = new SpinnerNumberModel(minCellSize,0.0,1000000,10.0);
        String minSizeLabel = "Select min. size: ";
        String minUnitLabel = "µm²";
        Box spinnerNuc4 = addLabeledSpinnerUnit(minSizeLabel, doubleSpinMinSize, minUnitLabel );
        filterBox.add(spinnerNuc4);

        SpinnerModel doubleSpinMaxSize = new SpinnerNumberModel(maxCellSize,0.0,1000000,10.0);
        String maxSizeLabel = "Select max. size: ";
        String maxUnitLabel = "µm²";
        Box spinnerNuc5 = addLabeledSpinnerUnit(maxSizeLabel, doubleSpinMaxSize, maxUnitLabel);
        filterBox.add(spinnerNuc5);

        SpinnerModel doubleSpinLowCirc = new SpinnerNumberModel(lowCircCellSize,0.0,1.0,0.1);
        String minCircLabel = "Select minimal circularity: ";
        String minCircUnit = "";
        Box lowCircBox = addLabeledSpinnerUnit(minCircLabel, doubleSpinLowCirc, minCircUnit);
        filterBox.add(lowCircBox);

        SpinnerModel doubleSpinHighCirc = new SpinnerNumberModel(highCircCelLSize,0.0,1.0,0.1);
        String highCircLabel = "Select maximal circularity: ";
        String highCircUnit = "";
        Box highCircBox = addLabeledSpinnerUnit(highCircLabel, doubleSpinHighCirc, highCircUnit);
        filterBox.add(highCircBox);

        cellSegBox.add(filterBox);

        JCheckBox checkFilter = new JCheckBox("Filter by nuclei?");
        checkFilter.setSelected(true);
        checkFilter.setToolTipText("Only affects visualization");
        cellSegBox.add(checkFilter);

        // setup Buttons
        JButton previewButton = new JButton("Preview");
        //previewButton.addActionListener(new MyPreviewCellListener());
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
        SpinnerModel doubleSpinnerLoGSpot = new SpinnerNumberModel(sigmaLoGOrga, 0.0,20.0, 0.1);
        String spinLabelSpot1 = "LoG sigma: ";
        String spinUnitSpot1 = "px";
        Box spinSpot1 = addLabeledSpinnerUnit(spinLabelSpot1, doubleSpinnerLoGSpot, spinUnitSpot1);
        detectionBox.add(spinSpot1);

        SpinnerModel doubleSpinnerProminenceSpot = new SpinnerNumberModel(prominenceOrga, 0.0,1000.0, 0.0001);
        String spinLabelSpot2 = "Prominence: ";
        String spinUnitSpot2 = "A.U.";
        Box spinSpot2 = addLabeledSpinner5Digit(spinLabelSpot2, doubleSpinnerProminenceSpot, spinUnitSpot2);
        detectionBox.add(spinSpot2);

        organelleBox.add(detectionBox);

        JCheckBox checkFilter = new JCheckBox("Filter in nucleus?");
        checkFilter.setToolTipText("Only affects visualization");
        checkFilter.setSelected(true);
        organelleBox.add(checkFilter);

        // setup Buttons
        JButton previewButton = new JButton("Preview");
        //previewButton.addActionListener(new MyPreviewOrganelleListener());
        organelleBox.add(previewButton);

    }

    private void setUpSettingsTab() {

        JLabel settingsLabel = new JLabel("Specify experimental Settings: ");
        boxSettings.add(settingsLabel);

        SpinnerModel doubleSpinnerPixelSize = new SpinnerNumberModel(pxSizeMicron, 0.000,10.000, 0.001);
        String pixelSizeLabel = "Pixel size: ";
        String pixelSizeUnit = "µm";
        Box boxPixelSize = addLabeledSpinnerUnit(pixelSizeLabel,doubleSpinnerPixelSize, pixelSizeUnit);
        boxSettings.add(boxPixelSize);

        JCheckBox checkCalibration = new JCheckBox("Override metadata?");
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

            Boolean checkResetSettings = IJ.showMessageWithCancel("Warning!", "Reset Segmentation Settings?");

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


    PreviewGui ( String inputDirectory, String outputDirectory, ArrayList<String> filesToProcess ) {

        inputDir = inputDirectory;
        outputDir = outputDirectory;
        fileList = filesToProcess;

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

    PreviewGui ( String inputDirectory, String outputDirectory, ArrayList<String> filesToProcess,
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


}

