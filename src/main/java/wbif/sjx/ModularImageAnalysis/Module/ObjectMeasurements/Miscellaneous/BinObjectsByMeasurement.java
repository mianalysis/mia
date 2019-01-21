package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

public class BinObjectsByMeasurement extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MEASUREMENT = "Measurement";
    public static final String SMALLEST_BIN_CENTRE = "Smallest bin centre";
    public static final String LARGEST_BIN_CENTRE = "Largest bin centre";
    public static final String NUMBER_OF_BINS = "Number of bins";

    interface Measurements {
        String BIN = "Bin";
    }

    public static String getFullName(String measurement) {
        return "BIN // "+measurement;
    }

    @Override
    public String getTitle() {
        return "Bin objects by measurement";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting parameters
        String measurementName = parameters.getValue(MEASUREMENT);
        double smallestBin = parameters.getValue(SMALLEST_BIN_CENTRE);
        double largestBin = parameters.getValue(LARGEST_BIN_CENTRE);
        int numberOfBins = parameters.getValue(NUMBER_OF_BINS);

        double binWidth = (largestBin-smallestBin)/(numberOfBins-1);

        for (Obj inputObject:inputObjects.values()) {
            double measurement = inputObject.getMeasurement(measurementName).getValue();
            double bin = Math.round((measurement-smallestBin)/binWidth)*binWidth+smallestBin;

            // Ensuring the bin is within the specified range
            bin = Math.min(bin,largestBin);
            bin = Math.max(bin,smallestBin);

            inputObject.addMeasurement(new Measurement(getFullName(measurementName),bin));

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT,this));
        parameters.add(new DoubleP(SMALLEST_BIN_CENTRE,this,0d));
        parameters.add(new DoubleP(LARGEST_BIN_CENTRE,this,1d));
        parameters.add(new IntegerP(NUMBER_OF_BINS,this,1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String measurement = parameters.getValue(MEASUREMENT);

        String name = getFullName(measurement);
        MeasurementReference binMeasurement = objectMeasurementReferences.getOrPut(name);
        binMeasurement.setImageObjName(inputObjectsName);
        binMeasurement.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
