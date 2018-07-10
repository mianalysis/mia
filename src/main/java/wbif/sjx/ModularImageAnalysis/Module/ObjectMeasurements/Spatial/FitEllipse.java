package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import ij.ImagePlus;
import ij.ImageStack;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Analysis.EllipseCalculator;
import wbif.sjx.common.Analysis.EllipsoidCalculator;
import wbif.sjx.common.Object.Volume;

/**
 * Created by sc13967 on 19/06/2018.
 */
public class FitEllipse extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String FITTING_METHOD = "Fitting method";
    public static final String OBJECT_OUTPUT_MODE = "Object output mode";
    public static final String OUTPUT_OBJECTS = "Output objects";


    public interface OutputModes {
        String DO_NOT_STORE = "Do not store";
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[]{DO_NOT_STORE,CREATE_NEW_OBJECT,UPDATE_INPUT};

    }

    public interface Measurements {
        String ELLIPSE_THETA = "ELLIPSE // ORIENTATION_(DEGS)";
        String ELLIPSE_SEMI_MAJOR = "ELLIPSE // SEMI_MAJOR_(PX)";
        String ELLIPSE_SEMI_MINOR = "ELLIPSE // SEMI_MINOR_(PX)";

    }


    public void processObject(Obj inputObject, ObjCollection outputObjects, String objectOutputMode, Image templateImage) {
        ImagePlus templateImagePlus = templateImage.getImagePlus();
        templateImagePlus.setPosition(1,1,inputObject.getT());
        ImageStack imageStack = templateImagePlus.getStack();

        EllipseCalculator calculator = new EllipseCalculator(inputObject);
        addMeasurements(inputObject,calculator);
        getContainedPoints(inputObject,calculator);

//        Volume ellipsoid = calculator.getContainedPoints();

//        switch (objectOutputMode) {
//            case OutputModes.CREATE_NEW_OBJECT:
//                Obj ellipsoidObject = createNewObject(inputObject,ellipsoid,outputObjects);
//                if (ellipsoidObject != null) {
//                    outputObjects.add(ellipsoidObject);
//                    ellipsoidObject.cropToImageSize(templateImage);
//                }
//                break;
//            case OutputModes.UPDATE_INPUT:
//                updateInputObject(inputObject,ellipsoid);
//                inputObject.cropToImageSize(templateImage);
//                break;
//        }
    }

    public void getContainedPoints(Obj inputObject, EllipseCalculator calculator) {
        double[][] extents = inputObject.getExtents2D(true);

        double[] e2d = calculator.getEllipseFit();
        double x0 = calculator.getXCentre();
        double y0 = calculator.getYCentre();
        double a = calculator.getSemiMajorAxis();
        double b = calculator.getSemiMinorAxis();
        System.out.println(x0+"_"+y0+"_"+a+"_"+b+"_"+e2d[0]+"_"+e2d[1]+"_"+e2d[2]);

        Volume vol = new Volume(inputObject.getDistPerPxXY(),inputObject.getDistPerPxZ(),inputObject.getCalibratedUnits(),true);
        for (int x=(int) extents[0][0];x<extents[0][1];x++) {
            for (int y=(int) extents[1][0];y<extents[1][1];y++) {
//                System.out.println(x+"_"+y+"_"+((x-x0)*(x-x0)/(a*a))+((y-y0)*(y-y0)/(b*b)));
                if (((x-x0)*(x-x0)/(a*a))+((y-y0)*(y-y0)/(b*b)) < 1) vol.addCoord(x,y,0);
            }
        }

        inputObject.setPoints(vol.getPoints());

    }

    public Obj createNewObject (Obj inputObject, Volume ellipsoid, ObjCollection outputObjects) {
        if (ellipsoid == null) return null;

        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String units = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();

        Obj ellipsoidObject = new Obj(outputObjects.getName(),outputObjects.getNextID(),dppXY,dppZ,units,is2D);
        ellipsoidObject.setPoints(ellipsoid.getPoints());

        ellipsoidObject.addParent(inputObject);
        inputObject.addChild(ellipsoidObject);
        outputObjects.add(ellipsoidObject);

        return ellipsoidObject;

    }

    public void updateInputObject(Obj inputObject, Volume ellipsoid) {
        inputObject.setPoints(ellipsoid.getPoints());
    }

    public void addMeasurements(Obj inputObject, EllipseCalculator calculator) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();

//        double[] centres = calculator.getCentroid();
//        inputObject.addMeasurement(new Measurement(Measurements.X_CENT_PX,centres[0]));
//        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.X_CENT_CAL),centres[0]*dppXY));
//        inputObject.addMeasurement(new Measurement(Measurements.Y_CENT_PX,centres[1]));
//        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Y_CENT_CAL),centres[1]*dppXY));
//        inputObject.addMeasurement(new Measurement(Measurements.Z_CENT_SLICE,centres[2]*dppXY/dppZ));
//        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Z_CENT_CAL),centres[2]*dppZ));

    }


    @Override
    public String getTitle() {
        return "Fit ellipse";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);

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
            processObject(inputObject,outputObjects,objectOutputMode,templateImage);
            writeMessage("Processed object "+(++count)+" of "+nTotal);
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(TEMPLATE_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OBJECT_OUTPUT_MODE,Parameter.CHOICE_ARRAY, OutputModes.DO_NOT_STORE, OutputModes.ALL));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,""));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TEMPLATE_IMAGE));

        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

//        MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.X_CENT_PX);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);


        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
                relationships.addRelationship(inputObjectsName,outputObjectsName);

                break;
        }
    }
}
