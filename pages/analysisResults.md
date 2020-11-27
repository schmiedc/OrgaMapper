---
layout: default
title: analysisResults
---

# Analysis Results

The resulting plots will be saved individually in the specified input directory under plot_distance_map and plot_intensity_map. The processed data will be saved as raw and summarized data in .xlsx files:

InputFolder<br>
├── \<imageName\><br>
├── **plot\_distance\_map**<br>
├── **plot\_intensity\_map**<br>
├── \<Date\>\-\<Time\>\-settings.xml<br>
├── **\<Result name\>\_cell.xlsx**<br>
├── **\<Result name\>\_detection.xlsx**<br>
├── **\<Result name\>\_intensityProfile.xlsx**<br>
├── **\<Result name\>\_intensityRatio.xlsx**<br>
├── cellMeasurements.csv<br>
└── organelleDistance.csv<br>

## Result Files

- **\<Result name\>\_cell.xlsx**:<br>
Contains the summarized distance data per cell.

- **\<Result name\>\_detection.xlsx**: <br>
Contains the collected distance data for each individual detection for each cell.

- **\<Result name\>\_intensityProfile.xlsx**: <br>
Contains the collected intensity profile for each individual cell.

- **\<Result name\>\_intensityRatio.xlsx**:<br>
Contains the data for computing the intensity ratio.

## Overview Plots

Within the rShiny app you will then get overview plots under the tabs at the top for the different parameters.

<p align="center">
  <img src="../images/analysis/Tabs.png" alt="ResultImages">
</p>

First, under **Cell Measurements** you will be informed about the ferret’s diameter, area, number of detections and average intensity in the cytoplasm of the organelle channel:

<p align="center">
  <img src="../images/analysis/CellMeasurements.png" alt="ResultImages">
</p>

Second,you will find the organelle specific measurements under **Organelle Measurements**:

<p align="center">
  <img src="../images/analysis/OrganelleMeas.png" alt="ResultImages">
</p>

Finally, Organelle Intensity Map and optionally for another measurement channel you will find the intensity maps:

<p align="center">
  <img src="../images/analysis/Intensity.png" alt="ResultImages">
</p>
