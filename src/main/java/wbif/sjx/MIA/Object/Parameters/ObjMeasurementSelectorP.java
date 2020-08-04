package wbif.sjx.MIA.Object.Parameters;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.lang.annotations.NotNull;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.RefSelectorParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;

/**
 * Created by Stephen Cross on 18/02/2020.
 */
public class ObjMeasurementSelectorP extends Parameter {
    private String objectName = "";
    private TreeMap<String,Boolean> measurementStates = new TreeMap<>();

    public ObjMeasurementSelectorP(String name, Module module) {
        super(name, module);
    }

    public ObjMeasurementSelectorP(String name, Module module, @NotNull String objectName) {
        super(name, module);
        this.objectName = objectName;
    }

    public ObjMeasurementSelectorP(String name, Module module, @NotNull String objectName, String description) {
        super(name, module, description);
        this.objectName = objectName;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new RefSelectorParameter(this);
    }

    @Override
    public <T> T getValue() {
        validateStates();
        return (T) measurementStates;
    }

    @Override
    public <T> void setValue(T value) {
        measurementStates = (TreeMap<String,Boolean>) value;
    }

    @Override
    public String getRawStringValue() {
        validateStates();

        StringBuilder builder = new StringBuilder();

        for (String measurementName:measurementStates.keySet()) {
            builder.append("[NAME:")
                    .append(measurementName)
                    .append(",STATE:")
                    .append(measurementStates.get(measurementName))
                    .append("]");
        }

        return builder.toString();

    }

    @Override
    public void setValueFromString(String string) {
        Pattern pattern = Pattern.compile("\\[NAME:(.+?),STATE:(.+?)]");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            measurementStates.put(matcher.group(1),Boolean.valueOf(matcher.group(2)));
        }
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        return null;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * Iterate over all measurements.  If they're not present in the current object, remove them.  If they're
     * missing, add them.
     */
    public void validateStates() {
        // Getting list of all available measurements for selected object.
        ObjMeasurementRefCollection refs = module.getModules().getObjectMeasurementRefs(objectName);

        // If no object is assigned, clear the state collection
        if (refs == null) {
            measurementStates = new TreeMap<>();
            return;
        }

        // Iterate over all measurement states and check their availability
        measurementStates.keySet().removeIf(measurementName -> !refs.keySet().contains(measurementName));

        // Iterate over all measurements and add any that are missing
        for (ObjMeasurementRef ref:refs.values()) {
            measurementStates.putIfAbsent(ref.getName(),true);
        }
    }

    public TreeMap<String, Boolean> getMeasurementStates() {
        validateStates();
        return measurementStates;
    }

    public void setMeasurementStates(TreeMap<String, Boolean> measurementStates) {
        this.measurementStates = measurementStates;
    }
}
