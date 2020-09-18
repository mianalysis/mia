package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.PartnerObjectsP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.MathFunc.CumStat;

public class CalculateStatsForPartners extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PARTNER_OBJECTS = "Partner objects";

    public static final String STATISTIC_SEPARATOR = "Statistics";
    public static final String MEASUREMENT = "Measurement";
    public static final String CALCULATE_MEAN = "Calculate mean";
    public static final String CALCULATE_STD = "Calculate standard deviation";
    public static final String CALCULATE_MIN = "Calculate minimum";
    public static final String CALCULATE_MAX = "Calculate maximum";
    public static final String CALCULATE_SUM = "Calculate sum";

    public CalculateStatsForPartners(ModuleCollection modules) {
        super("Calculate statistics for partners", modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String STD = "STD";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";

    }

    public static String getFullName(String partnerObjectName, String measurement, String measurementType) {
        return "PARTNER_STATS // "+measurementType+"_"+partnerObjectName+"_\""+measurement+"\"";
    }

    public static void processObject(Obj inputObject, String partnerObjectsName, String measurement, boolean[] statsToCalculate) {
        ObjCollection partnerObjects = inputObject.getPartners(partnerObjectsName);

        // Calculating statistics for measurement
        CumStat cs = new CumStat();
        if (partnerObjects != null) {
            for (Obj partnerObject : partnerObjects.values()) {
                // Check the measurement exists
                if (partnerObject.getMeasurement(measurement) == null) continue;

                if (partnerObject.getMeasurement(measurement).getValue() != Double.NaN) {
                    cs.addMeasure(partnerObject.getMeasurement(measurement).getValue());
                }
            }
        }

        if (statsToCalculate[0]) {
            String name = getFullName(partnerObjectsName,measurement,Measurements.MEAN);
            inputObject.addMeasurement(new Measurement(name, cs.getMean()));
        }

        if (statsToCalculate[1]) {
            String name = getFullName(partnerObjectsName,measurement,Measurements.STD);
            inputObject.addMeasurement(new Measurement(name, cs.getStd()));
        }

        if (statsToCalculate[2]) {
            String name = getFullName(partnerObjectsName,measurement,Measurements.MIN);
            inputObject.addMeasurement(new Measurement(name, cs.getMin()));
        }

        if (statsToCalculate[3]) {
            String name = getFullName(partnerObjectsName,measurement,Measurements.MAX);
            inputObject.addMeasurement(new Measurement(name, cs.getMax()));
        }

        if (statsToCalculate[4]) {
            String name = getFullName(partnerObjectsName,measurement,Measurements.SUM);
            inputObject.addMeasurement(new Measurement(name, cs.getSum()));
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
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting other parameters
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);
        String measurement = parameters.getValue(MEASUREMENT);
        boolean[] statsToCalculate = new boolean[5];
        statsToCalculate[0] = parameters.getValue(CALCULATE_MEAN);
        statsToCalculate[1] = parameters.getValue(CALCULATE_STD);
        statsToCalculate[2] = parameters.getValue(CALCULATE_MIN);
        statsToCalculate[3] = parameters.getValue(CALCULATE_MAX);
        statsToCalculate[4] = parameters.getValue(CALCULATE_SUM);

        for (Obj inputObject:inputObjects.values()) {
            processObject(inputObject,partnerObjectsName,measurement,statsToCalculate);
        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS, this));
        
        parameters.add(new ParamSeparatorP(STATISTIC_SEPARATOR, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT,this));
        parameters.add(new BooleanP(CALCULATE_MEAN,this,true));
        parameters.add(new BooleanP(CALCULATE_STD,this,true));
        parameters.add(new BooleanP(CALCULATE_MIN,this,true));
        parameters.add(new BooleanP(CALCULATE_MAX,this,true));
        parameters.add(new BooleanP(CALCULATE_SUM,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ((PartnerObjectsP) parameters.getParameter(PARTNER_OBJECTS)).setPartnerObjectsName(objectName);

        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(partnerObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);
        String measurementName = parameters.getValue(MEASUREMENT);

        if ((boolean) parameters.getValue(CALCULATE_MEAN)) {
            String name = getFullName(partnerObjectsName,measurementName,Measurements.MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean value of measurement, \"" +measurementName+"\", for partner objects, \""+
                    partnerObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_STD)) {
            String name = getFullName(partnerObjectsName,measurementName,Measurements.STD);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation of measurement, \"" +measurementName+"\", for partner objects, \""+
                    partnerObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MIN)) {
            String name = getFullName(partnerObjectsName,measurementName,Measurements.MIN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Minimum value of measurement, \"" +measurementName+"\", for partner objects, \""+
                    partnerObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MAX)) {
            String name = getFullName(partnerObjectsName,measurementName,Measurements.MAX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Maximum value of measurement, \"" +measurementName+"\", for partner objects, \""+
                    partnerObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_SUM)) {
            String name = getFullName(partnerObjectsName,measurementName,Measurements.SUM);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Summed value of measurement, \"" +measurementName+"\", for partner objects, \""+
                    partnerObjectsName+"\".");
            returnedRefs.add(reference);
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
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
