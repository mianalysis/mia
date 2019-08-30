package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import ij.Prefs;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import wbif.sjx.MIA.Module.Hidden.WorkflowParameters;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ProjectObjects;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MeasureSkeleton extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String ADD_SKELETONS_TO_WORKSPACE = "Add skeletons to workspace";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String ANALYSIS_SEPARATOR = "Analysis settings";
    public static final String MINIMUM_BRANCH_LENGTH = "Minimum branch length";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";


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
        double dppXY = inputObject.getDppXY();

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

    static Image getProjectedImage(Obj inputObject) {
        // Generate projected object
        Obj projectedObj;
        try {
            projectedObj = ProjectObjects.process(inputObject,"Projected",true,false);
        } catch (IntegerOverflowException e) {
            e.printStackTrace();
            return null;
        }
        Image projectedImage = projectedObj.convertObjToImage("Projected");
        InvertIntensity.process(projectedImage);

        // Run skeletonisation
        BinaryOperations2D.process(projectedImage,BinaryOperations2D.OperationModes.SKELETONISE,1);
        InvertIntensity.process(projectedImage);

        return projectedImage;

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
        double minLength = parameters.getValue(MINIMUM_BRANCH_LENGTH);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // If necessary, converting to pixel units
        if (calibratedUnits) minLength = minLength/inputObjects.getFirst().getDppXY();

        ObjCollection outputObjects = null;
        if (addToWorkspace) {
            outputObjects = new ObjCollection(outputObjectsName);
            workspace.addObjects(outputObjects);
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        int nTotal = inputObjects.size();
        AtomicInteger count = new AtomicInteger();

        // For each object, create a child projected object, then generate a binary image.  This is run through ImageJ's
        // skeletonize plugin to ensure it has 4-way connectivity.  Finally, it is processed with the AnalyzeSkeleton
        // plugin.
        for (Obj inputObject:inputObjects.values()) {
            ObjCollection finalOutputObjects = outputObjects;
            double finalMinLength = minLength;
            Runnable task = () -> {
                Image projectedImage = getProjectedImage(inputObject);

                AnalyzeSkeleton_ analyzeSkeleton = new AnalyzeSkeleton_();
                analyzeSkeleton.setup("",projectedImage.getImagePlus());
                SkeletonResult skeletonResult = analyzeSkeleton.run(AnalyzeSkeleton_.NONE,false,false,projectedImage.getImagePlus(),true,false);
                analyzeSkeleton.run(AnalyzeSkeleton_.NONE, finalMinLength,false,projectedImage.getImagePlus(),true,false);

                // Adding the skeleton to the input object
                if (addToWorkspace) {
                    try {
                        Obj outputObject = projectedImage.convertImageToObjects(Image.VolumeTypes.POINTLIST,outputObjectsName).getFirst();
                        outputObject.setID(finalOutputObjects.getAndIncrementID());
                        inputObject.addChild(outputObject);
                        outputObject.addParent(inputObject);
                        finalOutputObjects.add(outputObject);

                    } catch (IntegerOverflowException e) {
                        e.printStackTrace();
                    }
                }

                // Taking the first result for each (in the event there was more than one isolated region)
                addMeasurements(inputObject,skeletonResult);

                writeMessage("Processed " + (count.incrementAndGet()) + " of " + nTotal + " objects");

            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
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
        parameters.add(new ParamSeparatorP(ANALYSIS_SEPARATOR,this));
        parameters.add(new DoubleP(MINIMUM_BRANCH_LENGTH,this,0d));
        parameters.add(new BooleanP(CALIBRATED_UNITS,this,false));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

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

        returnedParameters.add(parameters.getParameter(ANALYSIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_BRANCH_LENGTH));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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

    @Override
    public boolean verify() {
        return true;
    }
}
