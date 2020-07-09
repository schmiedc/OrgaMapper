package de.leibnizfmp;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PreviewGui extends JPanel {

    private String inputDir;
    private String outputDir;
    private ArrayList<String> fileList;

    // tabbed pane
    private JTabbedPane tabbedPane = new JTabbedPane();
    JFrame theFrame;

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
        //tabbedPane.addTab("Nuclei", boxSpotSeg);

        setUpCellsTab();
        //tabbedPane.addTab("Cells", boxBackground);

        setUpOrganellesTab();
        //tabbedPane.addTab("Organelles", boxBackground);

        setUpSettingsTab();
        //batchBox.add(boxSettings);

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
        //resetButton.addActionListener(new MyResetListener());
        saveLoadBox.add(resetButton);

        JButton resetDirButton = new JButton("Reset Directories");
        //resetDirButton.addActionListener(new MyResetDirectoryListener());
        saveLoadBox.add(resetDirButton);

        // add boxes to panel and frame
        background.add(BorderLayout.WEST, tabbedPane);
        //background.add(BorderLayout.EAST, batchBox);
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

    private void setUpSettingsTab() {
    }

    private void setUpOrganellesTab() {
    }

    private void setUpCellsTab() {
    }

    private void setUpNucleiTab() {
    }

    PreviewGui ( String inputDirectory, String outputDirectory, ArrayList<String> filesToProcess ) {

        inputDir = inputDirectory;
        outputDir = outputDirectory;
        fileList = filesToProcess;

    }


}
