package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Analysis.ColocalisationCalculator;

public class MeasureObjectColocalisation extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";


    public interface Measurements {
        String PCC = "PCC";
    }

    public static String getFullName(String imageName1,String imageName2, String measurement) {
        return "COLOCALISATION // "+imageName1+"_"+imageName2+"_"+measurement;
    }

    public static void measurePCC(Obj inputObject, Image image1, Image image2) {
        ImagePlus ipl1 = image1.getImagePlus();
        ImagePlus ipl2 = image2.getImagePlus();

        ipl1.setPosition(1,1,inputObject.getT()+1);
        ipl2.setPosition(1,1,inputObject.getT()+1);
        double pcc = ColocalisationCalculator.calculatePCC(ipl1.getImageStack(),ipl2.getImageStack(),inputObject);

        inputObject.addMeasurement(new Measurement(getFullName(image1.getName(),image2.getName(),Measurements.PCC),pcc));

    }


    @Override
    public String getTitle() {
        return "Measure object colocalisation";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection objects = workspace.getObjects().get(objectName);

        // Getting input images
        String imageName1 = parameters.getValue(INPUT_IMAGE_1);
        Image image1 = workspace.getImages().get(imageName1);

        String imageName2 = parameters.getValue(INPUT_IMAGE_2);
        Image image2 = workspace.getImages().get(imageName2);

        // Iterating over each object, taking the measurements
        for (Obj inputObject:objects.values()) {
            measurePCC(inputObject,image1,image2);
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_IMAGE_1, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_IMAGE_2, Parameter.INPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName1 = parameters.getValue(INPUT_IMAGE_1);
        String inputImageName2 = parameters.getValue(INPUT_IMAGE_2);

        objectMeasurementReferences.setAllCalculated(false);

        String name = getFullName(inputImageName1,inputImageName2,Measurements.PCC);
        MeasurementReference reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
