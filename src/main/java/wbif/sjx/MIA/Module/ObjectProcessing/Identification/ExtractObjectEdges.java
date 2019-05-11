package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImagePlus;
import ij.process.StackStatistics;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import java.util.ArrayList;

/**
 * Created by sc13967 on 01/08/2017.
 */
public class ExtractObjectEdges extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CREATE_EDGE_OBJECTS = "Create edge objects";
    public static final String OUTPUT_EDGE_OBJECTS = "Output edge objects";
    public static final String CREATE_INTERIOR_OBJECTS = "Create interior objects";
    public static final String OUTPUT_INTERIOR_OBJECTS = "Output interior objects";
    public static final String EDGE_MODE = "Edge determination";
    public static final String EDGE_DISTANCE = "Distance";
    public static final String EDGE_PERCENTAGE = "Percentage";

    public ExtractObjectEdges(ModuleCollection modules) {
        super(modules);
    }

    public interface EdgeModes {
        String DISTANCE_FROM_EDGE = "Distance to edge";
        String PERCENTAGE_FROM_EDGE = "Percentage of maximum distance to edge";

        String[] ALL = new String[]{DISTANCE_FROM_EDGE, PERCENTAGE_FROM_EDGE};

    }

    public static Obj getObjectEdge(Obj inputObject, ObjCollection edgeObjects, String edgeMode, double edgeDistance, double edgePercentage) throws IntegerOverflowException {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String calibratedUnits = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();

        // Creating new edge object
        Obj edgeObject = new Obj(edgeObjects.getName(), edgeObjects.getAndIncrementID() ,dppXY,dppZ,calibratedUnits,is2D);

        // Getting parent coordinates
        ArrayList<Integer> parentX = inputObject.getXCoords();
        ArrayList<Integer> parentY = inputObject.getYCoords();
        ArrayList<Integer> parentZ = inputObject.getZCoords();

        // Creating a Hyperstack to hold the distance transform.  The image is padded by 1px to ensure the distance
        // map knows where the object edges are.  If the image is a single plane there is no z-padding.
        double[][] range = inputObject.getExtents(true,false);
        int zPad = 0;
        if (range[2][1] - range[2][0] > 0) zPad = 1;

        ImagePlus iplObj = IJ.createHyperStack("Objects", (int) (range[0][1] - range[0][0] + 3),
                (int) (range[1][1] - range[1][0] + 3), 1, (int) (range[2][1] - range[2][0] + 1 + 2*zPad), 1, 8);

        // Setting pixels corresponding to the parent object to 1
        for (int i = 0; i < parentX.size(); i++) {
            iplObj.setPosition(1, (int) (parentZ.get(i) - range[2][0] + 1 + zPad), 1);
            iplObj.getProcessor().set((int) (parentX.get(i) - range[0][0] + 1), (int) (parentY.get(i) - range[1][0] + 1), 255);
        }

        // Creating distance map using MorphoLibJ
        short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
        DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights, true);
        iplObj.setStack(distTransform.distanceMap(iplObj.getStack()));

        // If percentage is being used, calculate the current value for edgeDistance
        if (edgeMode.equals(EdgeModes.PERCENTAGE_FROM_EDGE)) {
            StackStatistics stackStatistics = new StackStatistics(iplObj);
            double maxDist = stackStatistics.max;
            edgeDistance = (edgePercentage/100)*maxDist;
        }

        // Adding pixel intensities to CumStat
        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i = 0; i < parentX.size(); i++) {
            iplObj.setPosition(1, (int) (parentZ.get(i) - range[2][0] + 2), 1);
            double pixelVal = iplObj.getProcessor().getPixelValue((int) (parentX.get(i) - range[0][0] + 1), (int) (parentY.get(i) - range[1][0] + 1));

            if (pixelVal <= edgeDistance) {
                edgeObject.addCoord(parentX.get(i),parentY.get(i),parentZ.get(i));
                edgeObject.setT(inputObject.getT());
            }
        }

        // If the current object has a size, adding it to the current object collection and assigning relationships
        if (edgeObject.getXCoords() == null) return null;

        return edgeObject;

    }

    public static Obj getInterior(Obj inputObject, ObjCollection interiorObjects, String edgeMode, double edgeDistance, double edgePercentage) throws IntegerOverflowException {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String calibratedUnits = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();

        // Creating new edge object
        Obj interiorObject = new Obj(interiorObjects.getName(),interiorObjects.getAndIncrementID(),dppXY,dppZ,calibratedUnits,is2D);

        // Getting parent coordinates
        ArrayList<Integer> parentX = inputObject.getXCoords();
        ArrayList<Integer> parentY = inputObject.getYCoords();
        ArrayList<Integer> parentZ = inputObject.getZCoords();

        // Creating a Hyperstack to hold the distance transform.  The image is padded by 1px to ensure the distance
        // map knows where the object edges are.  If the image is a single plane there is no z-padding.
        double[][] range = inputObject.getExtents(true,false);
        int zPad = 0;
        if (range[2][1] - range[2][0] > 0) zPad = 1;

        ImagePlus iplObj = IJ.createHyperStack("Objects", (int) (range[0][1] - range[0][0] + 3),
                (int) (range[1][1] - range[1][0] + 3), 1, (int) (range[2][1] - range[2][0] + 1 + 2*zPad), 1, 8);

        // Setting pixels corresponding to the parent object to 1
        for (int i = 0; i < parentX.size(); i++) {
            iplObj.setPosition(1, (int) (parentZ.get(i) - range[2][0] + 1 + zPad), 1);
            iplObj.getProcessor().set((int) (parentX.get(i) - range[0][0] + 1), (int) (parentY.get(i) - range[1][0] + 1), 255);

        }

        // Creating distance map using MorphoLibJ
        short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
        DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights, true);
        iplObj.setStack(distTransform.distanceMap(iplObj.getStack()));

        // If percentage is being used, calculate the current value for edgeDistance
        if (edgeMode.equals(EdgeModes.PERCENTAGE_FROM_EDGE)) {
            StackStatistics stackStatistics = new StackStatistics(iplObj);
            double maxDist = stackStatistics.max;
            edgeDistance = (edgePercentage/100)*maxDist;
        }

        // Adding pixel intensities to CumStat
        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i = 0; i < parentX.size(); i++) {
            iplObj.setPosition(1, (int) (parentZ.get(i) - range[2][0] + 2), 1);
            double pixelVal = iplObj.getProcessor().getPixelValue((int) (parentX.get(i) - range[0][0] + 1), (int) (parentY.get(i) - range[1][0] + 1));

            if (pixelVal > edgeDistance ) {
                interiorObject.addCoord(parentX.get(i),parentY.get(i),parentZ.get(i));
                interiorObject.setT(inputObject.getT());

            }
        }

        // If the current object has a size, adding it to the current object collection and assigning relationships
        if (interiorObject.getXCoords() == null) return null;

        return interiorObject;

    }

    @Override
    public String getTitle() {
        return "Extract object edges";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean createEdgeObjects = parameters.getValue(CREATE_EDGE_OBJECTS);
        boolean createInteriorObjects = parameters.getValue(CREATE_INTERIOR_OBJECTS);
        String edgeMode = parameters.getValue(EDGE_MODE);
        double edgeDistance = parameters.getValue(EDGE_DISTANCE);
        double edgePercentage = parameters.getValue(EDGE_PERCENTAGE);

        // Initialising output edge objects
        String outputEdgeObjectName = null;
        ObjCollection outputEdgeObjects = null;
        if (createEdgeObjects) {
            outputEdgeObjectName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
            outputEdgeObjects = new ObjCollection(outputEdgeObjectName);
        }

        // Initialising output interior objects
        String outputInteriorObjectName = null;
        ObjCollection outputInteriorObjects = null;
        if (createInteriorObjects) {
            outputInteriorObjectName = parameters.getValue(OUTPUT_INTERIOR_OBJECTS);
            outputInteriorObjects = new ObjCollection(outputInteriorObjectName);
        }

        try {
            for (Obj inputObject : inputObjects.values()) {
                if (createEdgeObjects) {
                    Obj outputEdgeObject = getObjectEdge(inputObject, outputEdgeObjects, edgeMode, edgeDistance, edgePercentage);
                    if (outputEdgeObject != null) {
                        outputEdgeObjects.add(outputEdgeObject);
                        outputEdgeObject.addParent(inputObject);
                        inputObject.addChild(outputEdgeObject);
                    }
                }

                if (createInteriorObjects) {
                    Obj outputInteriorObject = getInterior(inputObject, outputInteriorObjects, edgeMode, edgeDistance, edgePercentage);
                    if (outputInteriorObject != null) {
                        outputInteriorObjects.add(outputInteriorObject);
                        outputInteriorObject.addParent(inputObject);
                        inputObject.addChild(outputInteriorObject);
                    }
                }
            }
        } catch (IntegerOverflowException e) {
            return false;
        }

        if (createEdgeObjects) workspace.addObjects(outputEdgeObjects);
        if (createInteriorObjects) workspace.addObjects(outputInteriorObjects);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(CREATE_EDGE_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_EDGE_OBJECTS, this));
        parameters.add(new BooleanP(CREATE_INTERIOR_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_INTERIOR_OBJECTS, this));
        parameters.add(new ChoiceP(EDGE_MODE, this, EdgeModes.DISTANCE_FROM_EDGE, EdgeModes.ALL));
        parameters.add(new DoubleP(EDGE_DISTANCE, this, 1.0));
        parameters.add(new DoubleP(EDGE_PERCENTAGE, this, 1.0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CREATE_EDGE_OBJECTS));
        if (parameters.getValue(CREATE_EDGE_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_EDGE_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(CREATE_INTERIOR_OBJECTS));
        if (parameters.getValue(CREATE_INTERIOR_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_INTERIOR_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(EDGE_MODE));

        if (parameters.getValue(EDGE_MODE).equals(EdgeModes.DISTANCE_FROM_EDGE)) {
            returnedParameters.add(parameters.getParameter(EDGE_DISTANCE));

        } else if (parameters.getValue(EDGE_MODE).equals(EdgeModes.PERCENTAGE_FROM_EDGE)) {
            returnedParameters.add(parameters.getParameter(EDGE_PERCENTAGE));

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return objectMeasurementRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        String inputObjects = parameters.getValue(INPUT_OBJECTS);

        if (parameters.getValue(CREATE_EDGE_OBJECTS)) {
            String outputEdgeObjects = parameters.getValue(OUTPUT_EDGE_OBJECTS);
            relationshipRefs.getOrPut(inputObjects, outputEdgeObjects);
        }

        if (parameters.getValue(CREATE_INTERIOR_OBJECTS)) {
            String outputInteriorObjects = parameters.getValue(OUTPUT_INTERIOR_OBJECTS);
            relationshipRefs.getOrPut(inputObjects,outputInteriorObjects);
        }

        return relationshipRefs;

    }

}
