package de.leibnizfmp.maporganelle;

import java.util.ArrayList;

public class FileListProperties {
    public final ArrayList<String> fileList;
    public final boolean multiSeriesSwitch;
    public FileListProperties(ArrayList<String> fileList, boolean multiSeriesSwitch) {

        this.fileList = fileList;
        this.multiSeriesSwitch = multiSeriesSwitch;

    }

}
