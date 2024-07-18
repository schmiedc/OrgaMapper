#@ File (label = "Ground truth directory", style = "directory") groundTruthDir
#@ String (label = "Ground truth suffix", value = "_mask.tif") gt_suffix
#@ File (label = "Test directory", style = "directory") input
#@ String (label = "Test suffix", value = "_orgaMapper.tif") input_suffix
#@ int (label = "Minimum distance", value=4, min=0, max=20, style="slider") minimumDistance

// ============================================================================
/*
 * Distance Metric
 *
 *  DESCRIPTION: Computes a the number of TP, FP, FN, Ambigious detections
 *  			 Computes an F1 score
 *  			 
 *  			 Input are two 8-bit masks with each pixel of value 255 
 *  			 representing a detection
 *  			 
 *  			 There is a mask for the groundtruth 
 *  			 There is a mask for the test image 
 *  			 The largest organelle size needs to be defined in pixels
 *  			 The test image is compared against the ground truth
 *
 *       AUTHOR: Christopher Schmied
 *
 *      CONTACT: schmied@fmp-berlin.de
 *
 *    INSTITUTE: Leibniz-Forschungsinstitut f r Molekulare Pharmakologie (FMP)
 *    			 Cellular Imaging - Core facility
 *    			 Campus Berlin-Buch
 *               Robert-Roessle-Str. 10
 *               13125 Berlin, Germany
 *
 * DEPENDENCIES: 
 *         BUGS:
 *        NOTES:
 *	
 *		Version: 1.0.0
 *      CREATED: 2024-07-01
 *     REVISION: 2024-07-15
 */
// ============================================================================

// Compute euclidean distance between two points
function measureDistance(image1X, image1Y, image2X, image2Y) {
	
	// returns distance in pixels
    return sqrt( pow( image2X - image1X, 2 ) + pow( image2Y - image1Y, 2 ) );
    
}

function calculateF1(truePositive, falsePositive, falseNegative) {
	
	return( ( truePositive ) / ( truePositive + 0.5 * ( falseNegative + falsePositive ) ) )
	
}

print("Ground truth folder " + groundTruthDir);
print("Testing folder " + input);
print("Saving to " + input);
processFolder(groundTruthDir);

function processFolder(groundTruthDir) {

	list = getFileList(groundTruthDir);
	
	for (i = 0; i < list.length; i++) {
		
		if(File.isDirectory(groundTruthDir + File.separator + list[i]))
		
			processFolder(groundTruthDir + File.separator + list[i]);
			
		if(endsWith(list[i], gt_suffix))
		
			processFile(groundTruthDir, gt_suffix, input, input_suffix, minimumDistance, list[i], i);
			
	}
}

function processFile(groundTruthDir, gt_suffix, input, input_suffix, minimumDistance, file, resultsIndex) {
	
	setBatchMode(true);
	
	groundTruthName = file;
	baseFileName = replace(file, gt_suffix, "");
	testImageName = baseFileName + input_suffix;
	
	print("Processing " + baseFileName);
	
	// =============================================================
	// Ground truth
	// =============================================================
	open(groundTruthDir + File.separator + groundTruthName);

	selectImage(groundTruthName);
	
	noiseTolerance = 1;
	run("Find Maxima...", "noise=" + noiseTolerance + " output=[Point Selection]");
	getSelectionCoordinates(image1XList, image1YList);
	run("Select None");
	image1NumberDetection = image1XList.length;
	
	print("Number of detections - manual: " + image1NumberDetection);
	
	// =============================================================
	// INPUT Comparison
	// =============================================================
	
	open(input + File.separator + testImageName);

	selectImage(testImageName);
	
	noiseTolerance = 1;
	run("Find Maxima...", "noise=" + noiseTolerance + " output=[Point Selection]");
	getSelectionCoordinates(image2XList, image2YList);
	run("Select None");
	image2NumberDetection = image2XList.length;
	
	print("Number of detections - auto: " + image2NumberDetection);

	// =============================================================
	// INPUT Comparison
	// =============================================================
	
	// creates new image to save distance matrix
	newImage("distanceMatrix", "8-bit black", image1NumberDetection, image2NumberDetection, 1);
	
	for (indexImage1 = 0; indexImage1 < image1NumberDetection; indexImage1++) {
		
	    image1X = image1XList[indexImage1];
	    image1Y = image1YList[indexImage1];
	    
	    for (indexImage2 = 0; indexImage2 < image2NumberDetection; indexImage2++) {
	    	
	    	 image2X = image2XList[indexImage2];
	    	 image2Y = image2YList[indexImage2];
	    	 
	    	 distanceValue = measureDistance(image1X, image1Y, image2X, image2Y);
	    	 
	    	 selectImage("distanceMatrix");
	    	 setPixel(indexImage1, indexImage2, distanceValue);
	    	
	    }
	    
	}
	
	// =============================================================
	// Threshold distance matrix 
	// =============================================================
	print("Minimum distance set to: " + minimumDistance);
	
	selectImage("distanceMatrix");
	setThreshold(0, minimumDistance);
	setOption("BlackBackground", true);
	run("Convert to Mask");
	
	setBatchMode("exit and display");
	
	// =============================================================
	// Compute true positive (TN), false negative (FN) and ambiguous 
	// =============================================================
	
	truePositive = 0;
	falseNegative = 0;
	ambiguous = 0;
	
	for (indexGroundTruth = 0; indexGroundTruth  < image1NumberDetection; indexGroundTruth ++) {
		
		countAboveThreshold = 0;
		
		for (indexDetection = 0; indexDetection < image2NumberDetection; indexDetection++) {
			
			result = getValue(indexGroundTruth, indexDetection);
			
			if ( result == 255 ) {
				
				countAboveThreshold = countAboveThreshold + 1;
				
			} 
			
		}
		
		if ( countAboveThreshold == 1) {
			
			truePositive = truePositive + 1;
			
		} else if ( countAboveThreshold == 0 ) {
			
			falseNegative = falseNegative + 1;
			
		} else if ( countAboveThreshold > 1 ) {
			
			ambiguous = ambiguous + 1;
			
		}
		
	}
	
	// =============================================================
	// Compute false positive
	// =============================================================
	
	falsePositive = 0;
	
	for (indexDetection = 0; indexDetection < image2NumberDetection; indexDetection++) {
		
		countAboveThreshold = 0;
		
		for (indexGroundTruth = 0; indexGroundTruth  < image1NumberDetection; indexGroundTruth ++) {
			
			result = getValue(indexGroundTruth, indexDetection);
			
			if ( result == 255 ) {
				
				countAboveThreshold = countAboveThreshold + 1;
				
			} 
			
		}
		
		if ( countAboveThreshold == 0 ) {
			
			falsePositive = falsePositive + 1;
			
		}
		
	}
	
	// =============================================================
	// Output
	// =============================================================
	// TODO: Save into common result table
	
	f1Score = calculateF1(truePositive, falsePositive, falseNegative);
	
	print("TP: " + truePositive);
	print("FP: " + falsePositive);
	print("FN: " + falseNegative);
	print("Ambiguous: " + ambiguous);
	print("F1: " + f1Score );
	
	setResult("Name", resultsIndex, baseFileName);
	setResult("Count_manual", resultsIndex,  image1NumberDetection);
	setResult("Count_test", resultsIndex,  image2NumberDetection);
	setResult("TP", resultsIndex, truePositive);
	setResult("FP", resultsIndex, falsePositive);
	setResult("FN", resultsIndex, falseNegative);
	setResult("Ambiguous", resultsIndex, ambiguous);
	setResult("F1", resultsIndex, f1Score);
	
	close("*");
	run("Collect Garbage");	

}

print("Macro finished");

getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
datum = "" + year + "-" + IJ.pad(month, 2) + "-" + IJ.pad(dayOfMonth, 2) + "";

selectWindow("Log");
saveAs("Text", input + File.separator + "Log_" + datum + ".txt");

selectWindow("Results");
saveAs("Text", input + File.separator + "Results_" + datum + ".txt");

close("Results");
