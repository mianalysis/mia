MIA: Modular Image Analysis
=========================
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1201372.svg)](https://doi.org/10.5281/zenodo.1201320)

This ImageJ/Fiji plugin provides a modular framework for assembling image and object analysis pipelines.  Detected objects can be transformed, filtered, measured and related.  Development of this plugin is ongoing.


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
- In Fiji, run the plugin from Plugins > Wolfson Bioimaging > Modular Image Analysis
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


Troubleshooting
---------------
- Unsupported major.minor version
> - Cause: MIA requires Java 8; however, older vesions of Fiji were packaged with Java 6.
> - Solution 1: Run the Fiji updater from Help > Update...
> - Solution 2: Download the latest version of  Fiji from [fiji.sc](http://fiji.sc).
> - Validation: In Fiji, go to Help > About ImageJ and verify Java is version 1.8.0 (or newer).


Acknowledgements
----------------
The plugin makes use of a combination of plugins packaged with Fiji as well as others that can be installed via the updater.

Required plugins pre-packaged with Fiji:
- [Auto Threshold](https://github.com/fiji/Auto_Threshold)
- [BioFormats](https://github.com/openmicroscopy/bioformats)
- [Correct Bleach](https://github.com/fiji/CorrectBleach)
- [TrackMate](https://github.com/fiji/TrackMate)

Required plugins that need installing via the Fiji updater:
- [Ridge Detection](https://github.com/thorstenwagner/ij-ridgedetection)
- [MorphoLibJ](https://github.com/ijpb/MorphoLibJ)

A list of bundled dependencies along with their respective licenses can be found [here](https://rawgit.com/SJCross/ModularImageAnalysis/master/target/site/dependencies.html).
