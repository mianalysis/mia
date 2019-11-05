package wbif.sjx.MIA.Module.InputOutput;


import ij.ImagePlus;
import ij.measure.Calibration;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.units.quantity.Length;
import ome.units.unit.Unit;
import ome.xml.meta.IMetadata;
import util.opencsv.CSVReader;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.Logging.LogRenderer;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.VolumeType;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Arrays;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class ObjectLoader extends Module {
    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String COORDINATE_SEPARATOR = "Coordinate input";
    public static final String COORDINATE_SOURCE = "Coordinate source";
    public static final String INPUT_FILE = "Input file";
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
    public static final String PARENT_OBJECTS_NAME = "Output parent objects name";
    public static final String PARENTS_COLUMN_INDEX = "Parent object ID index";

    public ObjectLoader(ModuleCollection modules) {
        super("Load objects",modules);
    }

    public interface CoordinateSources {
        String CURRENT_FILE = "Current file";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[]{CURRENT_FILE,SPECIFIC_FILE};

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


    File getInputFile(Workspace workspace) {
        String coordinateSource = parameters.getValue(COORDINATE_SOURCE);
        String filePath = parameters.getValue(INPUT_FILE);

        // Getting input file
        File inputFile = null;
        switch (coordinateSource) {
            case CoordinateSources.CURRENT_FILE:
            default:
                return workspace.getMetadata().getFile();
            case CoordinateSources.SPECIFIC_FILE:
                return new File(filePath);
        }
    }

    int[] getLimitsFromImage(Workspace workspace) {
        String referenceImageName = parameters.getValue(LIMITS_REFERENCE_IMAGE);
        Image image = workspace.getImage(referenceImageName);
        ImagePlus ipl = image.getImagePlus();

        return new int[]{ipl.getWidth(),ipl.getHeight(),ipl.getNSlices()};

    }

    int[] getLimitsFromManualValues() {
        int[] limits = new int[3];

        limits[0] = parameters.getValue(WIDTH);
        limits[1] = parameters.getValue(HEIGHT);
        limits[2] = parameters.getValue(N_SLICES);

        return limits;

    }

    int[] getLimitsFromMaximumCoordinates(Workspace workspace, File inputFile) {
        int xIdx = parameters.getValue(X_COLUMN_INDEX);
        int yIdx = parameters.getValue(Y_COLUMN_INDEX);
        int zIdx = parameters.getValue(Z_COLUMN_INDEX);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            MIA.log.writeDebug("File not found: \""+inputFile.getName()+"\"");
            return null;
        }

        // Initialising the limits array
        int[] limits = new int[]{0,0,0};

        // Iterating over each row, adding as a new Obj
        CSVReader csvReader = new CSVReader(reader);
        try {
            String[] row = csvReader.readNext();
            while (row != null) {
                // Parsing row
                int[] thisCoord;
                try {
                    thisCoord = Arrays.stream(row).mapToInt(Integer::parseInt).toArray();
                } catch (NumberFormatException e) {
                    // If this row doesn't contain numbers, skip to the next row
                    row = csvReader.readNext();
                    continue;
                }

                // Comparing the coordinates
                limits[0] = Math.max(limits[0],thisCoord[xIdx]);
                limits[1] = Math.max(limits[1],thisCoord[yIdx]);
                limits[2] = Math.max(limits[2],thisCoord[zIdx]);

                // Getting the next row
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

        String referenceImageName = parameters.getValue(LIMITS_REFERENCE_IMAGE);
        Image image = workspace.getImage(referenceImageName);
        Calibration calibration = image.getImagePlus().getCalibration();

        try {
            String path = workspace.getMetadata().getFile().getAbsolutePath();
            int seriesNumber = workspace.getMetadata().getSeriesNumber() - 1;
            Unit<Length> unit = Units.getOMEUnits();

            // Setting spatial calibration
            ServiceFactory factory = null;
            factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            IMetadata meta = service.createOMEXMLMetadata();

            ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
            reader.setMetadataStore((MetadataStore) meta);
            reader.setGroupFiles(false);
            reader.setId(path);
            reader.setSeries(seriesNumber);

            if (meta.getPixelsPhysicalSizeX(seriesNumber - 1) != null) {
                Length physicalSizeX = meta.getPixelsPhysicalSizeX(seriesNumber - 1);
                if (!unit.isConvertible(physicalSizeX.unit())) {
                    MIA.log.writeWarning("Can't convert units for file \"" + new File(path).getName() + "\".  Spatially calibrated values may be wrong");
                }
                cal[0] = (double) physicalSizeX.value(unit);
            }

            if (limits[2] > 1 && meta.getPixelsPhysicalSizeZ(seriesNumber - 1) != null) {
                Length physicalSizeZ = meta.getPixelsPhysicalSizeZ(seriesNumber - 1);
                cal[1] = (double) physicalSizeZ.value(unit);
            }

        } catch (DependencyException | FormatException | ServiceException | IOException e) {
            e.printStackTrace();
        }

        return cal;

    }

    double[] getCalibrationFromManualValues() {
        double[] cal = new double[2];

        cal[0] = parameters.getValue(XY_CAL);
        cal[1] = parameters.getValue(Z_CAL);

        return cal;

    }

    void loadObjects(ObjCollection outputObjects, File inputFile, int[] limits, double[] cal, @Nullable ObjCollection parentObjects) {
        int idIdx = parameters.getValue(ID_COLUMN_INDEX);
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
        String units = Units.getOMEUnits().getSymbol();
        try {
            CSVReader csvReader = new CSVReader(reader);

            String[] row = csvReader.readNext();
            int count = 1;
            while (row != null) {
                writeMessage("Loading object "+(count++));
                // Parsing row
                int[] thisCoord;
                try {
                     thisCoord = Arrays.stream(row).mapToInt(Integer::parseInt).toArray();
                } catch (NumberFormatException e) {
                    // If this row doesn't contain numbers, skip to the next row
                    row = csvReader.readNext();
                    continue;
                }

                // Creating the object and setting the coordinates
                Obj obj = new Obj(type,outputObjects.getName(),thisCoord[idIdx],limits[0],limits[1],limits[2],cal[0],cal[1],units);
                obj.add(thisCoord[xIdx],thisCoord[yIdx],thisCoord[zIdx]);
                obj.setT(thisCoord[tIdx]);
                outputObjects.add(obj);

                // Adding parent object, if necessary
                if (parentObjects != null) {
                    int parentID = thisCoord[parentsIdx];

                    // Getting parent object.  If it doesn't exist, adding it to the parent objects collection
                    parentObjects.putIfAbsent(parentID,new Obj(parentObjects.getName(),parentID,obj));
                    Obj parentObj = parentObjects.get(parentID);

                    // Adding relationship
                    parentObj.addChild(obj);
                    obj.addParent(parentObj);

                }

                // Getting the next row
                row = csvReader.readNext();

            }

            // Closing the readers
            csvReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (PointOutOfRangeException e) {
            MIA.log.writeWarning(e.getMessage());
        }
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
    public boolean process(Workspace workspace) {
        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String limitsSource = parameters.getValue(LIMITS_SOURCE);
        String calSource = parameters.getValue(CALIBRATION_SOURCE);
        boolean createParents = parameters.getValue(CREATE_PARENTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME);
        int parentsIdx = parameters.getValue(PARENTS_COLUMN_INDEX);

        // Getting file to load
        File inputFile = getInputFile(workspace);

        // Getting limits for output objects
        int[] limits = null;
        switch (limitsSource) {
            case LimitsSources.FROM_IMAGE:
                limits = getLimitsFromImage(workspace);
            case LimitsSources.MANUAL:
                limits = getLimitsFromManualValues();
            case LimitsSources.MAXIMUM_COORDINATE:
                limits = getLimitsFromMaximumCoordinates(workspace,inputFile);
        }
        if (limits == null) return false;

        // Getting calibration for output objects
        double[] cal = null;
        switch (calSource) {
            case CalibrationSources.FROM_IMAGE:
                cal = getCalibrationFromImage(workspace,limits);
            case CalibrationSources.MANUAL:
                cal = getCalibrationFromManualValues();
        }
        if (cal == null) return false;

        // Creating output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);
        workspace.addObjects(outputObjects);

        // Creating parent objects
        ObjCollection parentObjects = null;
        if (createParents) {
            parentObjects = new ObjCollection(parentObjectsName);
            workspace.addObjects(parentObjects);
        }

        // Loading objects to the specified collections
        loadObjects(outputObjects,inputFile,limits,cal,parentObjects);

        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(COORDINATE_SEPARATOR,this));
        parameters.add(new ChoiceP(COORDINATE_SOURCE,this,CoordinateSources.CURRENT_FILE,CoordinateSources.ALL));
        parameters.add(new FilePathP(INPUT_FILE,this));
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
        parameters.add(new OutputObjectsP(PARENT_OBJECTS_NAME,this));
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
            case CoordinateSources.SPECIFIC_FILE:
                returnedParameters.add(parameters.get(INPUT_FILE));
                break;
        }
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
        if (parameters.getValue(CREATE_PARENTS)) {
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
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

        if (parameters.getValue(CREATE_PARENTS)) {
            String childObjectsName = parameters.getValue(OUTPUT_OBJECTS);
            String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME);
            returnedRelationships.add(relationshipRefs.getOrPut(parentObjectsName,childObjectsName));
        }

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
