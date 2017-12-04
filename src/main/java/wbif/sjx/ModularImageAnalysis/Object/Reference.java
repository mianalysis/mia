package wbif.sjx.ModularImageAnalysis.Object;

import java.util.HashSet;

/**
 * Created by sc13967 on 04/12/2017.
 */
public class Reference {
    private String name = "";
    private HashSet<MeasurementReference> measurementReferences = new HashSet<>();

    public void addMeasurementReference(MeasurementReference measurementReference) {
        measurementReferences.add(measurementReference);
    }

    /**
     * Adding all the measurements from another Reference instance
     * @param inputReference
     */
    public void addMeasurementReferences(Reference inputReference) {
        measurementReferences.addAll(inputReference.getMeasurementReferences());

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
            measurementNames[activeCount++] = measurementReference.getMeasurementName();
        }

        return measurementNames;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<MeasurementReference> getMeasurementReferences() {
        return measurementReferences;
    }
}
