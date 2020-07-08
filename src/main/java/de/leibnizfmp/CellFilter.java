package de.leibnizfmp;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.ParticleAnalyzer;
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

        return individualCellsFinal;

    }

    void filterByNuclei() {

    }


}
