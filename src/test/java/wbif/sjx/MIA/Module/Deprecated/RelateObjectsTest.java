package wbif.sjx.MIA.Module.Deprecated;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wbif.sjx.MIA.ExpectedObjects.*;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen Cross on 10/09/2017.
 */
public class RelateObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RelateObjects(null).getDescription());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testLinkMatchingIDsOneChild(VolumeType volumeType){
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new Spots3D(volumeType).getObjects(inputSpotsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.MATCHING_IDS);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Checking the workspace contains two object sets
        assertEquals(2,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());
        assertNotNull(workspace.getObjectSet(inputSpotsName));
        assertEquals(25,workspace.getObjectSet(inputSpotsName).size());

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:testObjects.values()) {
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

            // Testing the number of children
            assertNotNull(childSpots);
            assertEquals(1,childSpots.size());

            // Testing spot for parent
            Obj childSpot = childSpots.values().iterator().next();
            assertNotNull(childSpot.getParent(inputObjectsName));

            // Testing spot is at expected location
            int nPoints = testObject.getPoints().size();
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_SPOT_ID_X.name()).getValue();
            double actual = childSpot.getX(true)[0];
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_SPOT_ID_Y.name()).getValue();
            actual = childSpot.getY(true)[0];
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_SPOT_ID_Z.name()).getValue();
            actual = childSpot.getZ(true,false)[0];
            assertEquals(expected,actual,tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidLink(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new Spots3D(volumeType).getObjects(inputSpotsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = new Objects3D(volumeType).getOtherValues();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_X.name());
            int[] expectedY = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_Y.name());
            int[] expectedZ = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_Z.name());
            double[] expectedDist = (double[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_DIST.name());

            // Getting child objects (those linked here)
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

            // Each object won't necessarily have any children
            if (childSpots == null) continue;

            // Testing the number of children
            assertEquals(expectedX.length,childSpots.size());

            // Putting actual values into arrays
            int[] actualX = new int[childSpots.size()];
            int[] actualY = new int[childSpots.size()];
            int[] actualZ = new int[childSpots.size()];
            double[] actualDist = new double[childSpots.size()];

            int iter = 0;
            for (Obj childSpot:childSpots.values()) {
                actualX[iter] = (int) childSpot.getX(true)[0];
                actualY[iter] = (int) childSpot.getY(true)[0];
                actualZ[iter] = (int) childSpot.getZ(true,false)[0];
                String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,inputObjectsName);
                actualDist[iter++] = childSpot.getMeasurement(name).getValue();

            }

            // Sorting arrays
            Arrays.sort(expectedX);
            Arrays.sort(expectedY);
            Arrays.sort(expectedZ);
            Arrays.sort(expectedDist);
            Arrays.sort(actualX);
            Arrays.sort(actualY);
            Arrays.sort(actualZ);
            Arrays.sort(actualDist);

            // Comparing arrays
            assertArrayEquals(expectedX,actualX);
            assertArrayEquals(expectedY,actualY);
            assertArrayEquals(expectedZ,actualZ);
            assertArrayEquals(expectedDist,actualDist,tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidLink20px(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new Spots3D(volumeType).getObjects(inputSpotsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,20.0);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = new Objects3D(volumeType).getOtherValues();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_X.name());
            int[] expectedY = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_Y.name());
            int[] expectedZ = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_Z.name());
            double[] expectedDist = (double[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name());

            // Getting child objects (those linked here)
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

            // Each object won't necessarily have any children
            if (childSpots == null) continue;

            // Testing the number of children
            assertEquals(expectedX.length,childSpots.size());

            // Putting actual values into arrays
            int[] actualX = new int[childSpots.size()];
            int[] actualY = new int[childSpots.size()];
            int[] actualZ = new int[childSpots.size()];
            double[] actualDist = new double[childSpots.size()];

            int iter = 0;
            for (Obj childSpot:childSpots.values()) {
                actualX[iter] = (int) childSpot.getX(true)[0];
                actualY[iter] = (int) childSpot.getY(true)[0];
                actualZ[iter] = (int) childSpot.getZ(true,false)[0];
                String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,inputObjectsName);
                actualDist[iter++] = childSpot.getMeasurement(name).getValue();

            }

            // Sorting arrays
            Arrays.sort(expectedX);
            Arrays.sort(expectedY);
            Arrays.sort(expectedZ);
            Arrays.sort(expectedDist);
            Arrays.sort(actualX);
            Arrays.sort(actualY);
            Arrays.sort(actualZ);
            Arrays.sort(actualDist);

            // Comparing arrays
            assertArrayEquals(expectedX,actualX);
            assertArrayEquals(expectedY,actualY);
            assertArrayEquals(expectedZ,actualZ);
            assertArrayEquals(expectedDist,actualDist,tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidResponse2D(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedCentDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,proxObj1Name);
            double actualCentDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedCentDistPx, actualCentDistPx, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void trial(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

//        // Creating objects and adding to workspace
//        ObjCollection proxObj1 = new ProxCubes1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
//        workspace.addObjects(proxObj1);
//        ObjCollection proxObj2 = new ProxCubes2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
//        workspace.addObjects(proxObj2);

        ObjCollection proxObj1 = new ProxCubes1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        ObjCollection c1 = new ObjCollection(proxObj1Name,proxObj1);
        c1.add(proxObj1.get(20));
        workspace.addObjects(c1);
        ObjCollection proxObj2 = new ProxCubes2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        ObjCollection c2 = new ObjCollection(proxObj2Name,proxObj2);
        c2.add(proxObj2.get(12));
        workspace.addObjects(c2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());

        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        relateObjects.execute(workspace);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximitySurfaceLink(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        ObjCollection c1 = new ObjCollection(proxObj1Name,proxObj1);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_ID.name()).getValue();
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID, tolerance);

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj2Name);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_CAL.name()).getValue();
            name = Units.replace(RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_CAL,proxObj2Name));
            double actualSurfDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximitySurfaceLink5px(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,5.0);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_ID_5PX.name()).getValue();
            if (Double.isNaN(expectedParentID)) {
                assertNull(parentObj);
            } else {
                int actualParentID = parentObj.getID();
                assertEquals(expectedParentID, actualParentID, tolerance);
            }

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_PX_5PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj2Name);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_CAL_5PX.name()).getValue();
            name = Units.replace(RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_CAL,proxObj2Name));
            double actualSurfDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximitySurfaceResponse2DInOut(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.SURF_PROX_DIST_PX_INOUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximitySurfaceResponse2DIn(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.SURF_PROX_DIST_PX_IN.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximitySurfaceResponse2DOut(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.OUTSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.SURF_PROX_DIST_PX_OUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidSurfaceLink(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_ID.name()).getValue();
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID, tolerance);

            // Checking the distance to the parent
            double expectedDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj2Name);
            double actualfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistPx, actualfDistPx, tolerance);

            double expectedDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_CAL.name()).getValue();
            name = Units.replace(RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_CAL,proxObj2Name));
            double actualDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistCal, actualDistCal, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidSurfaceLink5px(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,5.0);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_ID_5PX.name()).getValue();
            if (Double.isNaN(expectedParentID)) {
                assertNull(parentObj);
            } else {
                int actualParentID = parentObj.getID();
                assertEquals(expectedParentID, actualParentID, tolerance);
            }

            // Checking the distance to the parent
            double expectedDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_PX_5PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj2Name);
            double actualfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistPx, actualfDistPx, tolerance);

            double expectedDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_CAL_5PX.name()).getValue();
            name = Units.replace(RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_CAL,proxObj2Name));
            double actualDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistCal, actualDistCal, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidSurfaceResponse2DInOut(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

//        ObjCollection o1 = new ObjCollection(proxObj1Name);
//        o1.add(proxObj1.getFirst());
//        workspace.addObjects(o1);
//        ObjCollection o2 = new ObjCollection(proxObj2Name);
//        o2.add(proxObj2.getFirst());
//        workspace.addObjects(o2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_SURF_PROX_DIST_PX_INOUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidSurfaceResponse2DIn(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_SURF_PROX_DIST_PX_IN.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testProximityCentroidSurfaceResponse2DOut(VolumeType volumeType) {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1(volumeType).getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2(volumeType).getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(new ModuleCollection());
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.OUTSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_SURF_PROX_DIST_PX_OUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test @Disabled
    public void testProximityToChildren() {
    }

    @Test @Disabled
    public void testSpatialLinking() {
    }

}