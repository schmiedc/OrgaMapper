package de.leibnizfmp;

import ij.IJ;
import ij.io.FileSaver;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileList {

    String suffix;

    /**
     * walks through input directory
     * filters file for file suffix and writes string into an array
     *
     * @param inputDir input directory
     * @return list containing file names as string for processing
     */
    ArrayList<String> getFileList(String inputDir) {

        List<String> results = null;
        Path inputPath = Paths.get(inputDir);

        // opens a stream and walks through file tree of given path
        try(Stream<Path> walk = Files.walk(inputPath)) {

            // gets the filenames converts them to a string
            results = walk.map(x -> inputPath.relativize(x).toString())
                    // filters them for the suffix
                    .filter(f -> f.endsWith(suffix))
                    // puts results to a List
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: files not found");
            IJ.error("Error: no valid files found!");
        }

        // casts list to arrayList
        ArrayList<String> filesToProcess = new ArrayList<>(results);

        return filesToProcess;

    }

    /**
     * walks through input directory
     * filters file for file suffix and writes string into an array
     *
     * @param inputDir input directory
     * @return list containing file names as string for processing
     */
    ArrayList<String> getFileMultiSeriesList(String inputDir) {

        List<String> fileList = null;

        Path inputPath = Paths.get(inputDir);

        // opens a stream and walks through file tree of given path
        try(Stream<Path> walk = Files.walk(inputPath)) {

            // gets the filenames converts them to a string
            fileList = walk.map(x -> inputPath.relativize(x).toString())
                    // filters them for the suffix
                    .filter(f -> f.endsWith(suffix))
                    // puts results to a List
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: files not found");
            IJ.error("Error: no valid files found!");
        }

        assert fileList != null;
        List<String> fileNameList = new ArrayList<>();

        for (int series = 0; series < fileList.size(); series++) {

            String testFile = fileList.get(series);

            ImageReader reader = new ImageReader();
            try {

                IJ.log("Try opening with BF");
                reader.setId(inputDir + File.separator + testFile);

            } catch (FormatException e) {

                IJ.error("Bio-formats format exception: " + e.getMessage());
                e.printStackTrace();

            } catch (IOException e) {

                IJ.error("Bio-formats cannot open file: " + e.getMessage());
                e.printStackTrace();

            }

            int seriesNumber = reader.getSeriesCount();
            IJ.log(seriesNumber + " image(s) for " + testFile);

            for (int seriesIndex = 0; seriesIndex < seriesNumber; seriesIndex++) {

                String nameWithoutExt = testFile.substring(0, testFile.lastIndexOf("."));

                String seriesName = nameWithoutExt + "_S" + seriesIndex;
                IJ.log(seriesName);
                fileNameList.add(seriesName);
            }

        }

        // casts list to arrayList
        assert fileNameList != null;

        ArrayList<String> filesToProcess = new ArrayList<>(fileNameList);

        return filesToProcess;

    }

    /**
     * loops through list1 and compares if items are in list2
     *
     * @param list1 first file list
     * @param list2 second file list
     * @return intersection
     */
    ArrayList<String> intersection(ArrayList<String> list1, ArrayList<String> list2) {

        ArrayList<String> list = new ArrayList<>();

        for (String item : list1) {

            if(list2.contains(item)) {

                list.add(item);

            }
        }

        return list;
    }

    FileList (String fileEnding) {

        suffix = fileEnding;

    }
}
