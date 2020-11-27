---
layout: default
title: rShinyApp
---

# rShiny App Execution
The data analysis is realized as a rShiny app. This allows you to read in the output data and execute the data processing using a graphical interface.

Install r and r studio for running the rShiny app:<br/>
[https://www.r-project.org/](https://www.r-project.org/)<br/>
[https://rstudio.com/products/rstudio/download/](https://rstudio.com/products/rstudio/download/)<br/>

Then download the contents of the OrgaMapper_Rshiny repository:
[OrgaMapper Rshiny](https://github.com/schmiedc/OrgaMapper_Rshiny/archive/main.zip)

Here is a link to the OrgaMapper Rshiny repository:
[https://github.com/schmiedc/OrgaMapper_Rshiny](https://github.com/schmiedc/OrgaMapper_Rshiny)


## rShiny app execution

Open the app.R file in RStudio. Then specify the location of folder that contains the scripts in the setwd() line:

<p align="center">
  <img src="../images/analysis/setWD.png" alt="setWD" class="inline"/>
</p>

To open the rShiny app press **_Run App_** in the top left corner of the RStudio editor:

<p align="center">
  <img src="../images/analysis/RunApp.png" alt="RunApp" class="inline"/>
</p>

The OrgaMapper data analysis interface will appear:

<p align="center">
  <img src="../images/analysis/rShinyInterface2.png" alt="RunApp" class="inline"/>
</p>

Specify the output folder of the Fiji plugin as input folder of the data analysis using Folder select.

<p align="center">
  <img src="../images/analysis/Folder.png" alt="Folder" class="inline"/>
</p>

## General settings:

Then specify which data set type your original image data was. If you processed multiseries .nd2 files or multiseries files of any different image data format then specify Multi series. If you processed single series file, like single series .tiff or other image formats then specify Single series (1).

To extract the series number properly you might need to adjust the regular expression setting for single series data under Single series number regex (2)

Then specify a name that the resulting .xlsx files should have (3).

Further you can specify an optional Feret’s diameter filter which you can also toggle on/off. You can also turn on and off the automatic background subtraction for the plots. Finally, computing the intensity maps is relatively time consuming, thus if you only require the distance maps you can turn it off here as well (4).

<p align="center">
  <img src="../images/analysis/GeneralHints.png" alt="Folder" class="inline"/>
</p>

## Map Distance

For the distance measurements you can specify the range of the raw as well as the normalized distance plot here:

<p align="center">
  <img src="../images/analysis/MapDistance.png" alt="Folder" class="inline"/>
</p>


## Map Intensity

For the intensity measurements you can specify the bin width, range of the raw as well as the normalized intensity plot:

<p align="center">
  <img src="../images/analysis/MapIntensity.png" alt="Folder" class="inline"/>
</p>

## Process & Plot

When done with the setup you can start the data processing and plotting by pressing Plot Data.

<p align="center">
  <img src="../images/analysis/Plot.png" alt="Folder" class="inline"/>
</p>

A progress bar in the lower right corner of the App will appear.

<p align="center">
  <img src="../images/analysis/Progress.png" alt="Folder" class="inline"/>
</p>
