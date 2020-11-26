# Introduction

The aim of the OrgaMapper workflow is to measure and map organelle distribution within cells with ease.
The distance of the organelles are related to the nucleus using location or signal intensity.

The image analysis plugin solves 3 main image segmentation tasks:

<strong>1. Nucleus segmentation:</strong> The nucleus is segmented using an intensity threshold.
Nuclei at the edge of the field of view are rejected.
The generated masks are filtered for size and shape.

<strong>2. Cell segmentation:</strong> Cells are segmented using an intensity threshold.
Touching cells are separated using a marker controlled watershed.
The cell ROIs are filtered such that each cell contains 1 nucleus.
Cells are further filered by size and shape.

<strong>3. Organelle detection:</strong> We use a blob detection to detect individual organelles to locate their number and the position within the cell.

The generated masks and ROIs are then used to perform the following measurements:



1. Filtered cell ROIs:
  - Intensity of the organelle channel and an optional measurement channel.
  - Ferets diameter as well as the cell area.

2. Filtered cell ROIs & Nucleus Mask:
  - Compute euclidean distance map (EDM) from edge of nucleus masks.
  - Measure distance of each organelle detection based on EDM.
  - Extract signal value and distance of the organelle channel and an optional measurement channel.

3. Outside of unfiltered cell ROIs:  
  - Background of the organelle channel and an optional measurement channel.

## How to cite

## Accepted datasets

Single multichannel .tiff files and multichannel multiseries files. We tested the workflow on multiseries .nd2 files from Nikon CSU.

For the Data analysis and plotting to work seamlessly with the image analysis we require the following naming pattern.
Single .tif files:<br>
\<Name\>\_\<Treatment\>\_\<Number\>

Multiseries files:<br>
\<Name\>\_\<Treatment\/Wellnumber\>

The data is expected to contain a channel with nucleus staining (DAPI) staining against cytoplasm (CMFA) and against an organelle of choice.

## Installation

For the image analysis you need to download and install Fiji: https://fiji.sc/
The plugin is available via an update site.

You need to add the Cellular-Imaging site to your list of update sites:

1. Select <strong>Help  › Update...</strong> from the menu bar.
2. Click on Manage update sites. Which opens the <strong>Manage update sites</strong> dialog.
3. Press <strong>Add update size</strong> a new line in the Manage update sites dialog appears
4. Add https://sites.imagej.net/Cellular-Imaging/ as url
5. Add an optional name such as Cellular-Imaging
6. Press <strong>Close</strong> and then <strong>Apply changes</strong>

For the data anaylsis you need to install R: https://www.r-project.org/
As an R editor I recommend to use RStudio: https://rstudio.com/products/rstudio/download/

## Hardware requirements

# Workflow execution

# Results and analysis

## Data analysis

# Conclusion
