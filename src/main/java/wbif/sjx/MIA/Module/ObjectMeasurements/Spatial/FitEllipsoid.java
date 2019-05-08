package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import ij.ImagePlus;
import ij.ImageStack;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ExtractObjectEdges;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Analysis.EllipsoidCalculator;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume;

/**
 * Created by sc13967 on 19/06/2018.
 */
public class FitEllipsoid extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String FITTING_MODE = "Fitting mode";
    public static final String USE_INTENSITY_WEIGHTING = "Use intensity weighting";
    public static final String OBJECT_OUTPUT_MODE = "Object output mode";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String LIMIT_AXIS_LENGTH = "Limit axis length";
    public static final String MAXIMUM_AXIS_LENGTH = "Maximum axis length";


    public interface FittingModes {
        String FIT_TO_WHOLE = "Fit to whole";
        String FIT_TO_SURFACE = "Fit to surface";

        String[] ALL = new String[]{FIT_TO_SURFACE,FIT_TO_WHOLE};

    }

    public interface OutputModes {
        String DO_NOT_STORE = "Do not store";
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[]{DO_NOT_STORE,CREATE_NEW_OBJECT,UPDATE_INPUT};

    }

    public interface Measurements {
        String X_CENT_PX = "ELLIPSOID // X_CENTROID (PX)";
        String X_CENT_CAL = "ELLIPSOID // X_CENTROID (${CAL})";
        String Y_CENT_PX = "ELLIPSOID // Y_CENTROID (PX)";
        String Y_CENT_CAL = "ELLIPSOID // Y_CENTROID (${CAL})";
        String Z_CENT_SLICE = "ELLIPSOID // Z_CENTROID (SLICE)";
        String Z_CENT_CAL = "ELLIPSOID // Z_CENTROID (${CAL})";
        String RADIUS_1_PX = "ELLIPSOID // RADIUS_1 (PX)";
        String RADIUS_1_CAL = "ELLIPSOID // RADIUS_1 (${CAL})";
        String RADIUS_2_PX = "ELLIPSOID // RADIUS_2 (PX)";
        String RADIUS_2_CAL = "ELLIPSOID // RADIUS_2 (${CAL})";
        String RADIUS_3_PX = "ELLIPSOID // RADIUS_3 (PX)";
        String RADIUS_3_CAL = "ELLIPSOID // RADIUS_3 (${CAL})";
        String SURFACE_AREA_PX = "ELLIPSOID // SURFACE_AREA (PX^2)";
        String SURFACE_AREA_CAL = "ELLIPSOID // SURFACE_AREA (${CAL}^2)";
        String VOLUME_PX = "ELLIPSOID // VOLUME (PX^3)";
        String VOLUME_CAL = "ELLIPSOID // VOLUME (${CAL}^3)";
        String ORIENTATION_1 = "ELLIPSOID // ORIENTATION_1 (DEGS)";
        String ORIENTATION_2 = "ELLIPSOID // ORIENTATION_2 (DEGS)";
        String SPHERICITY = "ELLIPSOID // SPHERICITY";

    }


    public void processObject(Obj inputObject, ObjCollection outputObjects, String objectOutputMode, Image templateImage,
                              String fittingMode, boolean useIntensityWeighting, double maxAxisLength) throws IntegerOverflowException {
        ImagePlus templateImagePlus = templateImage.getImagePlus();
        templateImagePlus.setPosition(1,1,inputObject.getT());
        ImageStack imageStack = templateImagePlus.getStack();

        EllipsoidCalculator calculator = null;
        switch (fittingMode) {
            case FittingModes.FIT_TO_WHOLE:
                calculator = useIntensityWeighting
                        ? new EllipsoidCalculator(inputObject, maxAxisLength, imageStack)
                        : new EllipsoidCalculator(inputObject, maxAxisLength);
                break;

            case FittingModes.FIT_TO_SURFACE:
                ObjCollection edgeObjects = new ObjCollection("Edge");
                String edgeMode = ExtractObjectEdges.EdgeModes.DISTANCE_FROM_EDGE;
                Obj edgeObject = ExtractObjectEdges.getObjectEdge(inputObject,edgeObjects,edgeMode,1.0,1.0);
                calculator = useIntensityWeighting
                        ? new EllipsoidCalculator(edgeObject, maxAxisLength, imageStack)
                        : new EllipsoidCalculator(edgeObject, maxAxisLength);
                break;
        }

        if (calculator == null || calculator.getRadii() == null) return;

        addMeasurements(inputObject,calculator);

        Volume ellipsoid = calculator.getContainedPoints();
        if (ellipsoid.getNVoxels() == 0) return;

        switch (objectOutputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                Obj ellipsoidObject = createNewObject(inputObject,ellipsoid,outputObjects);
                if (ellipsoidObject != null) {
                    outputObjects.add(ellipsoidObject);
                    ellipsoidObject.cropToImageSize(templateImage);
                }
                break;
            case OutputModes.UPDATE_INPUT:
                updateInputObject(inputObject,ellipsoid);
                inputObject.cropToImageSize(templateImage);
                break;
        }
    }

    public Obj createNewObject (Obj inputObject, Volume ellipsoid, ObjCollection outputObjects) {
        if (ellipsoid == null) return null;

        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String units = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();

        Obj ellipsoidObject = new Obj(outputObjects.getName(),outputObjects.getAndIncrementID(),dppXY,dppZ,units,is2D);
        ellipsoidObject.setPoints(ellipsoid.getPoints());
        ellipsoidObject.setT(inputObject.getT());

        ellipsoidObject.addParent(inputObject);
        inputObject.addChild(ellipsoidObject);
        outputObjects.add(ellipsoidObject);

        return ellipsoidObject;

    }

    public void updateInputObject(Obj inputObject, Volume ellipsoid) {
        inputObject.setPoints(ellipsoid.getPoints());
    }

    public void addMeasurements(Obj inputObject, EllipsoidCalculator calculator) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();

        double[] centres = calculator.getCentroid();
        inputObject.addMeasurement(new Measurement(Measurements.X_CENT_PX,centres[0]));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.X_CENT_CAL),centres[0]*dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.Y_CENT_PX,centres[1]));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Y_CENT_CAL),centres[1]*dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.Z_CENT_SLICE,centres[2]*dppXY/dppZ));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Z_CENT_CAL),centres[2]*dppZ));

        double[] radii = calculator.getRadii();
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_1_PX,radii[0]));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.RADIUS_1_CAL),radii[0]*dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_2_PX,radii[1]));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.RADIUS_2_CAL),radii[1]*dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_3_PX,radii[2]));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.RADIUS_3_CAL),radii[2]*dppXY));

        double surfaceArea = calculator.getSurfaceArea();
        inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_PX,surfaceArea));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SURFACE_AREA_CAL),surfaceArea*dppXY*dppXY));

        double volume = calculator.getVolume();
        inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX,volume));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.VOLUME_CAL),volume*dppXY*dppXY*dppXY));

        double[] orientations = calculator.getOrientationRads();
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_1,Math.toDegrees(orientations[0])));
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_2,Math.toDegrees(orientations[1])));

        double sphericity = calculator.getSphericity();
        inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY,sphericity));

    }


    @Override
    public String getTitle() {
        return "Fit ellipsoid";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        String fittingMode = parameters.getValue(FITTING_MODE);
        boolean useIntensityWeighting = parameters.getValue(USE_INTENSITY_WEIGHTING);
        boolean limitAxisLength = parameters.getValue(LIMIT_AXIS_LENGTH);
        double maxAxisLength = limitAxisLength ? parameters.getValue(MAXIMUM_AXIS_LENGTH) : Double.MAX_VALUE;

        // If necessary, creating a new ObjCollection and adding it to the Workspace
        ObjCollection outputObjects = null;
        if (objectOutputMode.equals(OutputModes.CREATE_NEW_OBJECT)) {
            outputObjects = new ObjCollection(outputObjectsName);
            workspace.addObjects(outputObjects);
        }

        // Getting template image
        Image templateImage = workspace.getImage(templateImageName);

        // Running through each object, taking measurements and adding new object to the workspace where necessary
        int count = 0;
        int nTotal = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            try {
                processObject(inputObject,outputObjects,objectOutputMode,templateImage,fittingMode,useIntensityWeighting,maxAxisLength);
            } catch (IntegerOverflowException e) {
                return false;
            }
            writeMessage("Processed object "+(++count)+" of "+nTotal);
        }

        if (showOutput) inputObjects.showMeasurements(this,workspace.getAnalysis().getModules());

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(TEMPLATE_IMAGE,this));
        parameters.add(new ChoiceP(FITTING_MODE,this,FittingModes.FIT_TO_SURFACE,FittingModes.ALL));
        parameters.add(new BooleanP(USE_INTENSITY_WEIGHTING,this,false));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE,this, OutputModes.DO_NOT_STORE, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));
        parameters.add(new BooleanP(LIMIT_AXIS_LENGTH,this,false));
        parameters.add(new DoubleP(MAXIMUM_AXIS_LENGTH,this,1000d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TEMPLATE_IMAGE));
        returnedParameters.add(parameters.getParameter(FITTING_MODE));
        returnedParameters.add(parameters.getParameter(USE_INTENSITY_WEIGHTING));

        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(LIMIT_AXIS_LENGTH));
        if (parameters.getValue(LIMIT_AXIS_LENGTH)) returnedParameters.add(parameters.getParameter(MAXIMUM_AXIS_LENGTH));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        objectMeasurementRefs.setAllAvailable(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.X_CENT_PX);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.X_CENT_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENT_PX);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.Y_CENT_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z_CENT_SLICE);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.Z_CENT_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_1_PX);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.RADIUS_1_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_2_PX);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.RADIUS_2_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_3_PX);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.RADIUS_3_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.SURFACE_AREA_PX);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.SURFACE_AREA_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.VOLUME_PX);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.VOLUME_CAL));
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION_1);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION_2);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementRefs.getOrPut(Measurements.SPHERICITY);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection relationships = new RelationshipRefCollection();
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
                relationships.addRelationship(inputObjectsName,outputObjectsName);

                break;
        }

        return relationships;

    }

}
