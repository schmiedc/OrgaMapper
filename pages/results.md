# Results of Fiji plugin

## Result images

The results of the workflow are structured the following way:

OutputFolder<br>
├── \<Date\>\-\<Time\>\-settings.xml<br>
├── cellMeasurements.csv<br>
├── organelleDistance.csv<br>
└── \<imageName\><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── **cellSegmentation.png**<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── **detections.tiff**<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── intDistance.csv<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── **nucSegmentation.png**<br>

For each image a folder will be created containing visualizations for the segmentation task:

1. **cellSegmentation.png** file contains the cytoplasm channel overlaid with the nuclei segmentation and an outline of the cell segmentation.
2. **detections.tiff** file contains the organelle channel with overlays from the nuclei and cell segmentation as well as the organelle detection.
3. **nucSegmtnation.png** contains the nucleus channel with an outline of the nucleus segmentation.

<p align="center">
  <img src="images/results_annotated.png" alt="ResultImages">
<p>

## Result tables

The resulting measurements will be saved in .csv tables either collected or per individual image in case of the intensity profiles:

OutputFolder<br>
├── **\<Date\>\-\<Time\>\-settings.xml**<br>
├── **cellMeasurements.csv**<br>
├── **organelleDistance.csv**<br>
└── \<imageName\><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── cellSegmentation.png<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── Detections.tiff<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── **intDistance.csv**<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── nucSegmentation.png<br>

1. **.xml settings**: will be created containing the processing settings used.
2. **cellMeasurements.csv** file contains the measurements of size, number of detections and intensity.
3. **organelleDistance.csv** contains the distance and peak intensity values for each detected organelle will be saved in the file.
4. **intDistance.csv** file contains the measurements of the intensity values of each pixel in the cytoplasm segmentation and their distance from the nucleus.
