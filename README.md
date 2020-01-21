[![TravisCI](https://travis-ci.com/SJCross/MIA.svg?branch=master)](https://travis-ci.com/SJCross/MIA)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1201372.svg)](https://doi.org/10.5281/zenodo.1201320)
[![JitPack](https://jitpack.io/v/SJCross/ModularImageAnalysis.svg)](https://jitpack.io/#SJCross/ModularImageAnalysis/)

[![Wolfson Bioimaging](./src/main/resources/Images/Logo_text_UoB_128.png)](http://www.bristol.ac.uk/wolfson-bioimaging/)

About MIA
---------
MIA is a Fiji plugin which provides a modular framework for assembling image and object analysis pipelines.  Detected objects can be transformed, filtered, measured and related.  Analysis pipelines are batch-enabled by default, allowing easy processing of high-content datasets.

MIA is developed in the [Wolfson Bioimaging Facility](http://www.bristol.ac.uk/wolfson-bioimaging/) at the University of Bristol.

Version: ${version}

Installation
------------
- The latest version of the plugin can be downloaded from the [Releases](https://github.com/SJCross/ModularImageAnalysis/releases) page.
- Place this .jar file into the /plugins directory of the your Fiji installation.
- If the "Biomedgroup" and "IJPB-plugins" plugins aren't already installed, you'll be prompted to download these via an automated installer.

Getting started
-----
- In Fiji, run the plugin from Plugins > Bristol WBIF > Modular Image Analysis
- To run an existing analysis workflow
> - Click "Load" and select the .mia workflow file
> - Depending on workflow configuration, a number of controls may be visible
> - When ready, click "Run" to start the analysis
> - While processing the status panel will display the number of files completed (batch mode) or a summary of the ongoing task (single file mode)
> - When finished, "Complete" will be displayed in the status panel.  Output XLS files will be saved in the input directory.
- To create a new workflow
> - Select View > "Switch to editing view" from the menu bar
> - Modules are added and removed from the workflow using the "+" and "-" buttons
> - Module order can be shifted with the arrow buttons
> - Selecting a module will display its relevant parameters in the right panel
> - Checkboxes to the right of each module determine if they are visible in the basic view (default view)
> - Input files and folders are specified using the "Input control" and Excel file export is configured in the "Output control"

Note
----
This plugin is still in development and test coverage is currently incomplete.  Please keep an eye on results and add an [issue](https://github.com/SJCross/ModularImageAnalysis/issues) if any problems are encountered.

Acknowledgements
----------------
The plugin makes use of a combination of plugins packaged with Fiji as well as others that can be installed via the updater.

Required plugins pre-packaged with Fiji:
- [AnalyzeSkeleton](https://github.com/fiji/AnalyzeSkeleton)
- [Auto Threshold](https://github.com/fiji/Auto_Threshold)
- [bUnwarpJ](https://github.com/fiji/bUnwarpJ)
- [BioFormats](https://github.com/openmicroscopy/bioformats)
- [Colour Deconvolution](https://github.com/fiji/Colour_Deconvolution)
- [Correct Bleach](https://github.com/fiji/CorrectBleach)
- [MPICBG](https://github.com/axtimwalde/mpicbg)
- [TrackMate](https://github.com/fiji/TrackMate)
- [Weka Trainable Segmentation](https://github.com/fiji/Trainable_Segmentation)

Required plugins that need installing via the Fiji updater:
- [Ridge Detection](https://github.com/thorstenwagner/ij-ridgedetection)
- [MorphoLibJ](https://github.com/ijpb/MorphoLibJ)

Plugins bundled with MIA:
- [BoneJ legacy plugins](https://github.com/mdoube/BoneJ)
- [Stack Focuser](https://imagej.nih.gov/ij/plugins/stack-focuser.html)

A list of bundled dependencies along with their respective licenses can be found [here](https://htmlpreview.github.io/?https://github.com/SJCross/MIA/blob/master/target/site/dependencies.html).
