package wbif.sjx.HighContent.Module;

import wbif.sjx.HighContent.Object.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

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
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);

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

                for (HCObject object:inputObjects.values()) {
                    double xCent = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(HCObject.X));
                    double yCent = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(HCObject.Y));
                    if (xCent==x & yCent==y & object.getCoordinates(HCObject.T).equals(f)) {
                        HCMeasurement objClass = new HCMeasurement(HCMeasurement.CLASS,currClass);
                        objClass.setSource(this);
                        object.addMeasurement(objClass);

                        break;
                    }
                }
            }

            // Removing objects that don't have an assigned class (first removing the parent-child relationships).
            // Otherwise, the class measurement is set to Double.NaN
            if (parameters.getValue(REMOVE_MISSING)) {
                for (HCObject object : inputObjects.values()) {
                    if (object.getMeasurement(HCMeasurement.CLASS) == null) {
                        object.removeRelationships(inputObjectsName);
                    }
                }
                inputObjects.entrySet().removeIf(entry -> entry.getValue().getMeasurement(HCMeasurement.CLASS) == null);

            } else {
                for (HCObject object : inputObjects.values()) {
                    if (object.getMeasurement(HCMeasurement.CLASS) == null) {
                        HCMeasurement objClass = new HCMeasurement(HCMeasurement.CLASS,Double.NaN);
                        objClass.setSource(this);
                        object.addMeasurement(objClass);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(CLASSIFICATION_FILE,HCParameter.FILE_PATH,null));
        parameters.addParameter(new HCParameter(REMOVE_MISSING,HCParameter.BOOLEAN,false));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CLASSIFICATION_FILE));
        returnedParameters.addParameter(parameters.getParameter(REMOVE_MISSING));

        return returnedParameters;

    }

    /**
     * Adds measurements from the current module to the measurement collection
     */
    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),HCMeasurement.CLASS);
    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
