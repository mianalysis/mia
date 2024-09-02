package io.github.mianalysis.mia.module.inputoutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.measure.Calibration;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.system.FileTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import util.opencsv.CSVReader;

/**
 * Created by sc13967 on 12/05/2017.
 */

/**
* Load centroid coordinates of pre-detected objects from file.  Loaded objects are stored in a single object collection and are represented by a single coordinate point.  For example, this module could be used to import detections from another piece of software or from a previous analysis run.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ObjectLoader extends Module {

	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Object output";

	/**
	* Objects loaded into the workspace will be stored with this name.  They will be accessible by subsequent modules using this name.
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";


	/**
	* 
	*/
    public static final String COORDINATE_SEPARATOR = "Coordinate input";

	/**
	* Controls where the coordinates for the output object collection will be loaded from:<br><ul><li>"Current file" (default option) will use the current root-file for the workspace (this is the file specified in the "Input control" module).</li><li>"Matching format" will load the coordinate file matching a filename based on the root-file for the workspace and a series of rules.</li><li>"Specific file" will load the coordinate file at the location specified by "Input file".</li></ul>
	*/
    public static final String COORDINATE_SOURCE = "Coordinate source";

	/**
	* Method to use for generation of the input filename:<br><ul><li>"Generic (from metadata)" (default) will generate a name from metadata values stored in the current workspace.</li><li>"Input filename with prefix" will load a file with the same name as the input image, but with an additional prefix, specified by the "Prefix" parameter.</li><li>"Input filename with suffix" will load a file with the same name as the input image, but with an additional suffix, specified by the "Suffix" parameter.</li></ul>
	*/
    public static final String NAME_FORMAT = "Name format";

	/**
	* Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the "V{name}" notation, where "name" is the name of the variable to insert.  Similarly, metadata values are specified with the "M{name}" notation.
	*/
    public static final String GENERIC_FORMAT = "Generic format";

	/**
	* List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.
	*/
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

	/**
	* Prefix to use when generating coordinate file filename in "Input filename with prefix" mode.
	*/
    public static final String PREFIX = "Prefix";

	/**
	* Suffix to use when generating coordinate file filename in "Input filename with suffix" mode.
	*/
    public static final String SUFFIX = "Suffix";

	/**
	* Extension for the generated filename.
	*/
    public static final String EXTENSION = "Extension";

	/**
	* Option to include the current series number when compiling filenames.  This may be necessary when working with multi-series files, as there will be multiple analyses completed for the same root file.
	*/
    public static final String INCLUDE_SERIES_NUMBER = "Include series number";

	/**
	* Path to specific file to be loaded when "Coordinate source" is in "Specific file" mode.
	*/
    public static final String INPUT_FILE = "Input file";


	/**
	* 
	*/
    public static final String COLUMN_SEPARATOR = "Column selection";

	/**
	* Index of column in input coordinates file specifying the object ID number.
	*/
    public static final String ID_COLUMN_INDEX = "ID-column index";

	/**
	* Index of column in input coordinates file specifying the object x-centroid location (pixel units).
	*/
    public static final String X_COLUMN_INDEX = "X-column index";

	/**
	* Index of column in input coordinates file specifying the object y-centroid location (pixel units).
	*/
    public static final String Y_COLUMN_INDEX = "Y-column index";

	/**
	* Index of column in input coordinates file specifying the object z-centroid location (slice units).
	*/
    public static final String Z_COLUMN_INDEX = "Z-column index";

	/**
	* Index of column in input coordinates file specifying the timepoint the object appears in.  Timepoint numbering starts at 0.
	*/
    public static final String T_COLUMN_INDEX = "Timepoint-column index";


	/**
	* 
	*/
    public static final String LIMIT_SEPARATOR = "Coordinate limits and calibration";

	/**
	* Controls how the spatial limits (width, height, number of slices and number of timepoints) for the output object collection are defined:<br><ul><li>"From image" limits match the spatial and temporal dimensions of an image in the workspace (specified with the "Limits reference image" parameter).  This is equivalent to how spatial limits are determined when identifying objects directly from an image.</li><li>"Manual" limits are specified using the "Width", "Height", "Number of slices" and "Number of timepoints" parameters.</li><li>"Maximum coordinate" limits are determined from the maximum x,y,z coordinates and timepoint present in the loaded coordinate set.</li></ul>
	*/
    public static final String LIMITS_SOURCE = "Limits source";

	/**
	* Image used to determine spatial and temporal limits of the output object collection if "Limits source" is set to "From image".
	*/
    public static final String LIMITS_REFERENCE_IMAGE = "Limits reference image";

	/**
	* Output object collection spatial width to be used if "Limits source" is set to "Manual".  Specified in pixel units.
	*/
    public static final String WIDTH = "Width";

	/**
	* Output object collection spatial height to be used if "Limits source" is set to "Manual".  Specified in pixel units.
	*/
    public static final String HEIGHT = "Height";

	/**
	* Output object collection number of slices (depth) to be used if "Limits source" is set to "Manual".  Specified in slice units.
	*/
    public static final String N_SLICES = "Number of slices";

	/**
	* Output object collection number of frames to be used if "Limits source" is set to "Manual".
	*/
    public static final String N_FRAMES = "Number of timepoints";

	/**
	* Time between adjacent frames if dealing with objects detected across multiple timepoints.  Units for this are specified in the main "Input control" module.
	*/
    public static final String FRAME_INTERVAL = "Frame interval";

	/**
	* Controls how the spatial calibration for the output object collection are defined:<br><ul><li>"From image" spatial calibrations match those of an image in the workspace (specified with the "Spatial calibration ref. image" parameter).  This is equivalent to how calibrations are determined when identifying objects directly from an image.</li><li>"Manual" spatial calibrations are specified using the "XY calibration (dist/px)" and "Z calibration (dist/px)" parameters.</li></ul>
	*/
    public static final String SPATIAL_CALIBRATION_SOURCE = "Spatial calibration source";

	/**
	* Image used to determine spatial calibrations of the output object collection if "Spatial calibration source" is set to "From image".
	*/
    public static final String SPATIAL_CALIBRATION_REFERENCE_IMAGE = "Spatial calibration ref. image";

	/**
	* Controls how the temporal calibration for the output object collection is defined:<br><ul><li>"From image" temporal calibration matches that of an image in the workspace (specified with the "Temporal calibration ref. image" parameter).  This is equivalent to how calibrations are determined when identifying objects directly from an image.</li><li>"Manual" temporal calibration is specified using the "Frame interval" parameter.</li></ul>
	*/
    public static final String TEMPORAL_CALIBRATION_SOURCE = "Temporal calibration source";

	/**
	* Image used to determine the temporal calibration of the output object collection if "Temporal calibration source" is set to "From image".
	*/
    public static final String TEMPORAL_CALIBRATION_REFERENCE_IMAGE = "Temporal calibration ref. image";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";


	/**
	* 
	*/
    public static final String RELATIONSHIP_SEPARATOR = "Relationship controls";

	/**
	* When selected, an output parent object collection can also be specified which allows objects to be linked.  These parent objectcs can only perform a linking function for the output objects; the parent objects themselves do not contain any coordinate information.  For example, the loaded parent objects could be tracks or clusters.
	*/
    public static final String CREATE_PARENTS = "Create parent objects";

	/**
	* Name of the output parent objects collection.
	*/
    public static final String PARENT_OBJECTS_NAME = "Output parent objects name";

	/**
	* Index of column in input coordinates file specifying the parent object ID number.
	*/
    public static final String PARENTS_COLUMN_INDEX = "Parent object ID index";

    public ObjectLoader(Modules modules) {
        super("Load objects", modules);
    }

    public interface CoordinateSources {
        String CURRENT_FILE = "Current file";
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[] { CURRENT_FILE, MATCHING_FORMAT, SPECIFIC_FILE };

    }

    public interface NameFormats {
        String GENERIC = "Generic (from metadata)";
        String INPUT_FILE_PREFIX = "Input filename with prefix";
        String INPUT_FILE_SUFFIX = "Input filename with suffix";

        String[] ALL = new String[] { GENERIC, INPUT_FILE_PREFIX, INPUT_FILE_SUFFIX };

    }

    public interface LimitsSources {
        String FROM_IMAGE = "From image";
        String MANUAL = "Manual";
        String MAXIMUM_COORDINATE = "Maximum coordinate";

        String[] ALL = new String[] { FROM_IMAGE, MANUAL, MAXIMUM_COORDINATE };

    }

    public interface CalibrationSources {
        String FROM_IMAGE = "From image";
        String MANUAL = "Manual";

        String[] ALL = new String[] { FROM_IMAGE, MANUAL };

    }

    public interface ParentTypes {
        String CLUSTER = "Cluster";
        String NORMAL = "Normal";
        String TRACK = "Track";

        String[] ALL = new String[] { CLUSTER, NORMAL, TRACK };

    }

    File getInputFile(Workspace workspace) {
        String coordinateSource = parameters.getValue(COORDINATE_SOURCE, workspace);
        String nameFormat = parameters.getValue(NAME_FORMAT, workspace);
        String genericFormat = parameters.getValue(GENERIC_FORMAT, workspace);
        String prefix = parameters.getValue(PREFIX, workspace);
        String suffix = parameters.getValue(SUFFIX, workspace);
        String ext = parameters.getValue(EXTENSION, workspace);
        boolean includeSeriesNumber = parameters.getValue(INCLUDE_SERIES_NUMBER, workspace);
        String filePath = parameters.getValue(INPUT_FILE, workspace);

        // Getting input file
        try {
            switch (coordinateSource) {
                case CoordinateSources.CURRENT_FILE:
                default:
                    return workspace.getMetadata().getFile();
                case CoordinateSources.MATCHING_FORMAT:
                    switch (nameFormat) {
                        case NameFormats.GENERIC:
                        default:
                            Metadata metadata = (Metadata) workspace.getMetadata().clone();
                            metadata.setComment(prefix);
                            return new File(FileTools.getGenericName(metadata, genericFormat));
                        case NameFormats.INPUT_FILE_PREFIX:
                            metadata = (Metadata) workspace.getMetadata().clone();
                            metadata.setComment(prefix);
                            return new File(ImageLoader.getPrefixName(metadata, includeSeriesNumber, ext));
                        case NameFormats.INPUT_FILE_SUFFIX:
                            metadata = (Metadata) workspace.getMetadata().clone();
                            metadata.setComment(suffix);
                            return new File(ImageLoader.getSuffixName(metadata, includeSeriesNumber, ext));
                    }
                case CoordinateSources.SPECIFIC_FILE:
                    return new File(filePath);
            }
        } catch (DependencyException | FormatException | IOException | ServiceException e) {
            MIA.log.writeError(e);
            return null;
        }
    }

    int[] getLimitsFromImage(Workspace workspace) {
        String referenceImageName = parameters.getValue(LIMITS_REFERENCE_IMAGE, workspace);
        Image image = workspace.getImage(referenceImageName);
        ImagePlus ipl = image.getImagePlus();

        return new int[] { ipl.getWidth(), ipl.getHeight(), ipl.getNSlices(), ipl.getNFrames() };

    }

    int[] getLimitsFromManualValues(Workspace workspace) {
        int[] limits = new int[4];

        limits[0] = parameters.getValue(WIDTH, workspace);
        limits[1] = parameters.getValue(HEIGHT, workspace);
        limits[2] = parameters.getValue(N_SLICES, workspace);
        limits[4] = parameters.getValue(N_FRAMES, workspace);

        return limits;

    }

    int[] getLimitsFromMaximumCoordinates(Workspace workspace, File inputFile) {
        int xIdx = parameters.getValue(X_COLUMN_INDEX, workspace);
        int yIdx = parameters.getValue(Y_COLUMN_INDEX, workspace);
        int zIdx = parameters.getValue(Z_COLUMN_INDEX, workspace);
        int tIdx = parameters.getValue(T_COLUMN_INDEX, workspace);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            MIA.log.writeWarning("File not found: \"" + inputFile.getName() + "\"");
            return null;
        }

        // Initialising the limits array
        int[] limits = new int[] { 0, 0, 0, 0 };

        // Iterating over each row, adding as a new Obj
        CSVReader csvReader = new CSVReader(reader);
        try {
            String[] row = csvReader.readNext();
            while (row != null) {
                try {
                    // Comparing the coordinates
                    int x = (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    int y = (int) Math.round((double) Double.parseDouble(row[yIdx]));
                    int z = zIdx == -1 ? 0 : (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    int t = Integer.parseInt(row[tIdx]);
                    limits[0] = Math.max(limits[0], x);
                    limits[1] = Math.max(limits[1], y);
                    limits[2] = Math.max(limits[2], z);
                    limits[3] = Math.max(limits[3], t);

                } catch (NumberFormatException e) {
                }

                row = csvReader.readNext();

            }

        } catch (IOException e) {
            MIA.log.writeError(e);
            return null;
        }

        limits[0] = limits[0] + 1;
        limits[1] = limits[1] + 1;
        limits[2] = limits[2] + 1;

        return limits;

    }

    double[] getSpatialCalibrationFromImage(Workspace workspace) {
        double[] cal = new double[2];

        String referenceImageName = parameters.getValue(SPATIAL_CALIBRATION_REFERENCE_IMAGE, workspace);
        Image image = workspace.getImage(referenceImageName);
        Calibration calibration = image.getImagePlus().getCalibration();

        cal[0] = calibration.pixelWidth;
        cal[1] = calibration.pixelDepth;

        return cal;

    }

    double[] getSpatialCalibrationFromManualValues(Workspace workspace) {
        double[] cal = new double[2];

        cal[0] = parameters.getValue(XY_CAL, workspace);
        cal[1] = parameters.getValue(Z_CAL, workspace);

        return cal;

    }

    double getTemporalCalibrationFromImage(Workspace workspace) {
        String referenceImageName = parameters.getValue(TEMPORAL_CALIBRATION_REFERENCE_IMAGE, workspace);
        Image image = workspace.getImage(referenceImageName);

        return image.getImagePlus().getCalibration().frameInterval;

    }

    double getTemporalCalibrationFromManualValues(Workspace workspace) {
        return parameters.getValue(FRAME_INTERVAL, workspace);

    }

    void loadObjects(Objs outputObjects, File inputFile, Workspace workspace, @Nullable Objs parentObjects) {
        int xIdx = parameters.getValue(X_COLUMN_INDEX, workspace);
        int yIdx = parameters.getValue(Y_COLUMN_INDEX, workspace);
        int zIdx = parameters.getValue(Z_COLUMN_INDEX, workspace);
        int tIdx = parameters.getValue(T_COLUMN_INDEX, workspace);
        int parentsIdx = parameters.getValue(PARENTS_COLUMN_INDEX, workspace);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            MIA.log.writeWarning("File not found: \"" + inputFile.getName() + "\"");
            return;
        }

        // Iterating over each row, adding as a new Obj
        VolumeType type = VolumeType.POINTLIST;
        int outOfRangeCount = 0;
        try {
            CSVReader csvReader = new CSVReader(reader);

            String[] row = csvReader.readNext();
            int count = 1;
            while (row != null) {
                // If any of these fields can't be read (e.g. for the title row) we get a
                // NumberFormatException and the row is skipped
                try {
                    // Comparing the coordinates
                    int x = (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    int y = (int) Math.round((double) Double.parseDouble(row[yIdx]));
                    int z = zIdx == -1 ? 0 : (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    int t = (int) Math.round((double) Double.parseDouble(row[tIdx]));

                    // Creating the object and setting the coordinates
                    Obj obj = outputObjects.createAndAddNewObject(type);
                    try {
                        obj.add(x, y, z);
                    } catch (PointOutOfRangeException e) {
                        outOfRangeCount++;
                    }
                    obj.setT(t);
                    outputObjects.add(obj);

                    // Adding parent object, if necessary
                    if (parentObjects != null) {
                        int parentID = (int) Math.round((double) Double.parseDouble(row[parentsIdx]));

                        // Getting parent object. If it doesn't exist, adding it to the parent objects
                        // collection
                        if (!parentObjects.containsKey(parentID))
                            parentObjects.createAndAddNewObject(obj.getVolumeType(), parentID);
                        Obj parentObj = parentObjects.get(parentID);

                        // Adding relationship
                        parentObj.addChild(obj);
                        obj.addParent(parentObj);

                    }

                    writeStatus("Loaded object " + (++count));

                } catch (NumberFormatException e) {
                }

                // Getting the next row
                row = csvReader.readNext();

            }

            // Closing the readers
            csvReader.close();
            reader.close();

        } catch (IOException e) {
            MIA.log.writeError(e);
        }

        if (outOfRangeCount > 0)
            MIA.log.writeWarning(outOfRangeCount + " points ignored due to exceeding spatial limits");
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Load centroid coordinates of pre-detected objects from file.  Loaded objects are stored in a single object collection and are represented by a single coordinate point.  For example, this module could be used to import detections from another piece of software or from a previous analysis run.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String limitsSource = parameters.getValue(LIMITS_SOURCE, workspace);
        String spatialCalSource = parameters.getValue(SPATIAL_CALIBRATION_SOURCE, workspace);
        String temporalCalSource = parameters.getValue(TEMPORAL_CALIBRATION_SOURCE, workspace);
        boolean createParents = parameters.getValue(CREATE_PARENTS, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME, workspace);

        // Getting file to load
        File inputFile = getInputFile(workspace);

        // Getting limits for output objects
        int[] limits = null;
        switch (limitsSource) {
            case LimitsSources.FROM_IMAGE:
                limits = getLimitsFromImage(workspace);
                break;
            case LimitsSources.MANUAL:
                limits = getLimitsFromManualValues(workspace);
                break;
            case LimitsSources.MAXIMUM_COORDINATE:
                limits = getLimitsFromMaximumCoordinates(workspace, inputFile);
                break;
        }
        if (limits == null)
            return Status.FAIL;

        // Getting calibration for output objects
        double[] spatialCal = null;
        switch (spatialCalSource) {
            case CalibrationSources.FROM_IMAGE:
                spatialCal = getSpatialCalibrationFromImage(workspace);
                break;
            case CalibrationSources.MANUAL:
                spatialCal = getSpatialCalibrationFromManualValues(workspace);
                break;
        }
        if (spatialCal == null)
            return Status.FAIL;

        double temporalCal = Double.NaN;
        switch (temporalCalSource) {
            case CalibrationSources.FROM_IMAGE:
                temporalCal = getTemporalCalibrationFromImage(workspace);
                break;
            case CalibrationSources.MANUAL:
                temporalCal = getTemporalCalibrationFromManualValues(workspace);
                break;
        }
        if (Double.isNaN(temporalCal))
            return Status.FAIL;

        String units = SpatialUnit.getOMEUnit().getSymbol();
        SpatCal calibration = new SpatCal(spatialCal[0], spatialCal[1], units, limits[0], limits[1], limits[2]);

        // Creating output objects
        Objs outputObjects = new Objs(outputObjectsName, calibration, limits[3], temporalCal,
                TemporalUnit.getOMEUnit());
        workspace.addObjects(outputObjects);

        // Creating parent objects
        Objs parentObjects = null;
        if (createParents) {
            double frameInterval = parameters.getValue(FRAME_INTERVAL, workspace);
            parentObjects = new Objs(parentObjectsName, calibration, limits[3], frameInterval,
                    TemporalUnit.getOMEUnit());
            workspace.addObjects(parentObjects);
        }

        // Loading objects to the specified collections
        loadObjects(outputObjects, inputFile, workspace, parentObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(COORDINATE_SEPARATOR, this));
        parameters.add(new ChoiceP(COORDINATE_SOURCE, this, CoordinateSources.CURRENT_FILE, CoordinateSources.ALL));
        parameters.add(new ChoiceP(NAME_FORMAT, this, NameFormats.GENERIC, NameFormats.ALL));
        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, ParameterState.MESSAGE, 170));
        parameters.add(new StringP(PREFIX, this));
        parameters.add(new StringP(SUFFIX, this));
        parameters.add(new StringP(EXTENSION, this));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER, this, true));
        parameters.add(new FilePathP(INPUT_FILE, this));

        parameters.add(new SeparatorP(COLUMN_SEPARATOR, this));
        parameters.add(new IntegerP(ID_COLUMN_INDEX, this, 0));
        parameters.add(new IntegerP(X_COLUMN_INDEX, this, 1));
        parameters.add(new IntegerP(Y_COLUMN_INDEX, this, 2));
        parameters.add(new IntegerP(Z_COLUMN_INDEX, this, 3));
        parameters.add(new IntegerP(T_COLUMN_INDEX, this, 4));

        parameters.add(new SeparatorP(LIMIT_SEPARATOR, this));
        parameters.add(new ChoiceP(LIMITS_SOURCE, this, LimitsSources.FROM_IMAGE, LimitsSources.ALL));
        parameters.add(new InputImageP(LIMITS_REFERENCE_IMAGE, this));
        parameters.add(new IntegerP(WIDTH, this, 512));
        parameters.add(new IntegerP(HEIGHT, this, 512));
        parameters.add(new IntegerP(N_SLICES, this, 1));
        parameters.add(new IntegerP(N_FRAMES, this, 1));
        parameters.add(new DoubleP(FRAME_INTERVAL, this, 1d));
        parameters.add(
                new ChoiceP(SPATIAL_CALIBRATION_SOURCE, this, CalibrationSources.FROM_IMAGE, CalibrationSources.ALL));
        parameters.add(new InputImageP(SPATIAL_CALIBRATION_REFERENCE_IMAGE, this));
        parameters.add(new DoubleP(XY_CAL, this, 1d));
        parameters.add(new DoubleP(Z_CAL, this, 1d));
        parameters.add(
                new ChoiceP(TEMPORAL_CALIBRATION_SOURCE, this, CalibrationSources.FROM_IMAGE, CalibrationSources.ALL));
        parameters.add(new InputImageP(TEMPORAL_CALIBRATION_REFERENCE_IMAGE, this));

        parameters.add(new SeparatorP(RELATIONSHIP_SEPARATOR, this));
        parameters.add(new BooleanP(CREATE_PARENTS, this, false));
        parameters.add(new OutputObjectsP(PARENT_OBJECTS_NAME, this));
        parameters.add(new IntegerP(PARENTS_COLUMN_INDEX, this, 5));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.get(COORDINATE_SEPARATOR));
        returnedParameters.add(parameters.get(COORDINATE_SOURCE));
        switch ((String) parameters.getValue(COORDINATE_SOURCE, workspace)) {
            case CoordinateSources.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(NAME_FORMAT));
                switch ((String) parameters.getValue(NAME_FORMAT, workspace)) {
                    case NameFormats.GENERIC:
                        returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                        returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                        MetadataRefs metadataRefs = modules.getMetadataRefs(this);
                        parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                        break;
                    case NameFormats.INPUT_FILE_PREFIX:
                        returnedParameters.add(parameters.getParameter(PREFIX));
                        returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
                        returnedParameters.add(parameters.getParameter(EXTENSION));
                        break;
                    case NameFormats.INPUT_FILE_SUFFIX:
                        returnedParameters.add(parameters.getParameter(SUFFIX));
                        returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
                        returnedParameters.add(parameters.getParameter(EXTENSION));
                        break;
                }
                break;
            case CoordinateSources.SPECIFIC_FILE:
                returnedParameters.add(parameters.get(INPUT_FILE));
                break;
        }

        returnedParameters.add(parameters.get(COLUMN_SEPARATOR));
        returnedParameters.add(parameters.get(ID_COLUMN_INDEX));
        returnedParameters.add(parameters.get(X_COLUMN_INDEX));
        returnedParameters.add(parameters.get(Y_COLUMN_INDEX));
        returnedParameters.add(parameters.get(Z_COLUMN_INDEX));
        returnedParameters.add(parameters.get(T_COLUMN_INDEX));

        returnedParameters.add(parameters.get(LIMIT_SEPARATOR));
        returnedParameters.add(parameters.get(LIMITS_SOURCE));
        switch ((String) parameters.getValue(LIMITS_SOURCE, workspace)) {
            case LimitsSources.FROM_IMAGE:
                returnedParameters.add(parameters.get(LIMITS_REFERENCE_IMAGE));
                break;
            case LimitsSources.MANUAL:
                returnedParameters.add(parameters.get(WIDTH));
                returnedParameters.add(parameters.get(HEIGHT));
                returnedParameters.add(parameters.get(N_SLICES));
                returnedParameters.add(parameters.get(N_FRAMES));
                break;
        }

        returnedParameters.add(parameters.get(SPATIAL_CALIBRATION_SOURCE));
        switch ((String) parameters.getValue(SPATIAL_CALIBRATION_SOURCE, workspace)) {
            case CalibrationSources.FROM_IMAGE:
                returnedParameters.add(parameters.get(SPATIAL_CALIBRATION_REFERENCE_IMAGE));
                break;
            case CalibrationSources.MANUAL:
                returnedParameters.add(parameters.get(XY_CAL));
                returnedParameters.add(parameters.get(Z_CAL));
                break;
        }

        returnedParameters.add(parameters.get(TEMPORAL_CALIBRATION_SOURCE));
        switch ((String) parameters.getValue(TEMPORAL_CALIBRATION_SOURCE, workspace)) {
            case CalibrationSources.FROM_IMAGE:
                returnedParameters.add(parameters.get(TEMPORAL_CALIBRATION_REFERENCE_IMAGE));
                break;
            case CalibrationSources.MANUAL:
                returnedParameters.add(parameters.get(FRAME_INTERVAL));
                break;
        }

        returnedParameters.add(parameters.get(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.get(CREATE_PARENTS));
        if ((boolean) parameters.getValue(CREATE_PARENTS, workspace)) {
            returnedParameters.add(parameters.get(PARENT_OBJECTS_NAME));
            returnedParameters.add(parameters.get(PARENTS_COLUMN_INDEX));
        }

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
	return null; 
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        if ((boolean) parameters.getValue(CREATE_PARENTS, workspace)) {
            String childObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
            String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME, workspace);

            returnedRelationships.add(parentChildRefs.getOrPut(parentObjectsName, childObjectsName));
        }

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

    void addParameterDescriptions() {
        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Objects loaded into the workspace will be stored with this name.  They will be accessible by subsequent modules using this name.");

        parameters.get(COORDINATE_SOURCE).setDescription(
                "Controls where the coordinates for the output object collection will be loaded from:<br><ul>"

                        + "<li>\"" + CoordinateSources.CURRENT_FILE
                        + "\" (default option) will use the current root-file for the workspace (this is the file specified in the \""
                        + new InputControl(null).getName() + "\" module).</li>"

                        + "<li>\"" + CoordinateSources.MATCHING_FORMAT
                        + "\" will load the coordinate file matching a filename based on the root-file for the workspace and a series of rules.</li>"

                        + "<li>\"" + CoordinateSources.SPECIFIC_FILE
                        + "\" will load the coordinate file at the location specified by \"" + INPUT_FILE
                        + "\".</li></ul>");

        parameters.get(NAME_FORMAT).setDescription("Method to use for generation of the input filename:<br><ul>"

                + "<li>\"" + NameFormats.GENERIC
                + "\" (default) will generate a name from metadata values stored in the current workspace.</li>"

                + "<li>\"" + NameFormats.INPUT_FILE_PREFIX
                + "\" will load a file with the same name as the input image, but with an additional prefix, specified by the \""
                + PREFIX + "\" parameter.</li>"

                + "<li>\"" + NameFormats.INPUT_FILE_SUFFIX
                + "\" will load a file with the same name as the input image, but with an additional suffix, specified by the \""
                + SUFFIX + "\" parameter.</li></ul>");

        parameters.get(GENERIC_FORMAT).setDescription(
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");

        parameters.get(PREFIX).setDescription("Prefix to use when generating coordinate file filename in \""
                + NameFormats.INPUT_FILE_PREFIX + "\" mode.");

        parameters.get(SUFFIX).setDescription("Suffix to use when generating coordinate file filename in \""
                + NameFormats.INPUT_FILE_SUFFIX + "\" mode.");

        parameters.get(EXTENSION).setDescription("Extension for the generated filename.");

        parameters.get(INCLUDE_SERIES_NUMBER).setDescription(
                "Option to include the current series number when compiling filenames.  This may be necessary when working with multi-series files, as there will be multiple analyses completed for the same root file.");

        parameters.get(INPUT_FILE).setDescription("Path to specific file to be loaded when \"" + COORDINATE_SOURCE
                + "\" is in \"" + CoordinateSources.SPECIFIC_FILE + "\" mode.");

        parameters.get(ID_COLUMN_INDEX)
                .setDescription("Index of column in input coordinates file specifying the object ID number.");

        parameters.get(X_COLUMN_INDEX).setDescription(
                "Index of column in input coordinates file specifying the object x-centroid location (pixel units).");

        parameters.get(Y_COLUMN_INDEX).setDescription(
                "Index of column in input coordinates file specifying the object y-centroid location (pixel units).");

        parameters.get(Z_COLUMN_INDEX).setDescription(
                "Index of column in input coordinates file specifying the object z-centroid location (slice units).");

        parameters.get(T_COLUMN_INDEX).setDescription(
                "Index of column in input coordinates file specifying the timepoint the object appears in.  Timepoint numbering starts at 0.");

        parameters.get(LIMITS_SOURCE).setDescription(
                "Controls how the spatial limits (width, height, number of slices and number of timepoints) for the output object collection are defined:<br><ul>"

                        + "<li>\"" + LimitsSources.FROM_IMAGE
                        + "\" limits match the spatial and temporal dimensions of an image in the workspace (specified with the \""
                        + LIMITS_REFERENCE_IMAGE
                        + "\" parameter).  This is equivalent to how spatial limits are determined when identifying objects directly from an image.</li>"

                        + "<li>\"" + LimitsSources.MANUAL + "\" limits are specified using the \"" + WIDTH + "\", \""
                        + HEIGHT + "\", \"" + N_SLICES + "\" and \"" + N_FRAMES + "\" parameters.</li>"

                        + "<li>\"" + LimitsSources.MAXIMUM_COORDINATE
                        + "\" limits are determined from the maximum x,y,z coordinates and timepoint present in the loaded coordinate set.</li></ul>");

        parameters.get(LIMITS_REFERENCE_IMAGE).setDescription(
                "Image used to determine spatial and temporal limits of the output object collection if \""
                        + LIMITS_SOURCE + "\" is set to \"" + LimitsSources.FROM_IMAGE + "\".");

        parameters.get(WIDTH).setDescription("Output object collection spatial width to be used if \"" + LIMITS_SOURCE
                + "\" is set to \"" + LimitsSources.MANUAL + "\".  Specified in pixel units.");

        parameters.get(HEIGHT).setDescription("Output object collection spatial height to be used if \"" + LIMITS_SOURCE
                + "\" is set to \"" + LimitsSources.MANUAL + "\".  Specified in pixel units.");

        parameters.get(N_SLICES).setDescription("Output object collection number of slices (depth) to be used if \""
                + LIMITS_SOURCE + "\" is set to \"" + LimitsSources.MANUAL + "\".  Specified in slice units.");

        parameters.get(N_FRAMES).setDescription("Output object collection number of frames to be used if \""
                + LIMITS_SOURCE + "\" is set to \"" + LimitsSources.MANUAL + "\".");

        parameters.get(SPATIAL_CALIBRATION_SOURCE).setDescription(
                "Controls how the spatial calibration for the output object collection are defined:<br><ul>"

                        + "<li>\"" + CalibrationSources.FROM_IMAGE
                        + "\" spatial calibrations match those of an image in the workspace (specified with the \""
                        + SPATIAL_CALIBRATION_REFERENCE_IMAGE
                        + "\" parameter).  This is equivalent to how calibrations are determined when identifying objects directly from an image.</li>"

                        + "<li>\"" + CalibrationSources.MANUAL + "\" spatial calibrations are specified using the \""
                        + XY_CAL + "\" and \"" + Z_CAL + "\" parameters.</li></ul>");

        parameters.get(SPATIAL_CALIBRATION_REFERENCE_IMAGE)
                .setDescription("Image used to determine spatial calibrations of the output object collection if \""
                        + SPATIAL_CALIBRATION_SOURCE + "\" is set to \"" + CalibrationSources.FROM_IMAGE + "\".");

        parameters.get(XY_CAL).setDescription(
                "Distance per pixel in the XY plane.  Units for this are specified in the main \"Input control\" module.");

        parameters.get(Z_CAL).setDescription(
                "Distance per slice (Z-axis).  Units for this are specified in the main \"Input control\" module.");

        parameters.get(TEMPORAL_CALIBRATION_SOURCE).setDescription(
                "Controls how the temporal calibration for the output object collection is defined:<br><ul>"

                        + "<li>\"" + CalibrationSources.FROM_IMAGE
                        + "\" temporal calibration matches that of an image in the workspace (specified with the \""
                        + TEMPORAL_CALIBRATION_REFERENCE_IMAGE
                        + "\" parameter).  This is equivalent to how calibrations are determined when identifying objects directly from an image.</li>"

                        + "<li>\"" + CalibrationSources.MANUAL + "\" temporal calibration is specified using the \""
                        + FRAME_INTERVAL + "\" parameter.</li></ul>");

        parameters.get(TEMPORAL_CALIBRATION_REFERENCE_IMAGE)
                .setDescription("Image used to determine the temporal calibration of the output object collection if \""
                        + TEMPORAL_CALIBRATION_SOURCE + "\" is set to \"" + CalibrationSources.FROM_IMAGE + "\".");

        parameters.get(FRAME_INTERVAL).setDescription(
                "Time between adjacent frames if dealing with objects detected across multiple timepoints.  Units for this are specified in the main \"Input control\" module.");

        parameters.get(CREATE_PARENTS).setDescription(
                "When selected, an output parent object collection can also be specified which allows objects to be linked.  These parent objectcs can only perform a linking function for the output objects; the parent objects themselves do not contain any coordinate information.  For example, the loaded parent objects could be tracks or clusters.");

        parameters.get(PARENT_OBJECTS_NAME).setDescription("Name of the output parent objects collection.");

        parameters.get(PARENTS_COLUMN_INDEX)
                .setDescription("Index of column in input coordinates file specifying the parent object ID number.");

    }
}
