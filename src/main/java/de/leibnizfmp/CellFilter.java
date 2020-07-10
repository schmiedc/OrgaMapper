package de.leibnizfmp;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

/**
 * filter cells based on size and circularity
 *
 * @author christopher Schmied
 * @version 1.0.0
 */

public class CellFilter {

    static ImagePlus filterByCellSize(ImagePlus backgroundMask, ImagePlus separatedCells,
                               double minCellSize, double maxCellSize, double lowCirc, double highCirc) {

        Calibration calibration = backgroundMask.getCalibration();
        Double pxSizeFromImage = calibration.pixelWidth;
        int minSizePx = Image.calculateSizePx( pxSizeFromImage, minCellSize);
        int maxSizePx = Image.calculateSizePx( pxSizeFromImage, maxCellSize);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus individualCells = calculator.run("Multiply 32-bit", backgroundMask, separatedCells);
        ImageProcessor cellMask = individualCells.getProcessor().convertToByteProcessor();


        IJ.log("Filter cells by size: " + minCellSize + "-" + maxCellSize + " µm²");
        IJ.log("Filter cells by circ.: " + lowCirc + "-" + highCirc);
        // show masks = 4096
        ParticleAnalyzer analyzer = new ParticleAnalyzer(4096,0,null,
                minSizePx, maxSizePx, lowCirc, highCirc );

        ImagePlus mask = new ImagePlus("cellMask", cellMask);
        analyzer.analyze( mask );
        ImagePlus filteredMask = analyzer.getOutputImage();
        filteredMask.hide();

        ImageProcessor filteredMaskProcessor = filteredMask.getProcessor();
        filteredMaskProcessor.invertLut();

        ImagePlus individualCellsFinal = new ImagePlus ("individualCells", filteredMaskProcessor );

        individualCellsFinal.setCalibration( calibration );
        IJ.log("Cell Filter by size and circ. done");

        return individualCellsFinal;

    }

    static RoiManager filterByNuclei(ImagePlus cellMask, ImagePlus nucleiMask ) {

        IJ.log("Filtering cells by nuclei number");
        ParticleAnalyzer cellAnalyzer = new ParticleAnalyzer(2048, 0, null,
                0, Image.calculateMaxArea( cellMask.getWidth(), cellMask.getHeight() ) );

        RoiManager cellRoiManager = new RoiManager(false);
        ParticleAnalyzer.setRoiManager( cellRoiManager );

        cellAnalyzer.analyze( cellMask );

        IJ.log("Found " + cellRoiManager.getCount() + " cell(s) before filtering");

        for (int cellIndex = cellRoiManager.getCount(); cellIndex > 0; cellIndex--  ) {

            int roiIndex = cellIndex - 1;

            ParticleAnalyzer nucAnalyzer = new ParticleAnalyzer( 2048, 0, null,
                    0, Image.calculateMaxArea( cellMask.getWidth(), cellMask.getHeight() ) );

            RoiManager nucRoiManager = new RoiManager(false);
            ParticleAnalyzer.setRoiManager( nucRoiManager );

            cellRoiManager.select(nucleiMask, roiIndex);
            nucAnalyzer.analyze( nucleiMask );

            if ( nucRoiManager.getCount() == 1) {

                IJ.log("Cell " + cellIndex + ": found 1 nucleus, keep");

            } else {

                IJ.log("Cell " + cellIndex + ": found " + nucRoiManager.getCount() + " nuclei, delete");
                cellRoiManager.getRoi( roiIndex );
                cellRoiManager.runCommand( "Delete" );

            }

            nucRoiManager.reset();

        }

        IJ.log("Cell Filter by nuclei number done");

        return cellRoiManager;

    }

}
