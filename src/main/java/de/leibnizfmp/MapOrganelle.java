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

import loci.common.DebugTools;
import loci.formats.meta.IMetadata;
import loci.plugins.in.ImporterOptions;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.RealType;
import org.knowm.xchart.style.markers.None;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import loci.formats.FormatException;
import loci.plugins.BF;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import sun.rmi.server.UnicastServerRef;

import java.io.IOException;

/**
 * This example illustrates how to create an ImageJ {@link Command} plugin.
 * <p>
 * The code here is a simple Gaussian blur using ImageJ Ops.
 * </p>
 * <p>
 * You should replace the parameter fields with your own inputs and outputs,
 * and replace the {@link run} method implementation with your own logic.
 * </p>
 */
@Plugin(type = Command.class, menuPath = "Plugins>Map Organelle")
public class MapOrganelle<T extends RealType<T>> implements Command {
    //
    // Feel free to add more parameters here...
    //

    @Parameter
    private Dataset currentData;

    @Parameter
    private UIService uiService;

    @Parameter
    private OpService opService;

    @Override
    public void run() {

        // test paths
        String workingDir = "/data1/FMP_Docs/Projects/orgaPosJ_ME/";
        //String inputDir = "/Plugin_InputTest/";
        String inputDir = "/TestDataSet_LysoPos/";
        String outputDir = "/Plugin_OutputTest/";
        //String testFile =  "HeLa_control_1_WellA1_Seq0000.nd2 - HeLa_control_1_WellA1_Seq0000.nd2 (series 01).tif";
        String testFile = "HeLa_scr.nd2";
        String testInput = workingDir + inputDir;
        String inputPath = workingDir + inputDir + testFile;

        Image testImage = new Image(testInput, ".nd2", 1.0, 3, 1, "nucleus", "cytoplams", "lysosome","None");
        ImagePlus imp = testImage.openImageBF(testFile);
        imp.show();

        try {

            DebugTools.setRootLevel("OFF");

            // get the meta data from multiseries file
            ImageReader reader = new ImageReader();
            IMetadata omeMeta = MetadataTools.createOMEXMLMetadata();
            reader.setMetadataStore(omeMeta);
            reader.setId(inputPath);
            int nSeries = reader.getSeriesCount();
            reader.close();

        } catch (FormatException e) {
            IJ.error("Sorry, an error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            IJ.error("Sorry, an error occurred: " + e.getMessage());
        }




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
        new ImageJ();

        new MapOrganelle().run();


        //final UnsignedByteType threshold = new UnsignedByteType( 127 );
        //final net.imagej.ImageJ ij = new ImageJ();
        //final Img< BitType > mask = (Img<BitType>) ij.op().threshold().apply( img, threshold );
        //ImageJFunctions.show( mask );

    }

}
