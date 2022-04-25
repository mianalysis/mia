<!-- [![TravisCI](https://travis-ci.org/mianalysis/mia.svg?branch=master)](https://travis-ci.org/mianalysis/mia) -->
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1201372.svg)](https://doi.org/10.5281/zenodo.1201320)
[![JitPack](https://jitpack.io/v/mianalysis/mia.svg)](https://jitpack.io/#mianalysis/mia/)
[![Testing](https://github.com/mianalysis/mia/actions/workflows/tests.yml/badge.svg)](https://github.com/mianalysis/mia/actions/workflows/tests.yml)


[![Wolfson Bioimaging](./src/main/resources/images/Logo_text_UoB_128.png)](http://www.bristol.ac.uk/wolfson-bioimaging/)

About MIA
------------
Image and object analysis workflow automation plugin for Fiji

For the full documentation, please go to [mianalysis.github.io](https://mianalysis.github.io)


Installation
------------
- The latest version of the plugin can be downloaded from the [Releases](https://github.com/mianalysis/mia/releases) page.
- Place this .jar file into the /plugins directory of the your Fiji installation.
- MIA requires the "Biomedgroup" and "MorphoLibJ" plugins to be installed.
> - For MIA versions before 0.11.0: Launch Fiji's [Updater](https://imagej.net/Updater) tool and add the "Biomedgroup" and "IJPB-plugins" (possibly listed as "MorphoLibJ") update sites, then restart Fiji.
> - For MIA versions 0.11.0 or newer: Start MIA as usual, then click "Yes" when prompted to automatically download the updates.

Create new workflow
------------
- In Fiji, run the plugin from Plugins > ModularImageAnalysis (MIA)
> - Select View > "Switch to editing view" from the menu bar
> - Modules are added and removed from the workflow using the "+" and "-" buttons
> - Module order can be shifted with the arrow buttons
> - Selecting a module will display its relevant parameters in the right panel
> - Checkboxes to the right of each module determine if they are visible in the processing view (default view)
> - Input files and folders are specified using the "Input control" and Excel file export is configured in the "Output control"

Use existing workflow
------------
- In Fiji, run the plugin from Plugins > ModularImageAnalysis (MIA)
- To run an existing analysis workflow
> - Click "Load" and select the .mia workflow file
> - Depending on workflow configuration, a number of controls may be visible
> - When ready, click "Run" to start the analysis
> - While processing the status panel will display the number of files completed (batch mode) or a summary of the ongoing task (single file mode)
> - When finished, "Complete" will be displayed in the status panel.  Output XLS files will be saved in the input directory.

Acknowledgements
------------
The plugin makes use of a combination of plugins packaged with Fiji as well as others that can be installed via the updater.

Required plugins pre-packaged with Fiji: [AnalyzeSkeleton](https://github.com/fiji/AnalyzeSkeleton), [Auto Threshold](https://github.com/fiji/Auto_Threshold), [bUnwarpJ](https://github.com/fiji/bUnwarpJ), [BioFormats](https://github.com/openmicroscopy/bioformats), [Colour Deconvolution](https://github.com/fiji/Colour_Deconvolution), [Correct Bleach](https://github.com/fiji/CorrectBleach), [MPICBG](https://github.com/axtimwalde/mpicbg), [TrackMate](https://github.com/fiji/TrackMate), [Weka Trainable Segmentation](https://github.com/fiji/Trainable_Segmentation).  Required plugins that need installing via the Fiji updater: [MorphoLibJ](https://github.com/ijpb/MorphoLibJ).  Plugins bundled with MIA: [BoneJ legacy plugins](https://github.com/mdoube/BoneJ), [Stack Focuser](https://imagej.nih.gov/ij/plugins/stack-focuser.html).

A list of bundled dependencies along with their respective licenses can be found [here](https://htmlpreview.github.io/?https://github.com/mianalysis/mia/blob/master/target/site/dependencies.html).

Special thanks to all MIA users who have provided vital feedback over the years.  In particular, big thanks to Dr. Dominic Alibhai for his many suggestions and ideas.

Citing MIA
------------
We hope you find MIA useful.  If you've used MIA in your research, please cite it using the Zenodo DOI for that version of the plugin.  Zenodo DOIs for MIA can be found [here](https://doi.org/10.5281/zenodo.1201320).

Publications
------------
MIA has been used in a variety of different analyses, a few published examples of which are listed below. For a more complete list, please go to [Publications](https://mianalysis.github.io/mia/html/publications.html).

Edmunds, G.L., _et al._, "Adenosine 2A receptor and TIM3 suppress cytolytic killing of tumor cells via cytoskeletal polarization", _Communications Biology_ (2022) **5**, doi: 10.1038/s42003-021-02972-8

Kague, E., _et al._, "3D assessment of intervertebral disc degeneration in zebrafish identifies changes in bone density that prime disc disease", _Bone Research_ (2021) **9**, doi: 10.1038/s41413-021-00156-y

McCaughey, J., _et al._, "ER-to-Golgi trafficking of procollagen in the absence of large carriers", _J Cell Biol_ (2019) **218** 929-948, doi: 10.1083/jcb.201806035

Roloff, E.v.L., _et al._, "Differences in autonomic innervation to the vertebrobasilar arteries in spontaneously hypertensive and Wistar rats", _J Physiol_ (2018) **596** 3505-3529, doi: 10.1113/JP275973

Ongoing development
------------
This plugin is still in development and test coverage is currently incomplete.  Please keep an eye on results and add an [issue](https://github.com/mianalysis/mia/issues) if any problems are encountered.

