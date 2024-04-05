---
layout: default
title: segmentationMembraneSignal
---

# Segmentation of membrane signal

## Example data

<!---
Link to example input data
-->

### Example data structure

Structure of data and folders:
Input<br>
├── MembraneStaining.tif<br>
└── Setting_MembraneStaining.xml<br>

Input_External_Segmentation<br>
└── MembraneStaining_NucSeg.tif<br>

Output<br>
├── MembraneStaining_S0<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── cellSegmentation.png<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── detections.tiff<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── intensityDistance.csv<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└──  nucSegmentation.png<br>
├── 2024-04-03T120353-settings.xml<br>
├── cellMeasurements.csv<br>
└── organelleDistance.csv<br>

### Example input images

The test data for this tutorial contains a nucleus stain as well as a membrane staining. To simulate and organelle channel for the organelle detection the membrane stainig has been duplicated.

<table>
  <tr>
    <td><img src="../images/seg_membrane_signal/nucleus.png" alt="nuclei" ></td>
    <td><img src="../images/seg_membrane_signal/membrane.png" alt="membrane"></td>
  </tr>
</table>

The segmentation of the nuclei via the internal intensity based segmentation was difficult. Therefore an external segmentation is provided. This segmentation has been generated by manually curating an segmentation generated via [Labkit](https://imagej.net/plugins/labkit/). 

Steps to generate this segmentation:
1. Pixel classification using Labkit.
2. Saved segmentation result as .tif.
3. Convert to ImageJ/Fiji mask (Threshold; 8-bit; Background: 0, Foreground: 255).
4. Binary operation: close holes and erode.
5. Load adjusted segmentation as labelling in Labkit.
6. Manual curation of segmentation using Labkit.
7. Nuclei at the edges of the field of view excluded. 
8. Saved labelling as .tif
9. Convert to ImageJ/Fiji mask (Threshold; 8-bit; Background: 0, Foreground: 255).

<img src="../images/seg_membrane_signal/NucSeg.png" alt="NucSeg" class="inline"/>

## Load example data

1. Start Fiji
2. Open OrgaMapper
    **_Fiji > Plugins > Cellular-Imaging > Map Organelle_**
3. **_Setup dialog_** pops up

Specify the location of the input and the output directories. The file ending is .tif. Select the provided settings file and tick the external nucleus segmentation as external data input: 

<img src="../images/seg_membrane_signal/Invert_Setup.png" alt="SetupDialog" class="inline"/>

Press **_ok_** to continue.

The external data setup dialog pops up. Specify the input directory for the external nucleus segmentation. The Nucelus segmentation suffix should work as is:

<img src="../images/seg_membrane_signal/Invert_Setup2.png" alt="SetupDialog" class="inline"/>

Press **_ok_** to continue.

## Processing example data

The Preview based on the provided data and settings file should pop up:

<img src="../images/seg_membrane_signal/Invert_preview.png" alt="SetupDialog" class="inline"/>

The provided settings should work as is. You can check the previews for external nucleus segmentation:

<img src="../images/seg_membrane_signal/Invert_Nuc_Preview.png" alt="SetupDialog" class="inline"/>

As well as the preview for the cell segmentation. *Note:* that the **Invert Cell Image** setting is selected:

<img src="../images/seg_membrane_signal/Invert_Cell_Preview.png" alt="SetupDialog" class="inline"/>


You can the execute the batch processing via pressing **Batch Process**.

<p align="center">
  <img src="../images/BatchProcess.png" alt="BatchProcessing">
</p>