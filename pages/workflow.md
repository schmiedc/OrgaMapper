---
layout: default
title: Workflow
---

# Fiji Plugin Execution

## Example data

You can find example data here:[https://doi.org/10.5281/zenodo.10932803](https://doi.org/10.5281/zenodo.10932803)

**5_TestData**<br>
├── multi_series<br>
└── single_series<br>

## Start and loading data
1. Start Fiji
2. Open OrgaMapper
    **_Fiji > Plugins > Cellular-Imaging > Map Organelle_**
3. **_Setup dialog_** pops up

<img src="../images/SetupDialog.png" alt="SetupDialog" class="inline"/>

Specify the location of the input, output directories and the file ending of the files to be analyzed. If already available specify the location and name of a settings file  or otherwise leave empty. You can specify files and directory also by drag & drop into the respective field.

*Note:* you can also load external segmentations and/or an external detection into the workflow please have a look at the [External segmentation](external_segmentation.html) tutorial.

Press **_ok_** to continue.


## OrgaMapper Preview

OrgaMapper will search recursively in the specified input directory for files with matching file ending. The OrgaMapper preview window will open. All the available input files will be displayed in the file list (1).

The left section of the preview menu contains tabs with the different processing settings for nuclei and cell segmentation as well as organelle detection. Upon first loading of the workflow without a settings file appropriate default parameters are loaded (2). 

The right section contains all key experimental settings such as pixel size.  Optionally the distance measurement can be also performed from the cell mask edge.Further the identity of each channel need to be specified here (3).

The lower bar contains menu items for saving, loading and resetting the processing settings as well as reseting the preview (4).

<img src="../images/Overview.png" alt="Overview" class="inline"/>

## Save, load, reset settings and reset directories

The dataset settings as well as the segmentation and detection settings can be saved and loaded. The reset *Reset Processing Settings* button will restore the processing setting to the system default. You can also restart the preview using the *Reset Preview* button:

<img src="../images/SaveLoadReset.png" alt="SaveLoadReset" class="inline"/>

The workflow stores the experimental settings in a .xml file. This is a machine readable text file. You can open it with any text editor. The file is stored with the date and time when it has been saved: **_\<Date\>\-\<Time\>\-settings.xml_**. Each setting relevant for the processing is stored with the name of the specific setting.

<img src="../images/settings.png" alt="settings" class="inline"/>

## Specify experimental settings

1. *Pixel size:* the pixel size of the loaded dataset is displayed. Verify if this is correct since segmentation parameters and the units of the measurements are depending on this setting. Modify this value and click the **_override metadata_** button if necessary.

2. *Measure distance from membrane:* the distance measurements (distnace of detections each pixel in the cytoplasm) can also be performed from the membrane by creating an euclidean distance map (EDM) from the cell mask edge. 

3. *Identity of the channels:* each channel is associated with a specific segmentation or measurement task. Select the correct channels for each identity. The Measurement channel is an optional setting. You can leave this on **_select_** or **_ignore_**. If you want to extract intensity measurements from another channel specify any of the other channels.
  - Nucleus channel
  - Cytoplasm channel
  - Organelle channel
  - Measure channel (Optional)

<p align="center">
  <img src="../images/ExperimentalSettings.png" alt="ExperimentalSettings">
</p>

## Preview the segmentation parameters

Select a test image of the loaded image in the file list. If no file has been selected the program will prompt you to do so. In the tabs of the left section you can select the processing tasks you can optimize.

You can preview each segmentation setting on different images. In fact we want to encourage you to do so to find the optimal parameters for the entire dataset.

*TIPP:* the proposed segmentation is displayed as overlay over the image stack like any other Fiji image. So you can use any Fiji tools (zoom, pan, b/c) or can even duplicate or save the image as .tiff stack with the overlay!

In the following sections we will discuss the different processing options for each task.

### Nuclei segmentation
<table>
  <tr>
    <td><img src="../images/preview/nuclei.png" alt="nuclei" ></td>
    <td><img src="../images/preview/Nuc_HeLa_scr_S8-1.png" alt="nuclei_seg"></td>
  </tr>
</table>

1. Segmentation:
  - Median filter size (px) - noise reduction using the median value of the neighboring pixels. The filter size determines the size of the neighborhood.
  - Rolling ball radius (px) - radius of the rolling ball background subtraction.
  - Select threshold - global intensity based thresholding algorithm.
  - Erode (x times) - optional erosion applied to the masked generated by the thresholding.

2. Filter: you can filter the segmented nuclei using minimum and maximum size in square µm. As well as minimum and maximum circularity. This is based on Fijis Particle analyzer tool.

3. Press **_Preview_** to see the outline of the segmentation on top of the nucleus channel. Adjust the brightness contrast of the image using:
**_Image > Adjust > Brightness/Contrast...-** - **_Ctrl + Shift + C_**

### Cell segmentation
<table>
  <td><img src="../images/preview/cells.png" alt="cells"></td>
  <td><img src="../images/preview/Cell_HeLa_scr_S8.png" alt="cell_seg" width="400"></td>
</table>

1. Segmentation:
  - Invert Cell Image - for a membrane staining the image can be inverted to achieve a segmentation. Tutorial: [Segmentation of membrane signal](seg_membrane_signal.html). 
  - Median filter size (px) - noise reduction using the median value of the neighboring pixels. The filter size determines the size of the neighborhood.
  - Rolling ball radius (px) - radius of the rolling ball background subtraction.
  - Global Threshold (A.U.) - global intensity based threshold.

2. Watershed settings:
  - Gaussian sigma (px) - a large gaussian filter is applied to smooth out irregularities in the image and detect single cells.
  - Prominence (A.U.) - detection of single cells to split touching cells.

3. Filter: tick this box to toggle the visualization cell segmentations with a single nucleus. This only affects the visualization. In the final processing the cells will always be filtered for cells that have a single nucleus. Cells without or with multiple nuclei present in the cell segmentation will always be filtered!

4. Press **_Preview_** to see the outline of the segmentation on top of the cytoplasm channel. Adjust the brightness contrast of the image using:
**_Image > Adjust > Brightness/Contrast..._** - **_Ctrl + Shift + C_**

### Organelle detection
<table>
  <td><img src="../images/preview/organelles.png" alt="organelle" align="left"></td>
  <td><img src="../images/preview/Orga_HeLa_scr_S8-1.png" alt="organelle_detection"></td>
</table>

1. Detect number & position of spots:
  - LoG sigma (px) - a laplacian of gaussian filter is applied to enhance blob like structures.
  - Prominence (A.U.) - detection of peaks in the filtered image.

2. Filter in nucleus: tick this box to toggle the visualization of detections in the nucleus area. This only affects the visualization. In the final processing the detections in the nucleus segmentation are always filtered.

3. Press **_Preview_** to see the outline of the segmentation on top of the organelle channel.
Adjust the brightness contrast of the image using:
**_Image > Adjust > Brightness/Contrast..._** - **_Ctrl + Shift + C_**


## Batch processing

Once you are happy with the segmentation parameters press Batch Process. The result of the workflow will be saved in the specified output directory along the used settings.

<p align="center">
  <img src="../images/BatchProcess.png" alt="BatchProcessing">
</p>

The progress of the processing will be written in the Log file. Once finished the Log file will display:

<p align="center">
  <img src="../images/Finished.png" alt="Finished">
</p>

## Results

For a documentation of the image analysis results have a look at the [Fiji Plugin Results](results.html).

The results can be processed using an RShiny workflow: [Shiny App Execution](rShinyApp.html).
