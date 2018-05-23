package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

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
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
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
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(SMALLEST_BIN_CENTRE,Parameter.DOUBLE,0d));
        parameters.add(new Parameter(LARGEST_BIN_CENTRE,Parameter.DOUBLE,1d));
        parameters.add(new Parameter(NUMBER_OF_BINS,Parameter.INTEGER,1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        parameters.updateValueSource(MEASUREMENT,inputObjectsName);

        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
