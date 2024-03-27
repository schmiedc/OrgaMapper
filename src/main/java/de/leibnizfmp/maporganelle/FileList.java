package de.leibnizfmp.maporganelle;

import ij.IJ;
import loci.formats.FormatException;
import loci.formats.ImageReader;

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

    private String suffix;

    /**
     * walks through input directory
     * filters file for file suffix and writes string into an array
     *
     * @param inputDir input directory
     * @return list containing file names as string for processing
     */
    @Deprecated
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
        assert results != null;

        return new ArrayList<>(results);

    }

    /**
     * walks through input directory
     * filters file for file suffix and writes string into an array
     *
     * @param inputDir input directory
     * @return list containing file names as string for processing
     */
    FileListProperties getFileMultiSeriesList(String inputDir) {

        List<String> fileList = null;
        boolean multiSeries = false;

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

        for (String testFile : fileList) {

            IJ.log("Found file: " + testFile);

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

            int seriesCount = reader.getSeriesCount();

            if (seriesCount > 1) {

                IJ.log("File is a multi series file");
                IJ.log(seriesCount + " images for " + testFile);
                multiSeries = true;

                for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {

                    String nameWithoutExt = testFile.substring(0, testFile.lastIndexOf("."));

                    String seriesName = nameWithoutExt + "_S" + seriesIndex;
                    fileNameList.add(seriesName);

                }

            } else if (seriesCount == 1) {

                IJ.log("File is a multi series file");
                IJ.log(seriesCount + " images for " + testFile);
                multiSeries = false;

                String nameWithoutExt = testFile.substring(0, testFile.lastIndexOf("."));

                String seriesName = nameWithoutExt + "_S0";
                fileNameList.add(seriesName);

            } else {

                IJ.error("File reader error");

            }

        }

        return new FileListProperties(new ArrayList<>(fileNameList), multiSeries);

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
