package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputObjectsP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
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
        double pcc = ColocalisationCalculator.calculatePCC(ipl1.getStack(),ipl2.getStack(),inputObject);

        inputObject.addMeasurement(new Measurement(getFullName(image1.getName(),image2.getName(),Measurements.PCC),pcc));

    }


    @Override
    public String getTitle() {
        return "Measure object colocalisation";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
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

        if (showOutput) objects.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE_1, this));
        parameters.add(new InputImageP(INPUT_IMAGE_2, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName1 = parameters.getValue(INPUT_IMAGE_1);
        String inputImageName2 = parameters.getValue(INPUT_IMAGE_2);

        objectMeasurementRefs.setAllCalculated(false);

        String name = getFullName(inputImageName1,inputImageName2,Measurements.PCC);
        MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);
        reference.setDescription("Pearson's Correlation Coefficient (PCC) calculated separately for pixels contained " +
                "within each \""+inputObjectsName+"\" object between images \""+inputImageName1+"\" and \""+
                inputImageName2+"\".  PCC values range from -1 to +1, where -1 corresponds to perfect anti-correlation " +
                "of signal and +1 to perfect correlation.");

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
