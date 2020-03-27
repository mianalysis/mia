package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.Workspace;

public class ReplaceMeasurementValue extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MEASUREMENT = "Measurement";
    public static final String VALUE_TO_REPLACE = "Value to replace";
    public static final String REPLACEMENT_VALUE = "Replacement value";


    public ReplaceMeasurementValue(ModuleCollection modules) {
        super("Replace measurement value", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        String measurementName = parameters.getValue(MEASUREMENT);
        String valueToReplace = parameters.getValue(VALUE_TO_REPLACE);
        String replacementValue = parameters.getValue(REPLACEMENT_VALUE);

        // Converting value Strings to double
        double inputValue = (valueToReplace.equals("NaN")) ? Double.NaN : Double.parseDouble(valueToReplace);
        double outputValue = (replacementValue.equals("NaN")) ? Double.NaN : Double.parseDouble(replacementValue);

        for (Obj inputObject:inputObjects.values()) {
            Measurement measurement = inputObject.getMeasurement(measurementName);
            if (measurement == null) continue;

            double currentValue = measurement.getValue();
            if ((Double.isNaN(currentValue) && Double.isNaN(inputValue)) || currentValue == inputValue) measurement.setValue(outputValue);

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT,this));
        parameters.add(new StringP(VALUE_TO_REPLACE,this,"NaN"));
        parameters.add(new StringP(REPLACEMENT_VALUE,this,"0"));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjectMeasurementP measurementParameter = parameters.getParameter(MEASUREMENT);
        measurementParameter.setObjectName(inputObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return "";
    }
}
