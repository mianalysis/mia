package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import ij.ImagePlus;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.IdentifyObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ProjectObjects;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

public class MeasureSkeleton extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String ADD_SKELETONS_TO_WORKSPACE = "Add skeletons to workspace";
    public static final String OUTPUT_OBJECTS = "Output objects";


    public interface Measurements {
        String nBranches = "SKELETON // NUM_BRANCHES";
        String nJunctions = "SKELETON // NUM_JUNCTIONS";
        String nEndPoints = "SKELETON // NUM_END_POINT_VOXELS";
        String nJunctionVoxels = "SKELETON // NUM_JUNCTION_VOXELS";
        String nSlabVoxels = "SKELETON // NUM_SLAB_VOXELS";
        String nTriplePoints = "SKELETON // NUM_TRIPLE_POINTS";
        String nQuadruplePoints = "SKELETON // NUM_QUADRUPLE_POINTS";
        String avBranchLengthPx = "SKELETON // AV_BRANCH_LENGTH (PX)";
        String avBranchLengthCal = "SKELETON // AV_BRANCH_LENGTH (${CAL})";
        String maxBranchLengthPx = "SKELETON // MAX_BRANCH_LENGTH (PX)";
        String maxBranchLengthCal = "SKELETON // MAX_BRANCH_LENGTH (${CAL})";

    }


    public MeasureSkeleton(ModuleCollection modules) {
        super("Measure skeleton", modules);
    }

    static void addMeasurements(Obj inputObject, SkeletonResult skeletonResult) {
        double dppXY = inputObject.getDistPerPxXY();

        inputObject.addMeasurement(new Measurement(Measurements.nBranches,skeletonResult.getBranches()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nJunctions,skeletonResult.getJunctions()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nEndPoints,skeletonResult.getEndPoints()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nJunctionVoxels,skeletonResult.getJunctionVoxels()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nSlabVoxels,skeletonResult.getSlabs()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nTriplePoints,skeletonResult.getTriples()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nQuadruplePoints,skeletonResult.getQuadruples()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.avBranchLengthPx,skeletonResult.getAverageBranchLength()[0]/dppXY));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.avBranchLengthCal),skeletonResult.getAverageBranchLength()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.maxBranchLengthPx,skeletonResult.getMaximumBranchLength()[0]/dppXY));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.maxBranchLengthCal),skeletonResult.getMaximumBranchLength()[0]));

    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        boolean addToWorkspace = parameters.getValue(ADD_SKELETONS_TO_WORKSPACE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjCollection outputObjects = null;
        if (addToWorkspace) {
            outputObjects = new ObjCollection(outputObjectsName);
            workspace.addObjects(outputObjects);
        }

        // For each object, create a child projected object, then generate a binary image.  This is run through ImageJ's
        // skeletonize plugin to ensure it has 4-way connectivity.  Finally, it is processed with the AnalyzeSkeleton
        // plugin.
        for (Obj inputObject:inputObjects.values()) {
            // Generate projected object
            Obj projectedObj;
            try {
                projectedObj = ProjectObjects.process(inputObject,"Projected",true);
            } catch (IntegerOverflowException e) {
                e.printStackTrace();
                continue;
            }
            Image projectedImage = projectedObj.convertObjToImage("Projected");
            InvertIntensity.process(projectedImage);

            // Run skeletonisation
            BinaryOperations2D.process(projectedImage,BinaryOperations2D.OperationModes.SKELETONISE,1);
            InvertIntensity.process(projectedImage);

            // Running skeleton analysis
            AnalyzeSkeleton_ analyzeSkeleton = new AnalyzeSkeleton_();
            analyzeSkeleton.setup("",projectedImage.getImagePlus());
            SkeletonResult skeletonResult = analyzeSkeleton.run(AnalyzeSkeleton_.NONE,false,false,projectedImage.getImagePlus(),true,false);

            // Adding the skeleton to the input object
            if (addToWorkspace) {
                try {
                    Obj outputObject = projectedImage.convertImageToObjects(outputObjectsName).getFirst();
                    outputObject.setID(outputObjects.getAndIncrementID());
                    inputObject.addChild(outputObject);
                    outputObject.addParent(inputObject);
                    outputObjects.add(outputObject);

                } catch (IntegerOverflowException e) {
                    e.printStackTrace();
                }
            }

            // Taking the first result for each (in the event there was more than one isolated region)
            addMeasurements(inputObject,skeletonResult);

        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new BooleanP(ADD_SKELETONS_TO_WORKSPACE,this,false));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_SKELETONS_TO_WORKSPACE));

        if (parameters.getValue(ADD_SKELETONS_TO_WORKSPACE)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.nBranches);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nJunctions);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nEndPoints);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nJunctionVoxels);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nSlabVoxels);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nTriplePoints);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nQuadruplePoints);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.avBranchLengthPx);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Units.replace(Measurements.avBranchLengthCal));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.maxBranchLengthPx);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Units.replace(Measurements.maxBranchLengthCal));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRefs = new RelationshipRefCollection();

        String parentObjectsName = parameters.getValue(INPUT_OBJECTS);
        String childObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        returnedRefs.add(relationshipRefs.getOrPut(parentObjectsName,childObjectsName));

        return returnedRefs;

    }

    @Override
    public String getDescription() {
        return "Uses the AnalyzeSkeleton plugin by Ignacio Arganda-Carreras (https://imagej.net/AnalyzeSkeleton).";
    }
}
