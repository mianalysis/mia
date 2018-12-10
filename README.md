ModularImageAnalysis
====================
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1201372.svg)](https://doi.org/10.5281/zenodo.1201320)
[![JitPack](https://jitpack.io/v/SJCross/ModularImageAnalysis.svg)](https://jitpack.io/v/SJCross/ModularImageAnalysis.svg)

This ImageJ/Fiji plugin provides a modular framework for assembling image and object analysis pipelines.  Detected objects can be transformed, filtered, measured and related.


Installation
------------
- The latest version of the plugin can be downloaded from the [Releases](https://github.com/SJCross/ModularImageAnalysis/releases) page.
- Place this .jar file into the /plugins directory of the your Fiji installation.
- It is necessary to install a couple of other Fiji plugins using the [updater](http://imagej.net/Updater):
> - In Fiji, go to Help > Update...
> - Go to "Manage update sites"
> - Ensure "Biomedgroup" and "IJPB-plugins" are selected, then click "Close"
> - Select "Apply changes" and restart Fiji when promted

Note: "Biomedgroup" enables the [Ridge Detection](https://github.com/thorstenwagner/ij-ridgedetection) plugin and "IJPB-plugins" enables [MorphoLibJ](https://github.com/ijpb/MorphoLibJ).

Usage
-----
- In Fiji, run the plugin from Plugins > Bristol WBIF > Modular Image Analysis
- To run an existing analysis workflow
> - Click "Load" and select the .mia workflow file
> - Depending on workflow configuration, a number of controls may be visible
> - When ready, click "Run" to start the analysis
> - While processing the status panel will display the number of files completed (batch mode) or a summary of the ongoing task (single file mode)
> - When finished, "Complete" will be displayed in the status panel.  Output XLSX files will be saved in the input directory.
- To create a new workflow
> - Select View > Editing mode from the menu bar
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


A list of bundled dependencies along with their respective licenses can be found [here](https://cdn.staticaly.com/gh/SJCross/ModularImageAnalysis/908a49be/target/site/dependencies.html).
