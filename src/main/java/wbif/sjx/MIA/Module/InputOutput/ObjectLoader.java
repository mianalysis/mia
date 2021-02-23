package wbif.sjx.MIA.Module.InputOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.measure.Calibration;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import util.opencsv.CSVReader;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Core.InputControl;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Units.SpatialUnit;
import wbif.sjx.MIA.Object.Units.TemporalUnit;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Object.Metadata;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class ObjectLoader extends Module {
    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String COORDINATE_SEPARATOR = "Coordinate input";
    public static final String COORDINATE_SOURCE = "Coordinate source";
    public static final String NAME_FORMAT = "Name format";
    public static final String GENERIC_FORMAT = "Generic format";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
    public static final String PREFIX = "Prefix";
    public static final String SUFFIX = "Suffix";
    public static final String EXTENSION = "Extension";
    public static final String INCLUDE_SERIES_NUMBER = "Include series number";
    public static final String INPUT_FILE = "Input file";

    public static final String COLUMN_SEPARATOR = "Column selection";
    public static final String ID_COLUMN_INDEX = "ID-column index";
    public static final String X_COLUMN_INDEX = "X-column index";
    public static final String Y_COLUMN_INDEX = "Y-column index";
    public static final String Z_COLUMN_INDEX = "Z-column index";
    public static final String T_COLUMN_INDEX = "Timepoint-column index";

    public static final String LIMIT_SEPARATOR = "Coordinate limits and calibration";
    public static final String LIMITS_SOURCE = "Limits source";
    public static final String LIMITS_REFERENCE_IMAGE = "Limits reference image";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String N_SLICES = "Number of slices";
    public static final String N_FRAMES = "Number of timepoints";
    public static final String FRAME_INTERVAL = "Frame interval";
    public static final String CALIBRATION_SOURCE = "Calibration source";
    public static final String CALIBRATION_REFERENCE_IMAGE = "Calibration reference image";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship controls";
    public static final String CREATE_PARENTS = "Create parent objects";
    public static final String PARENT_TYPE = "Parent objects type";
    public static final String PARENT_OBJECTS_NAME = "Output parent objects name";
    public static final String PARENTS_COLUMN_INDEX = "Parent object ID index";

    public ObjectLoader(ModuleCollection modules) {
        super("Load objects",modules);
    }

    public interface CoordinateSources {
        String CURRENT_FILE = "Current file";
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[]{CURRENT_FILE,MATCHING_FORMAT,SPECIFIC_FILE};

    }

    public interface NameFormats {
        String GENERIC = "Generic (from metadata)";
        String INPUT_FILE_PREFIX = "Input filename with prefix";
        String INPUT_FILE_SUFFIX = "Input filename with suffix";

        String[] ALL = new String[]{GENERIC,INPUT_FILE_PREFIX,INPUT_FILE_SUFFIX};

    }

    public interface LimitsSources {
        String FROM_IMAGE = "From image";
        String MANUAL = "Manual";
        String MAXIMUM_COORDINATE = "Maximum coordinate";

        String[] ALL = new String[]{FROM_IMAGE,MANUAL,MAXIMUM_COORDINATE};

    }

    public interface CalibrationSources {
        String FROM_IMAGE = "From image";
        String MANUAL = "Manual";

        String[] ALL = new String[]{FROM_IMAGE,MANUAL};

    }

    public interface ParentTypes {
        String CLUSTER = "Cluster";
        String NORMAL = "Normal";
        String TRACK = "Track";

        String[] ALL = new String[]{CLUSTER,NORMAL,TRACK};

    }


    File getInputFile(Workspace workspace) {
        String coordinateSource = parameters.getValue(COORDINATE_SOURCE);
        String nameFormat = parameters.getValue(NAME_FORMAT);
        String genericFormat = parameters.getValue(GENERIC_FORMAT);
        String prefix = parameters.getValue(PREFIX);
        String suffix = parameters.getValue(SUFFIX);
        String ext = parameters.getValue(EXTENSION);
        boolean includeSeriesNumber = parameters.getValue(INCLUDE_SERIES_NUMBER);
        String filePath = parameters.getValue(INPUT_FILE);

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
                            return new File(ImageLoader.getGenericName(metadata, genericFormat));
                        case NameFormats.INPUT_FILE_PREFIX:
                            metadata = (Metadata) workspace.getMetadata().clone();
                            metadata.setComment(prefix);
                            return new File(ImageLoader.getPrefixName(metadata,includeSeriesNumber,ext));
                        case NameFormats.INPUT_FILE_SUFFIX:
                            metadata = (Metadata) workspace.getMetadata().clone();
                            metadata.setComment(suffix);
                            return new File(ImageLoader.getSuffixName(metadata,includeSeriesNumber,ext));
                    }
                case CoordinateSources.SPECIFIC_FILE:
                    return new File(filePath);
            }
        } catch (DependencyException | FormatException | IOException | ServiceException e) {
            e.printStackTrace();
            return null;
        }
    }

    int[] getLimitsFromImage(Image image) {        
        ImagePlus ipl = image.getImagePlus();

        return new int[]{ipl.getWidth(),ipl.getHeight(),ipl.getNSlices(),ipl.getNFrames()};

    }

    int[] getLimitsFromManualValues() {
        int[] limits = new int[4];

        limits[0] = parameters.getValue(WIDTH);
        limits[1] = parameters.getValue(HEIGHT);
        limits[2] = parameters.getValue(N_SLICES);
        limits[4] = parameters.getValue(N_FRAMES);

        return limits;

    }

    int[] getLimitsFromMaximumCoordinates(Workspace workspace, File inputFile) {
        int xIdx = parameters.getValue(X_COLUMN_INDEX);
        int yIdx = parameters.getValue(Y_COLUMN_INDEX);
        int zIdx = parameters.getValue(Z_COLUMN_INDEX);
        int tIdx = parameters.getValue(T_COLUMN_INDEX);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            MIA.log.writeDebug("File not found: \""+inputFile.getName()+"\"");
            return null;
        }

        // Initialising the limits array
        int[] limits = new int[]{0,0,0,0};

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
                    limits[0] = Math.max(limits[0],x);
                    limits[1] = Math.max(limits[1],y);
                    limits[2] = Math.max(limits[2],z);
                    limits[3] = Math.max(limits[3],t);

                } catch (NumberFormatException e) {}

                row = csvReader.readNext();

            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        limits[0] = limits[0] + 1;
        limits[1] = limits[1] + 1;
        limits[2] = limits[2] + 1;

        return limits;

    }

    double[] getCalibrationFromImage(Workspace workspace, int[] limits) {
        double[] cal = new double[2];

        String referenceImageName = parameters.getValue(CALIBRATION_REFERENCE_IMAGE);
        Image image = workspace.getImage(referenceImageName);
        Calibration calibration = image.getImagePlus().getCalibration();

        cal[0] = calibration.pixelWidth;
        cal[1] = calibration.pixelDepth;

        return cal;

    }

    double[] getCalibrationFromManualValues() {
        double[] cal = new double[2];

        cal[0] = parameters.getValue(XY_CAL);
        cal[1] = parameters.getValue(Z_CAL);

        return cal;

    }

    void loadObjects(ObjCollection outputObjects, File inputFile, @Nullable ObjCollection parentObjects) {
        int xIdx = parameters.getValue(X_COLUMN_INDEX);
        int yIdx = parameters.getValue(Y_COLUMN_INDEX);
        int zIdx = parameters.getValue(Z_COLUMN_INDEX);
        int tIdx = parameters.getValue(T_COLUMN_INDEX);
        int parentsIdx = parameters.getValue(PARENTS_COLUMN_INDEX);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            MIA.log.writeDebug("File not found: \""+inputFile.getName()+"\"");
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
                // If any of these fields can't be read (e.g. for the title row) we get a NumberFormatException and the row is skipped
                try {
                    // Comparing the coordinates
                    int x = (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    int y = (int) Math.round((double) Double.parseDouble(row[yIdx]));
                    int z = zIdx == -1 ? 0 : (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    int t = (int) Math.round((double) Double.parseDouble(row[tIdx]));

                    // Creating the object and setting the coordinates
                    Obj obj = outputObjects.createAndAddNewObject(type);
                    try {
                        obj.add(x,y,z);
                    } catch (PointOutOfRangeException e) {
                        outOfRangeCount++;
                    }
                    obj.setT(t);
                    outputObjects.add(obj);

                    // Adding parent object, if necessary
                    if (parentObjects != null) {
                        int parentID = (int) Math.round((double) Double.parseDouble(row[parentsIdx]));

                        // Getting parent object.  If it doesn't exist, adding it to the parent objects collection
                        if (!parentObjects.containsKey(parentID))
                            parentObjects.createAndAddNewObject(obj.getVolumeType(),parentID);
                        Obj parentObj = parentObjects.get(parentID);

                        // Adding relationship
                        parentObj.addChild(obj);
                        obj.addParent(parentObj);

                    }

                    writeStatus("Loaded object "+(++count));

                } catch (NumberFormatException e) {}

                // Getting the next row
                row = csvReader.readNext();

            }

            // Closing the readers
            csvReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (outOfRangeCount > 0) MIA.log.writeWarning(outOfRangeCount+" points ignored due to exceeding spatial limits");
    }



    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "Load centroid coordinates of pre-detected objects from file.  Loaded objects are stored in a single object collection and are represented by a single coordinate point.  For example, this module could be used to import detections from another piece of software or from a previous analysis run.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String limitsSource = parameters.getValue(LIMITS_SOURCE);
        String referenceImageName = parameters.getValue(LIMITS_REFERENCE_IMAGE);
        double frameInterval = parameters.getValue(FRAME_INTERVAL);
        String calSource = parameters.getValue(CALIBRATION_SOURCE);
        boolean createParents = parameters.getValue(CREATE_PARENTS);
        String parentType = parameters.getValue(PARENT_TYPE);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME);

        // Getting file to load
        File inputFile = getInputFile(workspace);

        // Getting limits for output objects
        int[] limits = null;
        switch (limitsSource) {
            case LimitsSources.FROM_IMAGE:
                Image image = workspace.getImage(referenceImageName);
                frameInterval = image.getImagePlus().getCalibration().frameInterval;
                limits = getLimitsFromImage(image);
                break;
            case LimitsSources.MANUAL:
                limits = getLimitsFromManualValues();
                break;
            case LimitsSources.MAXIMUM_COORDINATE:
                limits = getLimitsFromMaximumCoordinates(workspace,inputFile);
                break;
        }
        if (limits == null) return Status.FAIL;

        // Getting calibration for output objects
        double[] cal = null;
        switch (calSource) {
            case CalibrationSources.FROM_IMAGE:
                cal = getCalibrationFromImage(workspace,limits);
                break;
            case CalibrationSources.MANUAL:
                cal = getCalibrationFromManualValues();
                break;
        }
        if (cal == null) return Status.FAIL;

        String units = SpatialUnit.getOMEUnit().getSymbol();
        SpatCal calibration = new SpatCal(cal[0],cal[1],units,limits[0],limits[1],limits[2]);

        // Creating output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName,calibration,limits[3],frameInterval,TemporalUnit.getOMEUnit());
        workspace.addObjects(outputObjects);

        // Creating parent objects
        ObjCollection parentObjects = null;
        if (createParents) {
            parentObjects = new ObjCollection(parentObjectsName,calibration,limits[3],frameInterval,TemporalUnit.getOMEUnit());
            workspace.addObjects(parentObjects);
        }

        // Loading objects to the specified collections
        loadObjects(outputObjects,inputFile,parentObjects);

        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(COORDINATE_SEPARATOR,this));
        parameters.add(new ChoiceP(COORDINATE_SOURCE,this,CoordinateSources.CURRENT_FILE,CoordinateSources.ALL));
        parameters.add(new ChoiceP(NAME_FORMAT,this, NameFormats.GENERIC, NameFormats.ALL));
        parameters.add(new StringP(GENERIC_FORMAT,this));
        parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS,this,false));
        parameters.add(new StringP(PREFIX,this));
        parameters.add(new StringP(SUFFIX,this));
        parameters.add(new StringP(EXTENSION,this));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER,this,true));
        parameters.add(new FilePathP(INPUT_FILE,this));

        parameters.add(new SeparatorP(COLUMN_SEPARATOR,this));
        parameters.add(new IntegerP(ID_COLUMN_INDEX,this,0));
        parameters.add(new IntegerP(X_COLUMN_INDEX,this,1));
        parameters.add(new IntegerP(Y_COLUMN_INDEX,this,2));
        parameters.add(new IntegerP(Z_COLUMN_INDEX,this,3));
        parameters.add(new IntegerP(T_COLUMN_INDEX,this,4));

        parameters.add(new SeparatorP(LIMIT_SEPARATOR,this));
        parameters.add(new ChoiceP(LIMITS_SOURCE,this,LimitsSources.FROM_IMAGE,LimitsSources.ALL));
        parameters.add(new InputImageP(LIMITS_REFERENCE_IMAGE,this));
        parameters.add(new IntegerP(WIDTH,this,512));
        parameters.add(new IntegerP(HEIGHT,this,512));
        parameters.add(new IntegerP(N_SLICES,this,1));
        parameters.add(new IntegerP(N_FRAMES,this,1));
        parameters.add(new DoubleP(FRAME_INTERVAL, this, 1d));
        parameters.add(new ChoiceP(CALIBRATION_SOURCE,this,CalibrationSources.FROM_IMAGE,CalibrationSources.ALL));
        parameters.add(new InputImageP(CALIBRATION_REFERENCE_IMAGE,this));
        parameters.add(new DoubleP(XY_CAL, this, 1d));
        parameters.add(new DoubleP(Z_CAL, this, 1d));

        parameters.add(new SeparatorP(RELATIONSHIP_SEPARATOR,this));
        parameters.add(new BooleanP(CREATE_PARENTS,this,false));
        parameters.add(new ChoiceP(PARENT_TYPE,this,ParentTypes.NORMAL,ParentTypes.ALL));
        parameters.add(new OutputObjectsP(PARENT_OBJECTS_NAME,this));
        parameters.add(new IntegerP(PARENTS_COLUMN_INDEX,this,5));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.get(COORDINATE_SEPARATOR));
        returnedParameters.add(parameters.get(COORDINATE_SOURCE));
        switch((String) parameters.getValue(COORDINATE_SOURCE)) {
            case CoordinateSources.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(NAME_FORMAT));
                switch ((String) parameters.getValue(NAME_FORMAT)) {
                    case NameFormats.GENERIC:
                        returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                        returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                        MetadataRefCollection metadataRefs = modules.getMetadataRefs(this);
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
        switch ((String) parameters.getValue(LIMITS_SOURCE)) {
            case LimitsSources.FROM_IMAGE:
                returnedParameters.add(parameters.get(LIMITS_REFERENCE_IMAGE));
                break;
            case LimitsSources.MANUAL:
                returnedParameters.add(parameters.get(WIDTH));
                returnedParameters.add(parameters.get(HEIGHT));
                returnedParameters.add(parameters.get(N_SLICES));
                returnedParameters.add(parameters.get(N_FRAMES));
                returnedParameters.add(parameters.get(FRAME_INTERVAL));
                break;
        }

        returnedParameters.add(parameters.get(CALIBRATION_SOURCE));
        switch ((String) parameters.getValue(CALIBRATION_SOURCE)) {
            case CalibrationSources.FROM_IMAGE:
                returnedParameters.add(parameters.get(CALIBRATION_REFERENCE_IMAGE));
                break;
            case CalibrationSources.MANUAL:
                returnedParameters.add(parameters.get(XY_CAL));
                returnedParameters.add(parameters.get(Z_CAL));
                break;
        }

        returnedParameters.add(parameters.get(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.get(CREATE_PARENTS));
        if ((boolean) parameters.getValue(CREATE_PARENTS)) {
            returnedParameters.add(parameters.get(PARENT_TYPE));
            returnedParameters.add(parameters.get(PARENT_OBJECTS_NAME));
            returnedParameters.add(parameters.get(PARENTS_COLUMN_INDEX));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        if ((boolean) parameters.getValue(CREATE_PARENTS)) {
            String childObjectsName = parameters.getValue(OUTPUT_OBJECTS);
            String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME);

            returnedRelationships.add(parentChildRefs.getOrPut(parentObjectsName,childObjectsName));
        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
      parameters.get(OUTPUT_OBJECTS).setDescription("Objects loaded into the workspace will be stored with this name.  They will be accessible by subsequent modules using this name.");

      parameters.get(COORDINATE_SOURCE).setDescription("Controls where the coordinates for the output object collection will be loaded from:<br><ul>"

      + "<li>\"" + CoordinateSources.CURRENT_FILE
      + "\" (default option) will use the current root-file for the workspace (this is the file specified in the \""+ new InputControl(null).getName() + "\" module).</li>"

      + "<li>\"" + CoordinateSources.MATCHING_FORMAT
      + "\" will load the coordinate file matching a filename based on the root-file for the workspace and a series of rules.</li>"

      + "<li>\"" + CoordinateSources.SPECIFIC_FILE + "\" will load the coordinate file at the location specified by \""+INPUT_FILE+"\".</li></ul>");

      parameters.get(NAME_FORMAT).setDescription("Method to use for generation of the input filename:<br><ul>"

              + "<li>\"" + NameFormats.GENERIC
              + "\" (default) will generate a name from metadata values stored in the current workspace.</li>"

              + "<li>\"" + NameFormats.INPUT_FILE_PREFIX
              + "\" will load a file with the same name as the input image, but with an additional prefix, specified by the \""+PREFIX+"\" parameter.</li>"

              + "<li>\"" + NameFormats.INPUT_FILE_SUFFIX
              + "\" will load a file with the same name as the input image, but with an additional suffix, specified by the \""+SUFFIX+"\" parameter.</li></ul>");

      parameters.get(GENERIC_FORMAT).setDescription("Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

      parameters.get(AVAILABLE_METADATA_FIELDS).setDescription("List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");

      parameters.get(PREFIX).setDescription("Prefix to use when generating coordinate file filename in \""+NameFormats.INPUT_FILE_PREFIX+"\" mode.");

      parameters.get(SUFFIX).setDescription("Suffix to use when generating coordinate file filename in \""+NameFormats.INPUT_FILE_SUFFIX+"\" mode.");

      parameters.get(EXTENSION).setDescription("Extension for the generated filename.");

      parameters.get(INCLUDE_SERIES_NUMBER).setDescription("Option to include the current series number when compiling filenames.  This may be necessary when working with multi-series files, as there will be multiple analyses completed for the same root file.");

      parameters.get(INPUT_FILE).setDescription("Path to specific file to be loaded when \""+COORDINATE_SOURCE+"\" is in \""+CoordinateSources.SPECIFIC_FILE+"\" mode.");

      parameters.get(ID_COLUMN_INDEX).setDescription("Index of column in input coordinates file specifying the object ID number.");

      parameters.get(X_COLUMN_INDEX).setDescription("Index of column in input coordinates file specifying the object x-centroid location (pixel units).");

      parameters.get(Y_COLUMN_INDEX).setDescription("Index of column in input coordinates file specifying the object y-centroid location (pixel units).");

      parameters.get(Z_COLUMN_INDEX).setDescription("Index of column in input coordinates file specifying the object z-centroid location (slice units).");

      parameters.get(T_COLUMN_INDEX).setDescription("Index of column in input coordinates file specifying the timepoint the object appears in.  Timepoint numbering starts at 0.");

      parameters.get(LIMITS_SOURCE).setDescription("Controls how the spatial limits (width, height, number of slices and number of timepoints) for the output object collection are defined:<br><ul>"

      +"<li>\""+LimitsSources.FROM_IMAGE+"\" limits match the spatial and temporal dimensions of an image in the workspace (specified with the \""+LIMITS_REFERENCE_IMAGE+"\" parameter).  This is equivalent to how spatial limits are determined when identifying objects directly from an image.</li>"

      +"<li>\""+LimitsSources.MANUAL+"\" limits are specified using the \""+WIDTH+"\", \""+HEIGHT+"\", \""+N_SLICES+"\" and \""+N_FRAMES+"\" parameters.</li>"

      +"<li>\""+LimitsSources.MAXIMUM_COORDINATE+"\" limits are determined from the maximum x,y,z coordinates and timepoint present in the loaded coordinate set.</li></ul>");

      parameters.get(LIMITS_REFERENCE_IMAGE).setDescription("Image used to determine spatial and temporal limits of the output object collection if \""+LIMITS_SOURCE+"\" is set to \""+LimitsSources.FROM_IMAGE+"\".");

      parameters.get(WIDTH).setDescription("Output object collection spatial width to be used if \""+LIMITS_SOURCE+"\" is set to \""+LimitsSources.MANUAL+"\".  Specified in pixel units.");

      parameters.get(HEIGHT).setDescription("Output object collection spatial height to be used if \""+LIMITS_SOURCE+"\" is set to \""+LimitsSources.MANUAL+"\".  Specified in pixel units.");

      parameters.get(N_SLICES).setDescription("Output object collection number of slices (depth) to be used if \""+LIMITS_SOURCE+"\" is set to \""+LimitsSources.MANUAL+"\".  Specified in slice units.");

      parameters.get(N_FRAMES).setDescription("Output object collection number of frames to be used if \""+LIMITS_SOURCE+"\" is set to \""+LimitsSources.MANUAL+"\".");

      parameters.get(CALIBRATION_SOURCE).setDescription("Controls how the spatial calibration for the output object collection are defined:<br><ul>"

      +"<li>\""+CalibrationSources.FROM_IMAGE+"\" spatial calibrations match those of an image in the workspace (specified with the \""+CALIBRATION_REFERENCE_IMAGE+"\" parameter).  This is equivalent to how calibrations are determined when identifying objects directly from an image.</li>"

      +"<li>\""+CalibrationSources.MANUAL+"\" spatial calibrations are specified using the \""+XY_CAL+"\" and \""+Z_CAL+"\" parameters.</li></ul>");

      parameters.get(CALIBRATION_REFERENCE_IMAGE).setDescription("Image used to determine spatial calibrations of the output object collection if \""+CALIBRATION_SOURCE+"\" is set to \""+CalibrationSources.FROM_IMAGE+"\".");

      parameters.get(XY_CAL).setDescription("Distance per pixel in the XY plane.  Units for this are specified in the main \"Input control\" module.");

      parameters.get(Z_CAL).setDescription("Distance per slice (Z-axis).  Units for this are specified in the main \"Input control\" module.");

      parameters.get(CREATE_PARENTS).setDescription("When selected, an output parent object collection can also be specified which allows objects to be linked.  These parent objectcs can only perform a linking function for the output objects; the parent objects themselves do not contain any coordinate information.  For example, the loaded parent objects could be tracks or clusters.");

      parameters.get(PARENT_TYPE).setDescription("Controls the type of parent objects being used if the \""+CREATE_PARENTS+"\" parameter is selected.  Choices are: "+String.join(", ", ParentTypes.ALL+"."));

      parameters.get(PARENT_OBJECTS_NAME).setDescription("Name of the output parent objects collection.");

      parameters.get(PARENTS_COLUMN_INDEX).setDescription("Index of column in input coordinates file specifying the parent object ID number.");

    }
}
