// package io.github.mianalysis.mia.module.objects.detect;

// import java.awt.Color;
// import java.awt.Dimension;
// import java.awt.GridBagConstraints;
// import java.awt.GridBagLayout;
// import java.awt.Insets;
// import java.util.TreeSet;

// import javax.swing.JComboBox;
// import javax.swing.JLabel;
// import javax.swing.JPanel;
// import javax.swing.JSeparator;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ij.ImagePlus;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.module.objects.track.TrackObjects;
// import io.github.mianalysis.mia.object.Objs;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.image.Image;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import io.github.mianalysis.mia.process.coordinates.ZInterpolator;
// import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
// import io.github.mianalysis.mia.process.selectors.ClassSelector;
// import io.github.mianalysis.mia.process.selectors.ObjectSelector;

// /**
//  * Created by sc13967 on 27/02/2018.
//  */

// /**
//  * Manually create objects using the ImageJ selection tools. Selected regions
//  * can be interpolated in Z and T to speed up the object creation process.<br>
//  * <br>
//  * This module will display a control panel and an image onto which selections
//  * are made. <br>
//  * <br>
//  * Following selection of a region to be included in the object, the user can
//  * either add this region to a new object ("Add new" button), or add it to an
//  * existing object ("Add to existing" button). The target object for adding to
//  * an existing object is specified using the "Existing object number" control (a
//  * list of existing object IDs is shown directly below this control).<br>
//  * <br>
//  * References to each selection are displayed below the controls.
//  * Previously-added regions can be re-selected by clicking the relevant
//  * reference. This allows selections to be deleted or used as a basis for
//  * further selections.<br>
//  * <br>
//  * Once all selections have been made, objects are added to the workspace with
//  * the "Finish" button.<br>
//  * <br>
//  * Objects need to be added slice-by-slice and can be linked in 3D using the
//  * "Add to existing" control.
//  */
// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class IdentifyObjectsSAMJ extends ManuallyIdentifyObjects {
//     private interface SelectorModes {
//         String NONE = "None";
//         String BRUSH = "Brush";
//         String POINT = "Point";
//         String RECTANGLE = "Rectangle";

//         String[] ALL = new String[] { NONE, BRUSH, POINT, RECTANGLE };

//     }

//     public IdentifyObjectsSAMJ(Modules modules) {
//         super("Identify objects with SAMJ", modules);
//     }

//     @Override
//     public String getVersionNumber() {
//         return "1.1.0";
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     protected JPanel getSAMJPanel() {
//         JPanel panel = new JPanel(new GridBagLayout());
//         GridBagConstraints c = new GridBagConstraints();

//         c.gridwidth = 1;
//         c.gridx = 0;
//         c.anchor = GridBagConstraints.LINE_START;
//         c.fill = GridBagConstraints.BOTH;
//         c.insets = new Insets(0, 0, 5, 0);

//         JLabel label = new JLabel("SAMJ selection mode");
//         c.insets = new Insets(0, 5, 5, 0);
//         panel.add(label, c);

//         JComboBox<String> comboBox = new JComboBox<>(SelectorModes.ALL);
//         comboBox.setSelectedItem(SelectorModes.POINT);
//         c.gridx++;
//         panel.add(comboBox, c);

//         JSeparator separator = new JSeparator();
//         separator.setForeground(new Color(0, 0, 0, 0));
//         c.gridx++;
//         c.weightx = 1;
//         panel.add(separator, c);

//         return panel;

//     }

//     @Override
//     public Status process(Workspace workspace) {// Local access to this is required for the action listeners
//         // Getting parameters
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
//         boolean addExistingObjects = parameters.getValue(ADD_EXISTING_OBJECTS, workspace);
//         String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
//         boolean applyExistingClass = parameters.getValue(APPLY_EXISTING_CLASS, workspace);
//         String metadataForClass = parameters.getValue(METADATA_FOR_CLASS, workspace);
//         String type = parameters.getValue(VOLUME_TYPE, workspace);
//         boolean outputTracks = parameters.getValue(OUTPUT_TRACKS, workspace);
//         String outputTrackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS, workspace);
//         boolean spatialInterpolation = parameters.getValue(SPATIAL_INTERPOLATION, workspace);
//         boolean temporalInterpolation = parameters.getValue(TEMPORAL_INTERPOLATION, workspace);
//         String instructionText = parameters.getValue(INSTRUCTION_TEXT, workspace);
//         String selectorType = parameters.getValue(SELECTOR_TYPE, workspace);
//         String messageOnImage = parameters.getValue(MESSAGE_ON_IMAGE, workspace);
//         String volumeTypeString = parameters.getValue(VOLUME_TYPE, workspace);
//         String pointMode = parameters.getValue(POINT_MODE, workspace);
//         boolean assignClasses = parameters.getValue(ASSIGN_CLASSES, workspace);
//         String classesSource = parameters.getValue(CLASSES_SOURCE, workspace);
//         String classFile = parameters.getValue(CLASS_FILE, workspace);
//         boolean allowAdditions = parameters.getValue(ALLOW_ADDITIONS, workspace);
//         String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
//         String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
//         String suffix = parameters.getValue(SAVE_SUFFIX, workspace);
//         String classList = parameters.getValue(CLASS_LIST, workspace);

//         // Getting input image
//         Image inputImage = workspace.getImage(inputImageName);
//         ImagePlus inputImagePlus = inputImage.getImagePlus();

//         setSelector(selectorType);

//         if (!outputTracks)
//             outputTrackObjectsName = null;

//         ObjectSelector objectSelector = new ObjectSelector();

//         ClassSelector classSelector = null;
//         if (assignClasses) {
//             TreeSet<String> classes = getClasses(classesSource, classFile, classList);
//             classSelector = new ClassSelector(classes, allowAdditions);
//             objectSelector.setClassSelector(classSelector);
//         }

//         // Adding SAMJ control panel
//         objectSelector.addExtraPanel(getSAMJPanel());

//         objectSelector.initialise(inputImagePlus, outputObjectsName, messageOnImage, instructionText, volumeTypeString,
//                 pointMode, outputTrackObjectsName, false);

//         // Loading existing objects
//         if (addExistingObjects) {
//             Objs inputObjects = workspace.getObjects(inputObjectsName);
//             if (!applyExistingClass)
//                 metadataForClass = null;

//             if (inputObjects != null) {
//                 objectSelector.addObjects(inputObjects, metadataForClass);
//                 objectSelector.updateOverlay();
//             }
//         }

//         objectSelector.setControlPanelVisible(true);

//         MIA.log.writeDebug(
//                 "Now, need to enable SAMJ.  In fact, should do SAMJ prep before image and object selector are made visible.");

//         // All the while the control is open, do nothing
//         while (objectSelector.isActive())
//             try {
//                 Thread.sleep(100);
//             } catch (InterruptedException e) {
//             }

//         // If more pixels than Integer.MAX_VALUE were assigned, return false
//         // (IntegerOverflowException).
//         if (objectSelector.hadOverflow())
//             return Status.FAIL;

//         // Writing classes to file
//         if (assignClasses && classSelector != null)
//             createClassFile(classesSource, classSelector, classFile, workspace, appendSeriesMode, appendDateTimeMode,
//                     suffix);

//         // Getting objects
//         Objs outputObjects = objectSelector.getObjects();
//         Objs outputTrackObjects = objectSelector.getTrackObjects();

//         // If necessary, apply interpolation
//         try {
//             if (spatialInterpolation)
//                 ZInterpolator.applySpatialInterpolation(outputObjects, type);
//             if (outputTracks && temporalInterpolation)
//                 ObjectSelector.applyTemporalInterpolation(outputObjects, outputTrackObjects, type);
//         } catch (IntegerOverflowException e) {
//             return Status.FAIL;
//         }

//         workspace.addObjects(outputObjects);

//         // Showing the selected objects
//         if (showOutput)
//             if (outputTracks)
//                 TrackObjects.showObjects(outputObjects, outputTrackObjectsName);
//             else
//                 outputObjects.convertToImageIDColours().show(false);

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         super.initialiseParameters();

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         return super.updateAndGetParameters();

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
//         return super.updateAndGetObjectMetadataRefs();
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return super.updateAndGetParentChildRefs();
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }
