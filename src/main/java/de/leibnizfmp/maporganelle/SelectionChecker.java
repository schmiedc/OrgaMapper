package de.leibnizfmp.maporganelle;

import ij.IJ;
import ij.WindowManager;
import java.util.ArrayList;
import java.util.Arrays;

public class SelectionChecker {

    static boolean checkSelectedFile(String selectedFile, String fileFormat, ArrayList<String> fileList) {

        IJ.log("Selected File: " + selectedFile);

        // check if there are windows open already
        int openImages = WindowManager.getImageCount();

        boolean selectedFileChecker;

        // if there are image windows open check if they are of the list and of the selected image
        if  ( openImages != 0 ) {

            IJ.log("There are images open!");

            String[] openImage = WindowManager.getImageTitles();
            ArrayList<String> openImageList = new ArrayList<>(Arrays.asList(openImage));

            FileList fileUtility = new FileList(fileFormat);
            ArrayList<String> openInputImages = fileUtility.intersection(openImageList, fileList);

            selectedFileChecker = false;

            for (String image : openInputImages) {

                if (image.equals(selectedFile)) {

                    IJ.log(selectedFile + " is already open");
                    selectedFileChecker = true;
                    break;

                } else {

                    IJ.selectWindow(image);
                    IJ.run("Close");
                    selectedFileChecker = false;

                }

            }

        } else {

            IJ.log("There are no images open!");
            selectedFileChecker = false;

        }

        return selectedFileChecker;

    }
}
