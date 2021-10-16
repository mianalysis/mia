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

## Running workflows from a macro


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
- Version error (versions before v0.21.0?)
   - Requires TrackMate 6 or lower

# Section: General MIA structure
## Object models
- Pointlist
- Quadtree
- Octree

-------------------
# EXAMPLES
