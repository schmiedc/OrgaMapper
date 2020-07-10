package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

import java.io.File;
import java.io.IOException;

/**
 * this class implements the image that is analyzed
 * an image has a source directory, format, px size, px unit, number of channels,
 * channel identities, series number
 *
 * @author christopher schmied
 * @version 1.0.0
 */

public class Image {

    /**
     * directory : String, source directory
     */
    private final String directory;

    /**
     * format: String, format of the image
     */
    public final String format;

    /**
     * numChannel : int, number of channels
     */
    private final int numChannel;

    /**
     * numSeries : int, number of series
     */
    private final int seriesID;

    /**
     *  nucleus : int, number of nucleus channel
     */
    public final int nucleus;

    /**
     *  cytoplasm : int, number of cytoplasm channel
     */
    public final int cytoplasm;

    /**
     *  organelle : int, number of organelle channel
     */
    public final int organelle;

    /**
     *  measure : int, number of measurement channel
     */
    public final int measure;

    /**
     * opens the image using the ImageJ default opener
     *
     * @param inputFile name of image
     * @return and ImagePlus object
     */
    ImagePlus openImage(String inputFile) {

        // open a example pHlorin image
        IJ.log("Opening file: " + inputFile);
        ImagePlus image = IJ.openImage(directory + File.separator + inputFile);
        image.setTitle(inputFile);

        return image;
    }

    /**
     * opens the image using bio-formats importer
     *
     * @param inputFile name of image
     * @return and ImagePlus object
     */
    ImagePlus openWithBF(String inputFile) {

        IJ.log("Opening file: " + inputFile);

        ImporterOptions options = null;
        try {
            options = new ImporterOptions();
        } catch (IOException e) {
            IJ.error("Bio-formats I/O: " + e.getMessage());
            e.printStackTrace();
        }
        options.setId(directory + File.separator + inputFile);
        options.setSeriesOn(seriesID,true);
        ImagePlus[] imp = new ImagePlus[0];
        try {
            imp = BF.openImagePlus(options);
        } catch (FormatException e) {
            IJ.error("Bio-formats format exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            IJ.error("Bio-formats cannot open file: " + e.getMessage());
            e.printStackTrace();
        }

        return imp[0];
    }

    /**
     * sets the calibration of the image
     *
     * @return the scale/calibration of the image
     */
    static Calibration calibrate(String sizeUnit, double pxSizeCalib){

        Calibration imageScale = new Calibration();
        imageScale.setXUnit(sizeUnit);
        imageScale.setYUnit(sizeUnit);

        imageScale.pixelHeight = pxSizeCalib;
        imageScale.pixelWidth = pxSizeCalib;

        return imageScale;

    }

    /**
     * converts SI unit size into pixels
     *
     * @param pxSize in micron
     * @param size specified size
     * @return size in pixel
     */
    static int calculateSizePx(Double pxSize, Double size) {

        double pxArea = pxSize * pxSize;

        return (int)Math.round(size / pxArea);

    }

    static int calculateMaxArea(int width, int heigth) {

        return width * heigth;

    }

    /**
     * Image constructor
     *
     * @param inputDir source directory
     * @param imageFormat image format
     * @param pxSize pixel size in micron
     * @param numberChannels number of Channels
     * @param whichSeriesNum the series ID
     * @param nucleusChannel identity of channel 1
     * @param cytoplasmChannel identity of channel 2
     * @param organelleChannel identity of channel 3
     * @param measureChannel  identity of channel 4
     */
    public Image(String inputDir, String imageFormat, int numberChannels,
                 int whichSeriesNum, int nucleusChannel, int cytoplasmChannel, int organelleChannel, int measureChannel ){

        directory = inputDir;
        format = imageFormat;
        numChannel = numberChannels;
        seriesID = whichSeriesNum;
        nucleus = nucleusChannel;
        cytoplasm = cytoplasmChannel;
        organelle = organelleChannel;
        measure = measureChannel ;

    }

    public Image(String inputDir, String imageFormat, int numberChannels,
                 int whichSeriesNum, int nucleusChannel, int cytoplasmChannel, int organelleChannel){

        directory = inputDir;
        format = imageFormat;
        numChannel = numberChannels;
        seriesID = whichSeriesNum;
        nucleus = nucleusChannel;
        cytoplasm = cytoplasmChannel;
        organelle = organelleChannel;
        measure = 0;

    }




}
