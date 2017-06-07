package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class RelateObjects extends HCModule {
    public final static String PARENT_OBJECTS = "Parent (larger) objects";
    public final static String CHILD_OBJECTS = "Child (smaller) objects";
    public final static String RELATE_MODE = "Method to relate objects";

    private final static String MATCHING_IDS = "Matching IDs";
    private final static String SPATIAL_OVERLAP = "Spatial overlap";
    private final static String[] RELATE_MODES = new String[]{MATCHING_IDS,SPATIAL_OVERLAP};

    public static void linkMatchingIDs(HCObjectSet parentObjects, HCObjectSet childObjects) {
        for (HCObject parentObject:parentObjects.values()) {
            int ID = parentObject.getID();

            HCObject childObject = childObjects.get(ID);

            if (childObject != null) {
                parentObject.addChild(childObjects.getName(),childObject);
                childObject.setParent(parentObject);

            }
        }
    }

    public static void spatialLinking(HCObjectSet parentObjects, HCObjectSet childObjects) {
        // Runs through each child object against each parent object
        for (HCObject parentObject:parentObjects.values()) {
            // Getting parent coordinates
            ArrayList<Integer> parentX = parentObject.getCoordinates(HCObject.X);
            ArrayList<Integer> parentY = parentObject.getCoordinates(HCObject.Y);
            ArrayList<Integer> parentZ = parentObject.getCoordinates(HCObject.Z);

            // Creating a Hyperstack to hold the distance transform
            int[][] coordinateRange = parentObject.getCoordinateRange();
            ImagePlus ipl = IJ.createHyperStack("Objects", coordinateRange[HCObject.X][1] + 1,
                    coordinateRange[HCObject.Y][1] + 1, 1, coordinateRange[HCObject.Z][1] + 1, 1, 16);

            // Setting pixels corresponding to the parent object to 1
            for (int i=0;i<parentX.size();i++) {
                ipl.setPosition(1,parentZ.get(i),1);
                ipl.getProcessor().set(parentX.get(i),parentY.get(i),1);

            }

            // Creating distance map using MorphoLibJ
            short[] weights = ChamferWeights3D.CITY_BLOCK.getShortWeights();
            DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights,false);
            ImageStack distanceMap = distTransform.distanceMap(ipl.getStack());

            for (HCObject childObject:childObjects.values()) {
                // Only testing if the child is present in the same dimensions as the parent
                HashMap<Integer,Integer> parentPositions = parentObject.getPositions();
                HashMap<Integer,Integer> childPositions = childObject.getPositions();

                boolean matchingDimensions = true;
                for (int dim:parentPositions.keySet()) {
                    if (!parentPositions.get(dim).equals(childPositions.get(dim))) {
                        matchingDimensions = false;
                        break;
                    }
                }
                if (!matchingDimensions) continue;

                // Getting the child centroid location
                ArrayList<Integer> childX = childObject.getCoordinates(HCObject.X);
                ArrayList<Integer> childY = childObject.getCoordinates(HCObject.Y);
                ArrayList<Integer> childZ = childObject.getCoordinates(HCObject.Z);

                int xCent = (int) Math.round(MeasureObjectCentroid.calculateCentroid(childX));
                int yCent = (int) Math.round(MeasureObjectCentroid.calculateCentroid(childY));
                int zCent = (int) Math.round(MeasureObjectCentroid.calculateCentroid(childZ));

                // Testing if the child centroid exists in the object
                for (int i=0;i<parentX.size();i++) {
                    if (parentX.get(i)==xCent & parentY.get(i)==yCent & parentZ.get(i)==zCent) {
                        parentObject.addChild(childObjects.getName(),childObject);
                        childObject.setParent(parentObject);

                        // Getting position within current parent object
                        HCMeasurement absDistanceFromEdge = new HCMeasurement("Distance from parent edge (px)");
                        absDistanceFromEdge.setValue(distanceMap.getVoxel(xCent,yCent,zCent));
                        childObject.addMeasurement(absDistanceFromEdge);

                        break;

                    }
                }
            }
        }
    }

    @Override
    public String getTitle() {
        return "Relate objects";

    }

    @Override
    public String getHelp() {
        return "****Currently distance map (location of children within parents) doesn't take difference in XY and Z calibration into account***";
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        HCName parentObjectName = parameters.getValue(PARENT_OBJECTS);
        HCObjectSet parentObjects = workspace.getObjects().get(parentObjectName);

        HCName childObjectName = parameters.getValue(CHILD_OBJECTS);
        HCObjectSet childObjects = workspace.getObjects().get(childObjectName);

        // Getting parameters
        String relateMode = parameters.getValue(RELATE_MODE);

        if (relateMode.equals(MATCHING_IDS)) {
            if (verbose) System.out.println("["+moduleName+"] Relating objects by matching ID numbers");
            linkMatchingIDs(parentObjects,childObjects);

        } else if (relateMode.equals(SPATIAL_OVERLAP)) {
            if (verbose) System.out.println("["+moduleName+"] Relating objects by spatial overlap");
            spatialLinking(parentObjects,childObjects);

        }

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(PARENT_OBJECTS, HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(CHILD_OBJECTS, HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(RELATE_MODE, HCParameter.CHOICE_ARRAY,RELATE_MODES[0],RELATE_MODES));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {
        measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS),"Distance from parent edge (px)");

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(PARENT_OBJECTS),parameters.getValue(CHILD_OBJECTS));

    }
}

