package io.github.mianalysis.mia.module.objects.detect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JPanel;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;

import com.drew.lang.annotations.NotNull;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.module.objects.detect.extensions.ManualExtension;
import io.github.mianalysis.mia.module.objects.detect.extensions.ManualExtensionDependencies;
import io.github.mianalysis.mia.module.objects.track.TrackObjects;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactories;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.quadtree.OctreeFactory;
import io.github.mianalysis.mia.object.coordinates.volume.quadtree.QuadtreeFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ClassHunter;
import io.github.mianalysis.mia.process.coordinates.ZInterpolator;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import io.github.mianalysis.mia.process.selectors.ClassSelector;
import io.github.mianalysis.mia.process.selectors.ObjectSelector;

/**
 * Created by sc13967 on 27/02/2018.
 */

/**
 * Manually create objects using the ImageJ selection tools. Selected regions
 * can be interpolated in Z and T to speed up the object creation process.<br>
 * <br>
 * This module will display a control panel and an image onto which selections
 * are made. <br>
 * <br>
 * Following selection of a region to be included in the object, the user can
 * either add this region to a new object ("Add new" button), or add it to an
 * existing object ("Add to existing" button). The target object for adding to
 * an existing object is specified using the "Existing object number" control (a
 * list of existing object IDs is shown directly below this control).<br>
 * <br>
 * References to each selection are displayed below the controls.
 * Previously-added regions can be re-selected by clicking the relevant
 * reference. This allows selections to be deleted or used as a basis for
 * further selections.<br>
 * <br>
 * Once all selections have been made, objects are added to the workspace with
 * the "Finish" button.<br>
 * <br>
 * Objects need to be added slice-by-slice and can be linked in 3D using the
 * "Add to existing" control.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ManuallyIdentifyObjects extends AbstractSaver {
    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image/object input";

    /**
     * Image onto which selections will be drawn. This will be displayed
     * automatically when the module runs.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * 
     */
    public static final String ADD_EXISTING_OBJECTS = "Add existing objects";

    /**
     * 
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * 
     */
    public static final String ALLOW_MISSING_OBJECTS = "Allow missing objects";

    /**
     * 
     */
    public static final String APPLY_EXISTING_CLASS = "Apply existing class";

    /**
     * 
     */
    public static final String METADATA_FOR_CLASS = "Metadata item for class";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
     * Objects created by this module.
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
     * The method used to store pixel coordinates. This only affects performance and
     * memory usage, there is no difference in results obtained using difference
     * storage methods.<br>
     * <ul>
     * <li>"Pointlist" (default) stores object coordinates as a list of XYZ
     * coordinates. This is most efficient for small objects, very thin objects or
     * objects with lots of holes.</li>
     * <li>"Octree" stores objects in an octree format. Here, the coordinate space
     * is broken down into cubes of different sizes, each of which is marked as
     * foreground (i.e. an object) or background. Octrees are most efficient when
     * there are lots of large cubic regions of the same label, as the space can be
     * represented by larger (and thus fewer) cubes. This is best used when there
     * are large, completely solid objects. If z-axis sampling is much larger than
     * xy-axis sampling, it's typically best to opt for the quadtree method.</li>
     * <li>"Quadtree" stores objects in a quadtree format. Here, each Z-plane of the
     * object is broken down into squares of different sizes, each of which is
     * marked as foreground (i.e. an object) or background. Quadtrees are most
     * efficient when there are lots of large square regions of the same label, as
     * the space can be represented by larger (and thus fewer) squares. This is best
     * used when there are large, completely solid objects.</li>
     * </ul>
     */
    public static final String VOLUME_TYPE = "Volume type";

    /**
     * When selected, the same object can be identified across multiple timepoints.
     * The same ID should be used for all objects in this "track" - this will become
     * the ID of the track object itself, while each timepoint instance will be
     * assigned its own unique ID. This feature also enables the use of temporal
     * intepolation of objects.
     */
    public static final String OUTPUT_TRACKS = "Output tracks";

    /**
     * Name of track objects to be added to the workspace. These will be parents of
     * the individual timepoint instances and provide a way of grouping all the
     * individual timepoint instances of a particular object. Track objects
     * themselves do not contain any coordinate information.
     */
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";

    /**
     * Interpolate objects in Z. Objects assigned the same ID will be interpolated
     * to appear in all slices between the top-most and bottom-most specific slices.
     * Specified regions must contain a degree of overlap (higher overlap will give
     * better results).
     */
    public static final String SPATIAL_INTERPOLATION = "Spatial interpolation";

    /**
     * Interpolate objects across multiple frames. Objects assigned the same ID will
     * be interpolated to appear in all frames between the first and last specified
     * timepoints. Specified regions must contain a degree of overlap (higher
     * overlap will give better results).
     */
    public static final String TEMPORAL_INTERPOLATION = "Temporal interpolation";

    public static final String CLASS_SEPARATOR = "Class controls";

    public static final String ASSIGN_CLASSES = "Assign classes";

    public static final String CLASSES_SOURCE = "Classes source";

    public static final String CLASS_FILE = "Class file";

    public static final String ALLOW_ADDITIONS = "Allow additions";

    public static final String CLASS_LIST = "Class list (comma-separated)";

    /**
    * 
    */
    public static final String SELECTION_SEPARATOR = "Object selection controls";

    /**
     * Text that will be displayed to the user on the object selection control
     * panel. This can inform them of the steps they need to take to select the
     * objects.
     */
    public static final String INSTRUCTION_TEXT = "Instruction text";

    /**
     * Default region drawing tool to enable. This tool can be changed by the user
     * when selecting regions. Choices are: Freehand line, Freehand region, Line,
     * Oval, Points, Polygon, Rectangle, Segmented line, Wand (tracing) tool.
     */
    public static final String SELECTOR_TYPE = "Default selector type";
    public static final String POINT_MODE = "Point mode (point-type ROIs only)";

    /**
     * Message to display in title of image.
     */
    public static final String MESSAGE_ON_IMAGE = "Message on image";

    protected HashSet<ManualExtension> extensions = new HashSet<>();
    protected ObjectSelector objectSelector = null;

    public ManuallyIdentifyObjects(String name, Modules modules) {
        super(name, modules);

        // Getting extensions and adding their parameters to this collection, so they
        // can be saved and loaded
        extensions = getAvailableExtensions();
        for (ManualExtension extension : extensions)
            parameters.addAll(extension.getAllParameters());

    }

    public ManuallyIdentifyObjects(Modules modules) {
        this("Manually identify objects", modules);
    }

    public interface SelectorTypes {
        String FREEHAND_LINE = "Freehand line";
        String FREEHAND_REGION = "Freehand region";
        String LINE = "Line";
        String OVAL = "Oval";
        String POINTS = "Points";
        String POLYGON = "Polygon";
        String RECTANGLE = "Rectangle";
        String SEGMENTED_LINE = "Segmented line";
        String SINGLE_POINT = "Single point";
        String WAND = "Wand (tracing) tool";

        String[] ALL = new String[] { FREEHAND_LINE, FREEHAND_REGION, LINE, OVAL, POINTS, POLYGON, RECTANGLE,
                SEGMENTED_LINE, SINGLE_POINT, WAND };

    }

    public interface SpatialInterpolationModes {
        String NONE = "None";
        String SPATIAL = "Spatial";

        String[] ALL = new String[] { NONE, SPATIAL };

    }

    public interface SpatioTemporalInterpolationModes extends SpatialInterpolationModes {
        String NONE = "None";
        String TEMPORAL = "Temporal";
        String SPATIAL_AND_TEMPORAL = "Spatial and temporal";

        String[] ALL = new String[] { NONE, SPATIAL, TEMPORAL, SPATIAL_AND_TEMPORAL };

    }

    public interface ClassesSources {
        String EXISTING_CLASS_FILE = "Existing class file";
        String FIXED_LIST = "Fixed list";
        String NEW_CLASS_FILE = "New class file";

        String[] ALL = new String[] { EXISTING_CLASS_FILE, FIXED_LIST, NEW_CLASS_FILE };

    }

    public interface PointModes extends ObjectSelector.PointModes {
    }

    public interface ObjMetadataItems extends ObjectSelector.ObjMetadataItems {
    }

    HashSet<ManualExtension> getAvailableExtensions() {
        // Getting available extension dependencies
        ManualExtensionDependencies dependencies = new ManualExtensionDependencies();

        // Creating a list of available extensions
        HashSet<ManualExtension> extensions = new HashSet<>();

        List<PluginInfo<ManualExtension>> plugins = ClassHunter.getPlugins(ManualExtension.class);
        for (PluginInfo<ManualExtension> plugin : plugins) {
            // Checking dependencies have been met
            String className = plugin.getClassName().substring(plugin.getClassName().lastIndexOf(".") + 1);
            if (!dependencies.compatible(className, false))
                continue;

            try {
                ManualExtension extension = (ManualExtension) Class.forName(plugin.getClassName())
                        .getConstructor(Module.class).newInstance(this);

                extensions.add(extension);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException
                    | ClassNotFoundException e) {
                MIA.log.writeError(e);
            }
        }

        return extensions;

    }

    void setSelector(String selectorType) {
        switch (selectorType) {
            case SelectorTypes.FREEHAND_LINE:
                IJ.setTool(Toolbar.FREELINE);
                return;
            default:
            case SelectorTypes.FREEHAND_REGION:
                IJ.setTool(Toolbar.FREEROI);
                return;
            case SelectorTypes.LINE:
                IJ.setTool(Toolbar.LINE);
                return;
            case SelectorTypes.OVAL:
                IJ.setTool(Toolbar.OVAL);
                return;
            case SelectorTypes.POINTS:
                IJ.setTool("multi");
                return;
            case SelectorTypes.POLYGON:
                IJ.setTool(Toolbar.POLYGON);
                return;
            case SelectorTypes.RECTANGLE:
                IJ.setTool(Toolbar.RECTANGLE);
                return;
            case SelectorTypes.SEGMENTED_LINE:
                IJ.setTool(Toolbar.POLYLINE);
                return;
            case SelectorTypes.SINGLE_POINT:
                IJ.setTool("point");
                return;
            case SelectorTypes.WAND:
                IJ.setTool(Toolbar.WAND);
                return;
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Manually create objects using the ImageJ selection tools.  Selected regions can be interpolated in Z and T to speed up the object creation process."
                + "<br><br>This module will display a control panel and an image onto which selections are made.  "
                + "<br><br>Following selection of a region to be included in the object, the user can either add this region to a new object (\"Add new\" button), or add it to an existing object (\"Add to existing\" button).  "
                + "The target object for adding to an existing object is specified using the \"Existing object number\" control (a list of existing object IDs is shown directly below this control)."
                + "<br><br>References to each selection are displayed below the controls.  Previously-added regions can be re-selected by clicking the relevant reference.  This allows selections to be deleted or used as a basis for further selections."
                + "<br><br>Once all selections have been made, objects are added to the workspace with the \"Finish\""
                + " button.<br><br>Objects need to be added slice-by-slice and can be linked in 3D using the \""
                + "Add to existing\" control.";
    }

    protected static TreeSet<String> getClasses(String classesSource, String classFile, String classList) {
        TreeSet<String> classes = new TreeSet<>();

        switch (classesSource) {
            case ClassesSources.EXISTING_CLASS_FILE:
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(classFile)));
                    String row = "";
                    while ((row = bufferedReader.readLine()) != null) {
                        row = row.toString().replace("\uFEFF", "");
                        for (String item : row.split(","))
                            classes.add(item);
                    }
                    bufferedReader.close();
                } catch (FileNotFoundException e) {
                    MIA.log.writeWarning("File not found: \"" + classFile + "\"");
                    return null;
                } catch (IOException e) {
                    MIA.log.writeError(e);
                    return null;
                }

                break;
            case ClassesSources.FIXED_LIST:
                String[] classesList = classList.split(",");
                for (String item : classesList)
                    classes.add(item);

                break;
        }

        return classes;

    }

    protected void createClassFile(String classesSource, ClassSelector classSelector, String classFile,
            WorkspaceI workspace, String appendSeriesMode, String appendDateTimeMode, String suffix) {
        switch (classesSource) {
            case ClassesSources.EXISTING_CLASS_FILE:
                TreeSet<String> allClasses = classSelector.getAllClasses();
                writeClassesFile(classFile, allClasses);
                break;
            case ClassesSources.NEW_CLASS_FILE:
                allClasses = classSelector.getAllClasses();
                String outputPath = getOutputPath(modules, workspace);
                String outputName = getOutputName(modules, workspace);

                // Adding last bits to name
                outputPath = outputPath + outputName;
                outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
                outputPath = appendDateTime(outputPath, appendDateTimeMode);
                outputPath = outputPath + suffix + ".csv";

                writeClassesFile(outputPath, allClasses);

                break;
        }
    }

    protected void writeClassesFile(String classFile, TreeSet<String> allClasses) {
        try {
            FileWriter writer;
            try {
                writer = new FileWriter(classFile);
            } catch (FileNotFoundException e) {
                writer = new FileWriter(appendDateTime(classFile, AbstractSaver.AppendDateTimeModes.ALWAYS));
            }

            for (String clazz : allClasses)
                writer.write(clazz + "\n");

            writer.close();

        } catch (IOException e) {
            MIA.log.writeError(e);
        }
    }

    public ObjectSelector getObjectSelector() {
        return objectSelector;
    }

    @Override
    public Status process(WorkspaceI workspace) {// Local access to this is required for the action listeners
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        boolean addExistingObjects = parameters.getValue(ADD_EXISTING_OBJECTS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        boolean applyExistingClass = parameters.getValue(APPLY_EXISTING_CLASS, workspace);
        String metadataForClass = parameters.getValue(METADATA_FOR_CLASS, workspace);
        String type = parameters.getValue(VOLUME_TYPE, workspace);
        boolean outputTracks = parameters.getValue(OUTPUT_TRACKS, workspace);
        String outputTrackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS, workspace);
        boolean spatialInterpolation = parameters.getValue(SPATIAL_INTERPOLATION, workspace);
        boolean temporalInterpolation = parameters.getValue(TEMPORAL_INTERPOLATION, workspace);
        String instructionText = parameters.getValue(INSTRUCTION_TEXT, workspace);
        String selectorType = parameters.getValue(SELECTOR_TYPE, workspace);
        String messageOnImage = parameters.getValue(MESSAGE_ON_IMAGE, workspace);
        String volumeTypeString = parameters.getValue(VOLUME_TYPE, workspace);
        String pointMode = parameters.getValue(POINT_MODE, workspace);
        boolean assignClasses = parameters.getValue(ASSIGN_CLASSES, workspace);
        String classesSource = parameters.getValue(CLASSES_SOURCE, workspace);
        String classFile = parameters.getValue(CLASS_FILE, workspace);
        boolean allowAdditions = parameters.getValue(ALLOW_ADDITIONS, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);
        String classList = parameters.getValue(CLASS_LIST, workspace);

        // Getting input image
        ImageI inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        CoordinateSetFactoryI factory = CoordinateSetFactories.getFactory(type);

        setSelector(selectorType);

        if (!outputTracks)
            outputTrackObjectsName = null;

        // Initialising any available extensions before the image is shown
        for (ManualExtension extension : extensions) {
            Status extensionStatus = extension.initialiseBeforeImageShown(workspace);
            if (extensionStatus != Status.PASS)
                return extensionStatus;
        }

        objectSelector = new ObjectSelector();
        objectSelector.setExtensions(extensions);

        ClassSelector classSelector = null;
        if (assignClasses) {
            TreeSet<String> classes = getClasses(classesSource, classFile, classList);
            classSelector = new ClassSelector(classes, allowAdditions);
            objectSelector.setClassSelector(classSelector);
        }

        for (ManualExtension extension : extensions) {
            JPanel extensionControlPanel = extension.getControlPanel();
            if (extensionControlPanel != null)
                objectSelector.addExtraPanel(extensionControlPanel);
        }

        objectSelector.initialise(inputImagePlus, outputObjectsName, messageOnImage, instructionText, volumeTypeString,
                pointMode, outputTrackObjectsName, false);

        // Loading existing objects
        if (addExistingObjects) {
            Objs inputObjects = workspace.getObjects(inputObjectsName);
            if (!applyExistingClass)
                metadataForClass = null;

            if (inputObjects != null) {
                objectSelector.addObjects(inputObjects, metadataForClass);
                objectSelector.updateOverlay();
            }
        }

        // Initialising any available extensions after the image is shown
        objectSelector.setImageVisible(true);
        ImagePlus displayIpl = objectSelector.getDisplayIpl();
        for (ManualExtension extension : extensions) {
            Status extensionStatus = extension.initialiseAfterImageShown(displayIpl);
            if (extensionStatus != Status.PASS)
                return extensionStatus;
        }

        objectSelector.setControlPanelVisible(true);

        // All the while the control is open, do nothing
        while (objectSelector.isActive())
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

        // If more pixels than Integer.MAX_VALUE were assigned, return false
        // (IntegerOverflowException).
        if (objectSelector.hadOverflow())
            return Status.FAIL;

        // Writing classes to file
        if (assignClasses)
            createClassFile(classesSource, classSelector, classFile, workspace, appendSeriesMode, appendDateTimeMode,
                    suffix);

        // Getting objects
        Objs outputObjects = objectSelector.getObjects();
        Objs outputTrackObjects = objectSelector.getTrackObjects();

        // If necessary, apply interpolation
        try {
            if (spatialInterpolation)
                ZInterpolator.applySpatialInterpolation(outputObjects, factory);
            if (outputTracks && temporalInterpolation)
                ObjectSelector.applyTemporalInterpolation(outputObjects, outputTrackObjects, factory);
        } catch (IntegerOverflowException e) {
            return Status.FAIL;
        }

        workspace.addObjects(outputObjects);

        // Showing the selected objects
        if (showOutput)
            if (outputTracks)
                TrackObjects.showObjects(outputObjects, outputTrackObjectsName);
            else
                outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(ADD_EXISTING_OBJECTS, this, false));
        parameters.add(new CustomInputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(ALLOW_MISSING_OBJECTS, this, false));
        parameters.add(new BooleanP(APPLY_EXISTING_CLASS, this, false));
        parameters.add(new CustomObjectMetadataP(METADATA_FOR_CLASS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, CoordinateSetFactories.getDefaultFactoryName(), CoordinateSetFactories.listFactoryNames()));
        parameters.add(new BooleanP(SPATIAL_INTERPOLATION, this, false));
        parameters.add(new BooleanP(OUTPUT_TRACKS, this, false));
        parameters.add(new OutputTrackObjectsP(OUTPUT_TRACK_OBJECTS, this));
        parameters.add(new BooleanP(TEMPORAL_INTERPOLATION, this, false));

        parameters.add(new SeparatorP(CLASS_SEPARATOR, this));
        parameters.add(new BooleanP(ASSIGN_CLASSES, this, false));
        parameters.add(new ChoiceP(CLASSES_SOURCE, this, ClassesSources.FIXED_LIST, ClassesSources.ALL));
        parameters.add(new FilePathP(CLASS_FILE, this));
        parameters.add(new BooleanP(ALLOW_ADDITIONS, this, false));
        parameters.add(new StringP(CLASS_LIST, this));

        parameters.add(new SeparatorP(SELECTION_SEPARATOR, this));
        parameters.add(new TextAreaP(INSTRUCTION_TEXT, this,
                "Draw round an object, then select one of the following"
                        + "\n(or click \"Finish adding objects\" at any time)."
                        + "\nDifferent timepoints must be added as new objects.",
                true, 100));
        parameters.add(new ChoiceP(SELECTOR_TYPE, this, SelectorTypes.FREEHAND_REGION, SelectorTypes.ALL));
        parameters.add(new ChoiceP(POINT_MODE, this, PointModes.INDIVIDUAL_OBJECTS, PointModes.ALL));
        parameters.add(new StringP(MESSAGE_ON_IMAGE, this, "Draw objects on this image"));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_IMAGE));
        returnedParameters.add(parameters.get(ADD_EXISTING_OBJECTS));
        if ((boolean) parameters.getValue(ADD_EXISTING_OBJECTS, workspace)) {
            returnedParameters.add(parameters.get(INPUT_OBJECTS));
            returnedParameters.add(parameters.get(ALLOW_MISSING_OBJECTS));
            boolean allowMissingObjects = parameters.getValue(ALLOW_MISSING_OBJECTS, workspace);
            ((CustomInputObjectsP) parameters.get(INPUT_OBJECTS)).setAllowMissingObjects(allowMissingObjects);
            ((CustomObjectMetadataP) parameters.get(METADATA_FOR_CLASS)).setAllowMissingObjects(allowMissingObjects);

            if ((boolean) parameters.getValue(ASSIGN_CLASSES, workspace)) {
                returnedParameters.add(parameters.get(APPLY_EXISTING_CLASS));
                if ((boolean) parameters.getValue(APPLY_EXISTING_CLASS, workspace)) {
                    returnedParameters.add(parameters.get(METADATA_FOR_CLASS));
                    String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
                    ((CustomObjectMetadataP) parameters.get(METADATA_FOR_CLASS)).setObjectName(inputObjectsName);
                }
            }
        }

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.get(VOLUME_TYPE));
        returnedParameters.add(parameters.get(POINT_MODE)); // Must always be visible, as user can change ROI type
        returnedParameters.add(parameters.get(SPATIAL_INTERPOLATION));
        returnedParameters.add(parameters.get(OUTPUT_TRACKS));

        if ((boolean) parameters.getValue(OUTPUT_TRACKS, workspace)) {
            returnedParameters.add(parameters.get(OUTPUT_TRACK_OBJECTS));
            returnedParameters.add(parameters.get(TEMPORAL_INTERPOLATION));
        }

        returnedParameters.add(parameters.get(CLASS_SEPARATOR));
        returnedParameters.add(parameters.get(ASSIGN_CLASSES));
        if ((boolean) parameters.getValue(ASSIGN_CLASSES, workspace)) {
            returnedParameters.add(parameters.get(CLASSES_SOURCE));
            switch ((String) parameters.getValue(CLASSES_SOURCE, workspace)) {
                case ClassesSources.EXISTING_CLASS_FILE:
                    returnedParameters.add(parameters.get(CLASS_FILE));
                    returnedParameters.add(parameters.get(ALLOW_ADDITIONS));
                    break;
                case ClassesSources.FIXED_LIST:
                    returnedParameters.add(parameters.get(CLASS_LIST));
                    break;
                case ClassesSources.NEW_CLASS_FILE:
                    Parameters saverParameters = super.updateAndGetParameters();
                    saverParameters.remove(FILE_SAVING_SEPARATOR);
                    returnedParameters.addAll(saverParameters);
                    break;
            }
        }

        returnedParameters.add(parameters.get(SELECTION_SEPARATOR));
        returnedParameters.add(parameters.get(INSTRUCTION_TEXT));
        returnedParameters.add(parameters.get(SELECTOR_TYPE));
        returnedParameters.add(parameters.get(MESSAGE_ON_IMAGE));

        for (ManualExtension extension : extensions)
            returnedParameters.addAll(extension.updateAndGetParameters());

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        if (!(boolean) parameters.getValue(ASSIGN_CLASSES, null))
            return null;

        ObjMetadataRefs returnedRefs = new ObjMetadataRefs();

        String objectsName = parameters.getValue(OUTPUT_OBJECTS, null);

        ObjMetadataRef ref = objectMetadataRefs.getOrPut(ObjMetadataItems.CLASS);
        ref.setObjectsName(objectsName);
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        WorkspaceI workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        if ((boolean) parameters.getValue(OUTPUT_TRACKS, workspace))
            returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(OUTPUT_TRACK_OBJECTS, workspace),
                    parameters.getValue(OUTPUT_OBJECTS, workspace)));

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    class CustomInputObjectsP extends InputObjectsP {
        private boolean allowMissingObjects = false;

        public CustomInputObjectsP(String name, Module module) {
            super(name, module);
        }

        public CustomInputObjectsP(String name, Module module, @NotNull String choice) {
            super(name, module);
            this.choice = choice;

        }

        public CustomInputObjectsP(String name, Module module, @NotNull String choice, String description) {
            super(name, module, description);
            this.choice = choice;

        }

        @Override
        public boolean verify() {
            if (allowMissingObjects)
                return true;
            else
                return super.verify();
        }

        @Override
        public boolean isValid() {
            if (allowMissingObjects)
                return true;
            else
                return super.isValid();
        }

        @Override
        public <T extends Parameter> T duplicate(Module newModule) {
            CustomInputObjectsP newParameter = new CustomInputObjectsP(name, newModule, getRawStringValue(),
                    getDescription());

            newParameter.setNickname(getNickname());
            newParameter.setVisible(isVisible());
            newParameter.setExported(isExported());
            newParameter.setAllowMissingObjects(allowMissingObjects);

            return (T) newParameter;

        }

        public boolean isAllowMissingObjects() {
            return allowMissingObjects;
        }

        public void setAllowMissingObjects(boolean allowMissingObjects) {
            this.allowMissingObjects = allowMissingObjects;
        }
    }

    public class CustomObjectMetadataP extends ObjectMetadataP {
        private boolean allowMissingObjects = false;

        public CustomObjectMetadataP(String name, Module module) {
            super(name, module);
        }

        public CustomObjectMetadataP(String name, Module module, String description) {
            super(name, module, description);
        }

        public CustomObjectMetadataP(String name, Module module, @NotNull String choice, @NotNull String objectName) {
            super(name, module);
            this.objectName = objectName;
            this.choice = choice;

        }

        public CustomObjectMetadataP(String name, Module module, @NotNull String choice, @NotNull String objectName,
                String description) {
            super(name, module, description);
            this.objectName = objectName;
            this.choice = choice;

        }

        @Override
        public boolean verify() {
            if (allowMissingObjects)
                return true;
            else
                return super.verify();
        }

        @Override
        public boolean isValid() {
            if (allowMissingObjects)
                return true;
            else
                return super.isValid();
        }

        @Override
        public <T extends Parameter> T duplicate(Module newModule) {
            CustomObjectMetadataP newParameter = new CustomObjectMetadataP(name, newModule, choice, objectName,
                    getDescription());

            newParameter.setNickname(getNickname());
            newParameter.setVisible(isVisible());
            newParameter.setExported(isExported());
            newParameter.setAllowMissingObjects(allowMissingObjects);

            return (T) newParameter;

        }

        public boolean isAllowMissingObjects() {
            return allowMissingObjects;
        }

        public void setAllowMissingObjects(boolean allowMissingObjects) {
            this.allowMissingObjects = allowMissingObjects;
        }
    }

    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(INPUT_IMAGE).setDescription(
                "Image onto which selections will be drawn.  This will be displayed automatically when the module runs.");

        parameters.get(OUTPUT_OBJECTS).setDescription("Objects created by this module.");

        parameters.get(VOLUME_TYPE).setDescription(
                "The method used to store pixel coordinates.  This only affects performance and memory usage, there is no difference in results obtained using difference storage methods.<br><ul>"
                        + "<li>\"" + new PointListFactory().getName()
                        + "\" (default) stores object coordinates as a list of XYZ coordinates.  This is most efficient for small objects, very thin objects or objects with lots of holes.</li>"
                        + "<li>\"" + new QuadtreeFactory().getName()
                        + "\" stores objects in an octree format.  Here, the coordinate space is broken down into cubes of different sizes, each of which is marked as foreground (i.e. an object) or background.  Octrees are most efficient when there are lots of large cubic regions of the same label, as the space can be represented by larger (and thus fewer) cubes.  This is best used when there are large, completely solid objects.  If z-axis sampling is much larger than xy-axis sampling, it's typically best to opt for the quadtree method.</li>"
                        + "<li>\"" + new OctreeFactory().getName()
                        + "\" stores objects in a quadtree format.  Here, each Z-plane of the object is broken down into squares of different sizes, each of which is marked as foreground (i.e. an object) or background.  Quadtrees are most efficient when there are lots of large square regions of the same label, as the space can be represented by larger (and thus fewer) squares.  This is best used when there are large, completely solid objects.</li></ul>");

        parameters.get(OUTPUT_TRACKS).setDescription(
                "When selected, the same object can be identified across multiple timepoints.  The same ID should be used for all objects in this \"track\" - this will become the ID of the track object itself, while each timepoint instance will be assigned its own unique ID.  This feature also enables the use of temporal intepolation of objects.");

        parameters.get(OUTPUT_TRACK_OBJECTS).setDescription(
                "Name of track objects to be added to the workspace.  These will be parents of the individual timepoint instances and provide a way of grouping all the individual timepoint instances of a particular object.  Track objects themselves do not contain any coordinate information.");

        parameters.get(SPATIAL_INTERPOLATION).setDescription(
                "Interpolate objects in Z.  Objects assigned the same ID will be interpolated to appear in all slices between the top-most and bottom-most specific slices.  Specified regions must contain a degree of overlap (higher overlap will give better results).");

        parameters.get(TEMPORAL_INTERPOLATION).setDescription(
                "Interpolate objects across multiple frames.  Objects assigned the same ID will be interpolated to appear in all frames between the first and last specified timepoints.  Specified regions must contain a degree of overlap (higher overlap will give better results).");

        parameters.get(INSTRUCTION_TEXT).setDescription(
                "Text that will be displayed to the user on the object selection control panel.  This can inform them of the steps they need to take to select the objects.");

        parameters.get(SELECTOR_TYPE).setDescription(
                "Default region drawing tool to enable.  This tool can be changed by the user when selecting regions.  Choices are: "
                        + String.join(", ", SelectorTypes.ALL) + ".");

        parameters.get(MESSAGE_ON_IMAGE).setDescription("Message to display in title of image.");

    }
}
