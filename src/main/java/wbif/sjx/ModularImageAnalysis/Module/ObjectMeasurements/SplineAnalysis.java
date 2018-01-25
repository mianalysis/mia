package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.SkeletonTools.PruneSkeleton;

/**
 * Created by sc13967 on 24/01/2018.
 */
public class SplineAnalysis extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";

    @Override
    public String getTitle() {
        return "Spline analysis";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Converting object to image, then inverting, so we have a black object on a white background
        ImagePlus objectIpl = inputObjects.values().iterator().next().getAsImage("Object").getImagePlus();

        InvertIntensity.process(objectIpl);

        // Skeletonise fish to get single backbone
        BinaryOperations.applyBinaryTransform(objectIpl,BinaryOperations.OperationModes.SKELETONISE_2D,1,0);
        objectIpl.show();

        // NEED TO REMOVE ALL BRANCHES, BUT THOSE THAT GIVE THE LONGEST END-TO-END PATH

        // Converting the skeleton to a linear sequence of coordinates


    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

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
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
