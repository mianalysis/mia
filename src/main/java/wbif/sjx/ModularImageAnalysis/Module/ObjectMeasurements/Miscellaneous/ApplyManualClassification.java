package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.FilePathP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputObjectsP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Applies manual object classifications from a .csv file at the specified location.  Each row of the file must
 * correspond to a different object and have the format [ID],[Classification]
 */
public class ApplyManualClassification extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLASSIFICATION_FILE = "Classification file";
    public static final String REMOVE_MISSING = "Remove objects without classification";

    public interface Measurements {
        String CLASS = "CLASSIFIER // CLASS";

    }

    @Override
    public String getTitle() {
        return "Apply manual classification";

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
    public boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting classification file and storing classifications as HashMap that can be easily read later on
        String classificationFilePath = parameters.getValue(CLASSIFICATION_FILE);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(classificationFilePath));
            String line;
            while((line=bufferedReader.readLine())!=null){
                // Getting current object value
                String vals[] = line.split(",");
                int x = Integer.valueOf(vals[0]);
                int y = Integer.valueOf(vals[1]);
                int f = Integer.valueOf(vals[2]);
                int currClass = Integer.valueOf(vals[3]);

                for (Obj object:inputObjects.values()) {
                    double xCent = object.getXMean(true);
                    double yCent = object.getYMean(true);
                    int timepoint = object.getT();

                    if (xCent==x & yCent==y & timepoint == f) {
                        Measurement objClass = new Measurement(Measurements.CLASS,currClass);
                        objClass.setSource(this);
                        object.addMeasurement(objClass);

                        break;
                    }
                }
            }

            bufferedReader.close();

            // Removing objects that don't have an assigned class (first removing the parent-child relationships).
            // Otherwise, the class measurement is set to Double.NaN
            if (parameters.getValue(REMOVE_MISSING)) {
                for (Obj object : inputObjects.values()) {
                    if (object.getMeasurement(Measurements.CLASS) == null) {
                        object.removeRelationships();
                    }
                }
                inputObjects.entrySet().removeIf(entry -> entry.getValue().getMeasurement(Measurements.CLASS) == null);

            } else {
                for (Obj object : inputObjects.values()) {
                    if (object.getMeasurement(Measurements.CLASS) == null) {
                        Measurement objClass = new Measurement(Measurements.CLASS,Double.NaN);
                        objClass.setSource(this);
                        object.addMeasurement(objClass);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (showOutput) inputObjects.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new FilePathP(CLASSIFICATION_FILE, this));
        parameters.add(new BooleanP(REMOVE_MISSING, this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CLASSIFICATION_FILE));
        returnedParameters.add(parameters.getParameter(REMOVE_MISSING));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllCalculated(false);

        MeasurementRef classMeas = objectMeasurementRefs.getOrPut(Measurements.CLASS);
        classMeas.setImageObjName(parameters.getValue(INPUT_OBJECTS));
        classMeas.setCalculated(true);

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
