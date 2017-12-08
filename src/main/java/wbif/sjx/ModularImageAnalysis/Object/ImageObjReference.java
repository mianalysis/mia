package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 04/12/2017.
 */
public class ImageObjReference extends Reference {
    private LinkedHashSet<MeasurementReference> measurementReferences = new LinkedHashSet<>();

    public void addMeasurementReference(MeasurementReference measurementReference) {
        measurementReferences.add(measurementReference);
    }

    /**
     * Adding all the measurements from another ImageObjReference instance
     * @param inputImageObjReference
     */
    public void addMeasurementReferences(ImageObjReference inputImageObjReference) {
        measurementReferences.addAll(inputImageObjReference.getMeasurementReferences());

    }

    public String[] getActiveMeasurementNames() {
        // Count the number of active measurements
        int activeCount = 0;
        for (MeasurementReference measurementReference:measurementReferences) {
            if (measurementReference.isCalculated()) activeCount++;
        }

        if (activeCount==0) return new String[]{""};

        // Creating a String[] containing the names
        String[] measurementNames = new String[activeCount];
        activeCount = 0;
        for (MeasurementReference measurementReference:measurementReferences) {
            measurementNames[activeCount++] = measurementReference.getName();
        }

        return measurementNames;

    }

    public LinkedHashSet<MeasurementReference> getMeasurementReferences() {
        return measurementReferences;
    }
}
