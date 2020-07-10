package wbif.sjx.MIA.Module.InputOutput;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.annotation.Nullable;

import ij.ImagePlus;
import ij.measure.Calibration;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import util.opencsv.CSVReader;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputClusterObjectsP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputTrackObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
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
    public static final String CALIBRATION_SOURCE = "Calibration source";
    public static final String CALIBRATION_REFERENCE_IMAGE = "Calibration reference image";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship controls";
    public static final String CREATE_PARENTS = "Create parent objects";
    public static final String PARENT_TYPE = "Parent objects type";
    public static final String PARENT_CLUSTERS_NAME = "Output parent clusters name";
    public static final String PARENT_OBJECTS_NAME = "Output parent objects name";
    public static final String PARENT_TRACKS_NAME = "Output parent tracks name";
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

    int[] getLimitsFromImage(Workspace workspace) {
        String referenceImageName = parameters.getValue(LIMITS_REFERENCE_IMAGE);
        Image image = workspace.getImage(referenceImageName);
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
                        parentObjects.putIfAbsent(parentID,new Obj(parentObjects.getName(),parentID,obj));
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
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String limitsSource = parameters.getValue(LIMITS_SOURCE);
        String calSource = parameters.getValue(CALIBRATION_SOURCE);
        boolean createParents = parameters.getValue(CREATE_PARENTS);
        String parentType = parameters.getValue(PARENT_TYPE);

        String parentObjectsName = null;
        switch (parentType) {
            case ParentTypes.CLUSTER:
                parentObjectsName = parameters.getValue(PARENT_CLUSTERS_NAME);
                break;
            case ParentTypes.NORMAL:
                parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME);
                break;
            case ParentTypes.TRACK:
                parentObjectsName = parameters.getValue(PARENT_TRACKS_NAME);
                break;
        }

        // Getting file to load
        File inputFile = getInputFile(workspace);

        // Getting limits for output objects
        int[] limits = null;
        switch (limitsSource) {
            case LimitsSources.FROM_IMAGE:
                limits = getLimitsFromImage(workspace);
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

        String units = Units.getOMEUnits().getSymbol();
        SpatCal calibration = new SpatCal(cal[0],cal[1],units,limits[0],limits[1],limits[2]);

        // Creating output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName,calibration,limits[3]);
        workspace.addObjects(outputObjects);

        // Creating parent objects
        ObjCollection parentObjects = null;
        if (createParents) {
            parentObjects = new ObjCollection(parentObjectsName,calibration,limits[3]);
            workspace.addObjects(parentObjects);
        }

        // Loading objects to the specified collections
        loadObjects(outputObjects,inputFile,parentObjects);

        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(COORDINATE_SEPARATOR,this));
        parameters.add(new ChoiceP(COORDINATE_SOURCE,this,CoordinateSources.CURRENT_FILE,CoordinateSources.ALL));
        parameters.add(new ChoiceP(NAME_FORMAT,this, NameFormats.GENERIC, NameFormats.ALL));
        parameters.add(new StringP(GENERIC_FORMAT,this));
        parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS,this,false));
        parameters.add(new StringP(PREFIX,this));
        parameters.add(new StringP(SUFFIX,this));
        parameters.add(new StringP(EXTENSION,this));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER,this,true));
        parameters.add(new FilePathP(INPUT_FILE,this));

        parameters.add(new ParamSeparatorP(COLUMN_SEPARATOR,this));
        parameters.add(new IntegerP(ID_COLUMN_INDEX,this,0));
        parameters.add(new IntegerP(X_COLUMN_INDEX,this,1));
        parameters.add(new IntegerP(Y_COLUMN_INDEX,this,2));
        parameters.add(new IntegerP(Z_COLUMN_INDEX,this,3));
        parameters.add(new IntegerP(T_COLUMN_INDEX,this,4));

        parameters.add(new ParamSeparatorP(LIMIT_SEPARATOR,this));
        parameters.add(new ChoiceP(LIMITS_SOURCE,this,LimitsSources.FROM_IMAGE,LimitsSources.ALL));
        parameters.add(new InputImageP(LIMITS_REFERENCE_IMAGE,this));
        parameters.add(new IntegerP(WIDTH,this,512));
        parameters.add(new IntegerP(HEIGHT,this,512));
        parameters.add(new IntegerP(N_SLICES,this,1));
        parameters.add(new IntegerP(N_FRAMES,this,1));
        parameters.add(new ChoiceP(CALIBRATION_SOURCE,this,CalibrationSources.FROM_IMAGE,CalibrationSources.ALL));
        parameters.add(new InputImageP(CALIBRATION_REFERENCE_IMAGE,this));
        parameters.add(new DoubleP(XY_CAL, this, 1d, "Distance per pixel in the XY plane.  Units for this are specified in the main \"Input control\" module."));
        parameters.add(new DoubleP(Z_CAL, this, 1d, "Distance per slice (Z-axis).  Units for this are specified in the main \"Input control\" module."));

        parameters.add(new ParamSeparatorP(RELATIONSHIP_SEPARATOR,this));
        parameters.add(new BooleanP(CREATE_PARENTS,this,false));
        parameters.add(new ChoiceP(PARENT_TYPE,this,ParentTypes.NORMAL,ParentTypes.ALL));
        parameters.add(new OutputClusterObjectsP(PARENT_CLUSTERS_NAME,this));
        parameters.add(new OutputObjectsP(PARENT_OBJECTS_NAME,this));
        parameters.add(new OutputTrackObjectsP(PARENT_TRACKS_NAME,this));
        parameters.add(new IntegerP(PARENTS_COLUMN_INDEX,this,5));

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
            switch ((String) parameters.getValue(PARENT_TYPE)) {
                case ParentTypes.CLUSTER:
                    returnedParameters.add(parameters.get(PARENT_CLUSTERS_NAME));
                    break;
                case ParentTypes.NORMAL:
                    returnedParameters.add(parameters.get(PARENT_OBJECTS_NAME));
                    break;
                case ParentTypes.TRACK:
                    returnedParameters.add(parameters.get(PARENT_TRACKS_NAME));
                    break;
            }
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
            String parentObjectsName = null;
            switch ((String) parameters.getValue(PARENT_TYPE)) {
                case ParentTypes.CLUSTER:
                    parentObjectsName = parameters.getValue(PARENT_CLUSTERS_NAME);
                    break;
                case ParentTypes.NORMAL:
                    parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME);
                    break;
                case ParentTypes.TRACK:
                    parentObjectsName = parameters.getValue(PARENT_TRACKS_NAME);
                    break;
            }
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
}
