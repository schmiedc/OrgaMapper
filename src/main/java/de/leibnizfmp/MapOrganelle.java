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
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.Binary;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import ij.Prefs;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import vib.oldregistration.RegistrationAlgorithm;

import java.util.ArrayList;


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
        //ij.command().run(MapOrganelle.class, true);

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

            ArrayList<String> fileListTest = new ArrayList<>(fileList.subList(0, 2));


            if ( runTest1 ) {


                PreviewGui guiTest = new PreviewGui(testInDir, testOutDir, fileList, fileEnding, 3);
                guiTest.setUpGui();

                //InputGuiFiji guiTest = new InputGuiFiji(testInDir, testOutDir, channelNumber,fileEnding, settings);
                //guiTest.createWindow();

                //BatchProcessor processBatch = new BatchProcessor(testInDir, testOutDir, fileListTest, fileEnding, channelNumber);
                //processBatch.processImage();


            } else if (runTest2) {

                // image settings
                int nucleusChannel = 1;
                int cytoplasmChannel = 2;
                int organelleChannel = 3;
                int measureChannel = 0;

                // settings for nucleus settings
                float kernelSizeNuc = 5;
                double rollingBallRadiusNuc = 50;
                String thresholdNuc = "Otsu";
                int erosionNuc = 2;
                double minSizeNuc = 100;
                double maxSizeNuc = 20000;
                double lowCircNuc = 0.0;
                double highCircNuc = 1.00;

                String fileName = fileList.get(0);

                // get file names
                String fileNameWOtExt = fileName.substring(0, fileName.lastIndexOf("_S"));
                // get series number from file name
                int stringLength = fileName.length();
                String seriesNumberString;
                seriesNumberString = fileName.substring( fileName.lastIndexOf("_S") + 2 , stringLength );
                int seriesNumber = Integer.parseInt(seriesNumberString);

                // open image
                Image processingImage = new Image(testInDir, fileEnding, channelNumber, seriesNumber, nucleusChannel, cytoplasmChannel, organelleChannel, measureChannel);
                ImagePlus image = processingImage.openWithMultiseriesBF( fileName );

                // open individual channels
                ImagePlus[] imp_channels = ChannelSplitter.split(image);
                ImagePlus nucleus = imp_channels[processingImage.nucleus - 1];
                ImagePlus cytoplasm = imp_channels[processingImage.cytoplasm - 1];
                ImagePlus organelle = imp_channels[processingImage.organelle - 1];
                nucleus.setOverlay(null);

                // get nucleus masks
                ImagePlus  nucleusMask = NucleusSegmenter.segmentNuclei(nucleus, kernelSizeNuc, rollingBallRadiusNuc, thresholdNuc, erosionNuc, minSizeNuc, maxSizeNuc, lowCircNuc, highCircNuc);
                nucleusMask.show();

                FileSaver save = new FileSaver(nucleusMask);
                save.saveAsTiff(testOutDir + "/nucleusMask.tif");

            }

        }
    }

}
