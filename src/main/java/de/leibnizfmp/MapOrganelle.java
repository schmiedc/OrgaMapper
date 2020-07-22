/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import ij.Prefs;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */
@Plugin(type = Command.class, menuPath = "Plugins>Cellular Imaging>Map Organelle")
public class MapOrganelle<T extends RealType<T>>  implements Command {

    @Override
    public void run() {

        Prefs.blackBackground = true;

        IJ.log("Starting map-organelle plugin");

        InputGuiFiji start = new InputGuiFiji();
        start.createWindow();

    }

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {

        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        Prefs.blackBackground = true;
        ij.command().run(MapOrganelle.class, true);

        boolean runTest1 = true;
        boolean runTest2 = false;

        if ( runTest1 || runTest2 ) {

            String testInDir = "/data1/FMP_Docs/Projects/orgaPosJ_ME/Plugin_InputTest/";
            //String testInDir = "/data1/FMP_Docs/Projects/orgaPosJ_ME/Plugin_InputTest_nd2/";
            String testOutDir = "/data1/FMP_Docs/Projects/orgaPosJ_ME/Plugin_OutputTest";
            int channelNumber = 3;
            String fileEnding = ".tif";
            //String fileEnding = ".nd2";
            String settings = "setting";

            FileList getFileList = new FileList(fileEnding);
            ArrayList<String> fileList = getFileList.getFileMultiSeriesList(testInDir);

            for (String file : fileList) {
                System.out.println(file);
            }


            if ( runTest1 ) {


                PreviewGui guiTest = new PreviewGui(testInDir, testOutDir, fileList, fileEnding, 3);
                guiTest.setUpGui();

                //InputGuiFiji guiTest = new InputGuiFiji(testInDir, testOutDir, channelNumber,fileEnding, settings);
                //guiTest.createWindow();

            } else if (runTest2) {

                ArrayList<String> fileListTest = new ArrayList<>(fileList.subList(0, 2));

                BatchProcessor processBatch = new BatchProcessor(testInDir, testOutDir, fileListTest, fileEnding, channelNumber);
                processBatch.processImage();

            }

        }
    }

}
