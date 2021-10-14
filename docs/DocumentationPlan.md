# HOMEPAGE
## Carousel
1. MIA (Modular Image Analysis)
   - Image: Overlaid screenshots of MIA
   - Subtext: MIA is a Fiji plugin providing a modular framework for assembling image and object analysis pipelines
   - Button: "Get started" goes to "Getting started" page of documentation
1. Automated
   - Image: Screenshot showing editing view
   - Subtext: Workflows can be automated from initial image loading through image processing, object detection, measurement extraction, visualisation, and data exporting
   - Button: "Learn more" goes to "Creating new workflows" guide section
1. Modular
   - Image: Screenshot showing module selection panel of editing view expanded out
   - Subtext: Near 200 modules integrated with key Fiji plugins such as BioFormats, TrackMate and Weka Trainable Segmentation
   - Button: "See modules" goes to list of module descriptions
1. High throughput
   - Image: Show file list view with part-complete progress bars
   - Subtext: Workflows are, by default, compatible with batch processing multiple files within a single folder
   - Button: "Learn more" goes to "Batch processing" guide page
1. Compatible
   - Image: 
   - Subtext: Designed for compatibility with spatially-calibrated 5D images, yielding image and object measurements in both pixel and physical (calibrated) units
   - Button: "Learn more" goes to general guide page
1. Extensible
   - Image: 
   - Subtext: Functionality can be extended both internally, via integration with Fiji’s scripting interface, and externally, with Java modules that extend the core MIA framework
   - Button: "Learn more" goes to "Scripting" guide page
<!-- 5. Reactive
   - Image: Screenshot of editing view with some modules inactive (red) and others skipped
   - Subtext: Module groups automatically activate in response to factors such as availability of images/objects, user inputs and measurement-based filters
   - Button: "Learn more" goes to "Reactive workflows" guide page -->


## Description
[Three buttons below carousel]:
- Get started (Play icon)
- Guides (Book icon)
- Examples (New icon with boxes linked together symbolising modules)

MIA is developed in the Wolfson Bioimaging Facility at the University of Bristol.

-------------------
# GUIDES
# Section: Getting started
## About MIA
- Images and objects
- Assigned measurements
- Relationships between objects
- Batch processing

## Installing MIA

## MIA user interface
- Screenshots showing different elements
- Processing view
- Editing view

## Using existing workflows
- Link to "Batch processing" section when talking about input files

## Results format
- Parameters sheet
- Log sheet
- Summary sheet
- Object-specific sheets

## Batch processing
- Selecting folder


# Section: Creating new workflows
## Loading images into a workflow
- General
    - Input control only specifies root path.  It doesn't actually load any data.
    - If a folder, MIA iterates over each valid file, running the analysis workflow on each file.
- Current file
    - Simple, gives access to that selected file.
- From ImageJ
    - Must be an open window
- Specific file
    - Will be the same for all analysis runs (good for reference images)

## Scripting

## Reactive workflows

## Deploying for end-users
- Making parameters visible
- Renaming modules and parameters
- Allowing modules to be turned on/off

## Troubleshooting
- Logging (debug and memory)

# Section: General MIA structure
## Object models
- Pointlist
- Quadtree
- Octree

-------------------
# EXAMPLES
