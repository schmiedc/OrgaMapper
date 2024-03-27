package de.leibnizfmp.maporganelle;

import java.util.ArrayList;

public class FileListProperties {
    public final ArrayList<String> fileList;
    public final boolean multiSeries;
    public FileListProperties(ArrayList<String> fileList, boolean multiSeries) {

        this.fileList = fileList;
        this.multiSeries = multiSeries;

    }

}
