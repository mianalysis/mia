package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


/**
* Distribute objects into a set of "bins" based on a measurement associated with each object.  Bins are evenly distributed between manually-specified smallest and largest bin centres.  The assigned bin for each object is stored as a new measurement associated with that object.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class BinObjectsByMeasurement extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* Objects from the workspace.  The specified measurement for each of these will be binned according to various parameters.  The assigned bin will be stored as a new measurement associated with this object.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String BIN_SEPARATOR = "Binning controls";

	/**
	* Measurement associated with the input objects that will be binned according to the other parameters.
	*/
    public static final String MEASUREMENT = "Measurement";

	/**
	* Centre value associated with the smallest bin.  Bins will be evenly distributed in bins between this value and the upper bin centre (specified by "Largest bin centre".
	*/
    public static final String SMALLEST_BIN_CENTRE = "Smallest bin centre";

	/**
	* Centre value associated with the largest bin.  Bins will be evenly distributed in bins between this value and the lower bin centre (specified by "Smallest bin centre".
	*/
    public static final String LARGEST_BIN_CENTRE = "Largest bin centre";

	/**
	* Number of bins to divide measurements into.  These will be evenly distributed in the range between "Smallest bin centre" and "Largest bin centre".
	*/
    public static final String NUMBER_OF_BINS = "Number of bins";

    public BinObjectsByMeasurement(Modules modules) {
        super("Bin objects by measurement", modules);
    }

    interface Measurements {
        String BIN = "Bin";
    }

    public static String getFullName(String measurement) {
        return "BIN // " + measurement;
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Distribute objects into a set of \"bins\" based on a measurement associated with each object.  Bins are evenly distributed between manually-specified smallest and largest bin centres.  The assigned bin for each object is stored as a new measurement associated with that object.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS,workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectName);

        // Getting parameters
        String measurementName = parameters.getValue(MEASUREMENT,workspace);
        double smallestBin = parameters.getValue(SMALLEST_BIN_CENTRE,workspace);
        double largestBin = parameters.getValue(LARGEST_BIN_CENTRE,workspace);
        int numberOfBins = parameters.getValue(NUMBER_OF_BINS,workspace);

        double binWidth = (largestBin - smallestBin) / (numberOfBins - 1);

        int count = 0;
        int total = inputObjects.size();
        for (ObjI inputObject : inputObjects.values()) {
            Measurement measurement = inputObject.getMeasurement(measurementName);
            if (measurement == null) {
                inputObject.addMeasurement(new Measurement(getFullName(measurementName), Double.NaN));
                continue;
            }

            double value = measurement.getValue();
            double bin = Math.round((value - smallestBin) / binWidth) * binWidth + smallestBin;

            // Ensuring the bin is within the specified range
            bin = Math.min(bin, largestBin);
            bin = Math.max(bin, smallestBin);

            inputObject.addMeasurement(new Measurement(getFullName(measurementName), bin));

            writeProgressStatus(++count, total, "objects");

        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(BIN_SEPARATOR, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new DoubleP(SMALLEST_BIN_CENTRE, this, 0d));
        parameters.add(new DoubleP(LARGEST_BIN_CENTRE, this, 1d));
        parameters.add(new IntegerP(NUMBER_OF_BINS, this, 1));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String measurement = parameters.getValue(MEASUREMENT,workspace);

        String name = getFullName(measurement);
        ObjMeasurementRef binMeasurement = objectMeasurementRefs.getOrPut(name);
        binMeasurement.setObjectsName(inputObjectsName);
        returnedRefs.add(binMeasurement);

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
	return null; 
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from the workspace.  The specified measurement for each of these will be binned according to various parameters.  The assigned bin will be stored as a new measurement associated with this object.");

        parameters.get(MEASUREMENT).setDescription(
                "Measurement associated with the input objects that will be binned according to the other parameters.");

        parameters.get(SMALLEST_BIN_CENTRE).setDescription(
                "Centre value associated with the smallest bin.  Bins will be evenly distributed in bins between this value and the upper bin centre (specified by \""
                        + LARGEST_BIN_CENTRE + "\".");

        parameters.get(LARGEST_BIN_CENTRE).setDescription(
                "Centre value associated with the largest bin.  Bins will be evenly distributed in bins between this value and the lower bin centre (specified by \""
                        + SMALLEST_BIN_CENTRE + "\".");

        parameters.get(NUMBER_OF_BINS).setDescription(
                "Number of bins to divide measurements into.  These will be evenly distributed in the range between \""
                        + SMALLEST_BIN_CENTRE + "\" and \"" + LARGEST_BIN_CENTRE + "\".");

    }
}
