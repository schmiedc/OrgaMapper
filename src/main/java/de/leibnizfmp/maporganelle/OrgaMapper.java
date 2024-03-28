/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.Prefs;
import net.imagej.ImageJ;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;


/**
 *
 */
@Plugin(type = Command.class, menuPath = "Plugins>Cellular Imaging>Map Organelle")
public class OrgaMapper<T extends RealType<T>>  implements Command {

    @Override
    public void run() {

        Prefs.blackBackground = true;

        IJ.log("Starting OrgaMapper plugin");

        InputGuiFiji start = new InputGuiFiji();
        start.createWindow();

    }

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     */
    public static void main(final String... args) throws Exception {

        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        Prefs.blackBackground = true;

        ij.command().run(OrgaMapper.class, true);

        boolean runTest1 = false;
        boolean runTest2 = false;
        boolean runTest3 = false;

        if ( runTest1 || runTest2 || runTest3 ) {

            String testInDir = "/home/schmiedc/FMP_Docs/Projects/OrgaMapper/2024-02-29_Revision/Feature_External-Detection/input/";

            String testOutDir = "/home/schmiedc/FMP_Docs/Projects/OrgaMapper/2024-02-29_Revision/Feature_External-Detection/output/";

            //String fileEnding = ".nd2";
            String fileEnding = ".tif";

            //String settings = "";
            String settings = "/home/schmiedc/FMP_Docs/Projects/OrgaMapper/2024-02-29_Revision/Feature_External-Detection/2024-03-21T191005-settings.xml";

            if ( runTest1 ) {

                FileList getFileList = new FileList(fileEnding);

                // generates the file list that is fed to the preview GUI
                FileListProperties fileListProperties = getFileList.getFileMultiSeriesList(testInDir);
                ArrayList<String> fileList = fileListProperties.fileList;
                boolean multiSeriesSwitch = fileListProperties.multiSeries;

                for (String file : fileList) {
                    System.out.println(file);
                }

                ArrayList<String> fileListTest = new ArrayList<>(fileList.subList(0, 3));

                IJ.log("Run test 1");
                PreviewGui guiTest = new PreviewGui(
                        testInDir,
                        testOutDir,
                        fileListTest,
                        fileEnding,
                        3,
                        0.157,
                        1,
                        2,
                        3,
                        multiSeriesSwitch);

                guiTest.setUpGui();

                IJ.log("Test 1 done");

            } else if ( runTest2 ) {

                IJ.log("Run test 2");
                InputGuiFiji guiTest = new InputGuiFiji(testInDir, testOutDir, fileEnding, settings);
                guiTest.createWindow();

                //BatchProcessor processBatch = new BatchProcessor(testInDir, testOutDir, fileListTest, fileEnding, channelNumber);
                //processBatch.processImage();

            } else if (runTest3) {


                //private String externalNucleusSegmentationDirectory = "/home/schmiedc/FMP_Docs/Projects/OrgaMapper/2024-02-29_Revision/Feature_External-Detection/input_extSegDetect/";
                //private String externalCellSegmentationDirectory = externalNucleusSegmentationDirectory;
                //private String externalDetectionDirectory = externalNucleusSegmentationDirectory;
                //private String externalSegmentationFileEnding = ".tif";
                //private String externalNucleusSegmentationSuffix = "NucSeg";
                //private String externalCellSegmentationSuffix = "CellSeg";
                //private String externalDetectionSuffix = "Detect";

                IJ.log("Test External segmentation GUI");
                // ExtSegDetectGUI guiTest = new ExtSegDetectGUI(true, false, false);
                // guiTest.createWindow();

            }

        }
    }

}
