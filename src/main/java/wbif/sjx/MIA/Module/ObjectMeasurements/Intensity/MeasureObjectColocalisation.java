package wbif.sjx.MIA.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Analysis.ColocalisationCalculator;

public class MeasureObjectColocalisation extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String COLOC_SEPARATOR = "Images to measure";
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";

    public MeasureObjectColocalisation(ModuleCollection modules) {
        super("Measure object colocalisation",modules);
    }


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
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
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

        if (showOutput) objects.showMeasurements(this,workspace.getAnalysis().getModules());

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(COLOC_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE_1, this));
        parameters.add(new InputImageP(INPUT_IMAGE_2, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName1 = parameters.getValue(INPUT_IMAGE_1);
        String inputImageName2 = parameters.getValue(INPUT_IMAGE_2);

        String name = getFullName(inputImageName1,inputImageName2,Measurements.PCC);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Pearson's Correlation Coefficient (PCC) calculated separately for pixels contained " +
                "within each \""+inputObjectsName+"\" object between images \""+inputImageName1+"\" and \""+
                inputImageName2+"\".  PCC values range from -1 to +1, where -1 corresponds to perfect anti-correlation " +
                "of signal and +1 to perfect correlation.");
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
