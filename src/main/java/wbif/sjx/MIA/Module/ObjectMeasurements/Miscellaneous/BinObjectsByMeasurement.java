package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class BinObjectsByMeasurement extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MEASUREMENT = "Measurement";
    public static final String SMALLEST_BIN_CENTRE = "Smallest bin centre";
    public static final String LARGEST_BIN_CENTRE = "Largest bin centre";
    public static final String NUMBER_OF_BINS = "Number of bins";

    public BinObjectsByMeasurement(ModuleCollection modules) {
        super("Bin objects by measurement",modules);
    }

    interface Measurements {
        String BIN = "Bin";
    }

    public static String getFullName(String measurement) {
        return "BIN // "+measurement;
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

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return Status.PASS;

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
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String measurement = parameters.getValue(MEASUREMENT);

        String name = getFullName(measurement);
        ObjMeasurementRef binMeasurement = objectMeasurementRefs.getOrPut(name);
        binMeasurement.setObjectsName(inputObjectsName);
        returnedRefs.add(binMeasurement);

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
