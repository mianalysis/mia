package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.MathFunc.CumStat;

public class CalculateStatsForChildren extends Module {
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String CHILD_OBJECTS = "Child objects";
    public static final String MEASUREMENT = "Measurement";
    public static final String CALCULATE_MEAN = "Calculate mean";
    public static final String CALCULATE_STD = "Calculate standard deviation";
    public static final String CALCULATE_MIN = "Calculate minimum";
    public static final String CALCULATE_MAX = "Calculate maximum";
    public static final String CALCULATE_SUM = "Calculate sum";

    public CalculateStatsForChildren(ModuleCollection modules) {
        super("Calculate statistics for children",modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String STD = "STD";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";

    }

    public static String getFullName(String childObjectName, String measurement, String measurementType) {
        return "CHILD_STATS // "+measurementType+"_"+childObjectName+"_\""+measurement+"\"";
    }

    public static void processObject(Obj parentObject, String childObjectsName, String measurement, boolean[] statsToCalculate) {
        ObjCollection childObjects = parentObject.getChildren(childObjectsName);

        // Calculating statistics for measurement
        CumStat cs = new CumStat();
        if (childObjects != null) {
            for (Obj childObject : childObjects.values()) {
                // Check the measurement exists
                if (childObject.getMeasurement(measurement) == null) continue;

                if (childObject.getMeasurement(measurement).getValue() != Double.NaN) {
                    cs.addMeasure(childObject.getMeasurement(measurement).getValue());
                }
            }
        }

        if (statsToCalculate[0]) {
            String name = getFullName(childObjectsName,measurement,Measurements.MEAN);
            parentObject.addMeasurement(new Measurement(name, cs.getMean()));
        }

        if (statsToCalculate[1]) {
            String name = getFullName(childObjectsName,measurement,Measurements.STD);
            parentObject.addMeasurement(new Measurement(name, cs.getStd()));
        }

        if (statsToCalculate[2]) {
            String name = getFullName(childObjectsName,measurement,Measurements.MIN);
            parentObject.addMeasurement(new Measurement(name, cs.getMin()));
        }

        if (statsToCalculate[3]) {
            String name = getFullName(childObjectsName,measurement,Measurements.MAX);
            parentObject.addMeasurement(new Measurement(name, cs.getMax()));
        }

        if (statsToCalculate[4]) {
            String name = getFullName(childObjectsName,measurement,Measurements.SUM);
            parentObject.addMeasurement(new Measurement(name, cs.getSum()));
        }
    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectsName);

        // Getting other parameters
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String measurement = parameters.getValue(MEASUREMENT);
        boolean[] statsToCalculate = new boolean[5];
        statsToCalculate[0] = parameters.getValue(CALCULATE_MEAN);
        statsToCalculate[1] = parameters.getValue(CALCULATE_STD);
        statsToCalculate[2] = parameters.getValue(CALCULATE_MIN);
        statsToCalculate[3] = parameters.getValue(CALCULATE_MAX);
        statsToCalculate[4] = parameters.getValue(CALCULATE_SUM);

        for (Obj parentObject:parentObjects.values()) {
            processObject(parentObject,childObjectsName,measurement,statsToCalculate);
        }

        if (showOutput) parentObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(PARENT_OBJECTS,this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS,this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT,this));
        parameters.add(new BooleanP(CALCULATE_MEAN,this,true));
        parameters.add(new BooleanP(CALCULATE_STD,this,true));
        parameters.add(new BooleanP(CALCULATE_MIN,this,true));
        parameters.add(new BooleanP(CALCULATE_MAX,this,true));
        parameters.add(new BooleanP(CALCULATE_SUM,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        returnedParameters.add(parameters.getParameter(CALCULATE_MEAN));
        returnedParameters.add(parameters.getParameter(CALCULATE_STD));
        returnedParameters.add(parameters.getParameter(CALCULATE_MIN));
        returnedParameters.add(parameters.getParameter(CALCULATE_MAX));
        returnedParameters.add(parameters.getParameter(CALCULATE_SUM));

        String objectName = parameters.getValue(PARENT_OBJECTS);
        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(objectName);

        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(childObjectsName);

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String measurementName = parameters.getValue(MEASUREMENT);

        if ((boolean) parameters.getValue(CALCULATE_MEAN)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Mean value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_STD)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.STD);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Standard deviation of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MIN)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.MIN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Minimum value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MAX)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.MAX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Maximum value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_SUM)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.SUM);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Summed value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
