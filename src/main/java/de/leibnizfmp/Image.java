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
    private String directory;

    /**
     * format: String, format of the image
     */
    private String format;

    /**
     * sizeUnit : String, unit of pixel size
     */
    private String sizeUnit;

    /**
     * pxSizeCalib : double, pixel size
     */
    private double pxSizeCalib;

    /**
     * numChannel : int, number of channels
     */
    private int numChannel;

    /**
     * numSeries : int, number of series
     */
    private int seriesID;

    /**
     *  channel1 : string, identity of channel1
     */
    private String channel1;

    /**
     *  channel2 : string, identity of channel2
     */
    private String channel2;

    /**
     *  channel3 : string, identity of channel3
     */
    private String channel3;

    /**
     *  channel4 : string, identity of channel4
     */
    private String channel4;

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

    ImagePlus openImageBF(String inputFile) {

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
    Calibration calibrate(){

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

    /**
     * Image constructor
     *
     * @param inputDir source directory
     * @param imageFormat image format
     * @param pxSize pixel size in micron
     * @param numberChannels number of Channels
     * @param whichSeriesNum the series ID
     * @param ch1 identity of channel 1
     * @param ch2 identity of channel 2
     * @param ch3 identity of channel 3
     * @param ch4 identity of channel 4
     */
    public Image(String inputDir, String imageFormat, double pxSize, int numberChannels,
                 int whichSeriesNum, String ch1, String ch2, String ch3, String ch4){

        directory = inputDir;
        format = imageFormat;
        sizeUnit = "micron";
        pxSizeCalib = pxSize;
        numChannel = numberChannels;
        seriesID = whichSeriesNum;
        channel1 = ch1;
        channel2 = ch2;
        channel3 = ch3;
        channel4 = ch4;

    }



}
