package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Applies manual object classifications from a .csv file at the specified location.  Each row of the file must
 * correspond to a different object and have the format [ID],[Classification]
 */
public class ApplyManualClassification extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLASSIFICATION_FILE = "Classification file";
    public static final String REMOVE_MISSING = "Remove objects without classification";

    @Override
    public String getTitle() {
        return "Apply manual classification";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

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
                    double xCent = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(Obj.X));
                    double yCent = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(Obj.Y));
                    if (xCent==x & yCent==y & object.getCoordinates(Obj.T).equals(f)) {
                        MIAMeasurement objClass = new MIAMeasurement(MIAMeasurement.CLASS,currClass);
                        objClass.setSource(this);
                        object.addMeasurement(objClass);

                        break;
                    }
                }
            }

            // Removing objects that don't have an assigned class (first removing the parent-child relationships).
            // Otherwise, the class measurement is set to Double.NaN
            if (parameters.getValue(REMOVE_MISSING)) {
                for (Obj object : inputObjects.values()) {
                    if (object.getMeasurement(MIAMeasurement.CLASS) == null) {
                        object.removeRelationships();
                    }
                }
                inputObjects.entrySet().removeIf(entry -> entry.getValue().getMeasurement(MIAMeasurement.CLASS) == null);

            } else {
                for (Obj object : inputObjects.values()) {
                    if (object.getMeasurement(MIAMeasurement.CLASS) == null) {
                        MIAMeasurement objClass = new MIAMeasurement(MIAMeasurement.CLASS,Double.NaN);
                        objClass.setSource(this);
                        object.addMeasurement(objClass);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CLASSIFICATION_FILE, Parameter.FILE_PATH,null));
        parameters.addParameter(new Parameter(REMOVE_MISSING, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CLASSIFICATION_FILE));
        returnedParameters.addParameter(parameters.getParameter(REMOVE_MISSING));

        return returnedParameters;

    }

    /**
     * Adds measurements from the current module to the measurement collection
     */
    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS), MIAMeasurement.CLASS);
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
